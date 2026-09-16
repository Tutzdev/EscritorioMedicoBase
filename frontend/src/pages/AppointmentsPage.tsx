import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarPlus, Check, Clock3, Play, RotateCcw, UserX, XCircle } from "lucide-react";
import { useMemo, useState, type FormEvent } from "react";
import { apiRequest, toQueryString } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { Button } from "../components/ui/Button";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/DataState";
import { Dialog } from "../components/ui/Dialog";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Input, Select, Textarea } from "../components/ui/FormField";
import { PageHeader } from "../components/ui/PageHeader";
import { Pagination } from "../components/ui/Pagination";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { Appointment, AppointmentStatus, PageResponse, Patient, Professional, ServiceOffering } from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { formatDateTime, formatTime, statusLabels, toDateTimeLocal, toOffsetDateTime } from "../utils/format";

const statuses = Object.keys(statusLabels) as AppointmentStatus[];

export function AppointmentsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [date, setDate] = useState(todayValue());
  const [status, setStatus] = useState("");
  const [professionalId, setProfessionalId] = useState("");
  const [page, setPage] = useState(0);
  const [editorOpen, setEditorOpen] = useState(false);
  const [rescheduling, setRescheduling] = useState<Appointment | null>(null);
  const [canceling, setCanceling] = useState<Appointment | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const canSchedule = user?.role === "ADMIN" || user?.role === "RECEPTIONIST";

  const range = useMemo(() => dayRange(date), [date]);
  const appointmentsQuery = useQuery({
    queryKey: ["appointments", date, status, professionalId, page],
    queryFn: () => apiRequest<PageResponse<Appointment>>(`/appointments${toQueryString({ status: status || undefined, professionalId: professionalId || undefined, from: range.from, to: range.to, page, size: 20, sort: "scheduledStart,asc" })}`),
  });
  const professionalsQuery = useQuery({ queryKey: ["professionals", "options"], queryFn: () => apiRequest<PageResponse<Professional>>("/professionals?active=true&size=100&sort=name,asc") });
  const patientsQuery = useQuery({ queryKey: ["patients", "options"], queryFn: () => apiRequest<PageResponse<Patient>>("/patients?active=true&size=100&sort=name,asc") });
  const servicesQuery = useQuery({ queryKey: ["services", "options"], queryFn: () => apiRequest<PageResponse<ServiceOffering>>("/services?active=true&size=100&sort=name,asc") });
  const actionMutation = useMutation({
    mutationFn: ({ appointment, nextStatus }: { appointment: Appointment; nextStatus: AppointmentStatus }) => apiRequest<Appointment>(`/appointments/${appointment.id}/status`, { method: "PATCH", body: JSON.stringify({ status: nextStatus }) }),
    onSuccess: () => { setActionError(null); void queryClient.invalidateQueries({ queryKey: ["appointments"] }); void queryClient.invalidateQueries({ queryKey: ["dashboard"] }); },
    onError: (requestError) => setActionError(getErrorMessage(requestError)),
  });

  const onSaved = () => {
    setEditorOpen(false);
    setRescheduling(null);
    setCanceling(null);
    void queryClient.invalidateQueries({ queryKey: ["appointments"] });
    void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
  };

  return <div className="page-stack">
    <PageHeader eyebrow="Operação diária" title="Agenda" description="Organize horários com validação de disponibilidade, duração e conflitos." action={canSchedule ? <Button icon={<CalendarPlus size={17} />} onClick={() => setEditorOpen(true)}>Novo agendamento</Button> : undefined} />
    <section className="agenda-toolbar" aria-label="Filtros da agenda">
      <Field label="Data" htmlFor="agenda-date"><Input id="agenda-date" type="date" value={date} onChange={(event) => { setDate(event.target.value); setPage(0); }} /></Field>
      <Field label="Profissional" htmlFor="agenda-professional"><Select id="agenda-professional" value={professionalId} onChange={(event) => { setProfessionalId(event.target.value); setPage(0); }}><option value="">Todos</option>{professionalsQuery.data?.content.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select></Field>
      <Field label="Status" htmlFor="agenda-status"><Select id="agenda-status" value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }}><option value="">Todos</option>{statuses.map((item) => <option key={item} value={item}>{statusLabels[item]}</option>)}</Select></Field>
      <Button variant="ghost" size="sm" icon={<RotateCcw size={15} />} onClick={() => { setDate(todayValue()); setStatus(""); setProfessionalId(""); setPage(0); }}>Hoje</Button>
    </section>
    {actionError ? <FeedbackMessage type="error">{actionError}</FeedbackMessage> : null}
    <section className="agenda-panel" aria-label="Compromissos da agenda">
      <header className="section-header"><div><p className="eyebrow">Linha clínica</p><h2>{formatDayTitle(date)}</h2></div><span>{appointmentsQuery.data?.totalElements ?? 0} horário(s)</span></header>
      {appointmentsQuery.isLoading ? <LoadingState /> : null}
      {appointmentsQuery.isError ? <ErrorState description="Não foi possível carregar a agenda." onRetry={() => void appointmentsQuery.refetch()} /> : null}
      {appointmentsQuery.data?.content.length === 0 ? <EmptyState title="Agenda livre neste dia" description="Nenhum atendimento corresponde aos filtros selecionados." action={canSchedule ? <Button onClick={() => setEditorOpen(true)}>Criar agendamento</Button> : undefined} /> : null}
      {appointmentsQuery.data?.content.length ? <div className="schedule-list">{appointmentsQuery.data.content.map((appointment) => <AppointmentRow key={appointment.id} appointment={appointment} canSchedule={canSchedule} isUpdating={actionMutation.isPending} onStatus={(nextStatus) => actionMutation.mutate({ appointment, nextStatus })} onReschedule={() => setRescheduling(appointment)} onCancel={() => setCanceling(appointment)} />)}</div> : null}
      {appointmentsQuery.data ? <Pagination page={appointmentsQuery.data.page} totalPages={appointmentsQuery.data.totalPages} totalElements={appointmentsQuery.data.totalElements} onPageChange={setPage} /> : null}
    </section>
    {editorOpen || rescheduling ? <AppointmentEditor open appointment={rescheduling} patients={patientsQuery.data?.content ?? []} professionals={professionalsQuery.data?.content ?? []} services={servicesQuery.data?.content ?? []} onOpenChange={(open) => { setEditorOpen(open); if (!open) setRescheduling(null); }} onSaved={onSaved} /> : null}
    {canceling ? <CancellationDialog appointment={canceling} onOpenChange={(open) => { if (!open) setCanceling(null); }} onSaved={onSaved} /> : null}
  </div>;
}

function AppointmentRow({ appointment, canSchedule, isUpdating, onStatus, onReschedule, onCancel }: { appointment: Appointment; canSchedule: boolean; isUpdating: boolean; onStatus: (status: AppointmentStatus) => void; onReschedule: () => void; onCancel: () => void }) {
  const canConfirm = appointment.status === "SCHEDULED";
  const canStart = appointment.status === "CONFIRMED";
  const canMarkNoShow = appointment.status === "SCHEDULED" || appointment.status === "CONFIRMED";
  const canModifySchedule = canSchedule && (appointment.status === "SCHEDULED" || appointment.status === "CONFIRMED");
  return <article className="schedule-item">
    <time className="schedule-item__time"><strong>{formatTime(appointment.scheduledStart)}</strong><span>{formatTime(appointment.scheduledEnd)}</span></time>
    <span className="schedule-item__rail" aria-hidden="true" />
    <div className="schedule-item__main"><div className="schedule-item__title"><div><strong>{appointment.patientName}</strong><span>{appointment.serviceName} · {appointment.durationMinutes} min</span></div><StatusBadge status={appointment.status} /></div><p>{appointment.professionalName}</p>{appointment.notes ? <small>{appointment.notes}</small> : null}</div>
    <div className="schedule-item__actions">
      {canConfirm ? <Button variant="secondary" size="sm" icon={<Check size={15} />} disabled={isUpdating} onClick={() => onStatus("CONFIRMED")}>Confirmar</Button> : null}
      {canStart ? <Button variant="secondary" size="sm" icon={<Play size={15} />} disabled={isUpdating} onClick={() => onStatus("IN_PROGRESS")}>Iniciar</Button> : null}
      {canMarkNoShow ? <Button variant="ghost" size="sm" icon={<UserX size={15} />} disabled={isUpdating} onClick={() => onStatus("NO_SHOW")}>Não compareceu</Button> : null}
      {canModifySchedule ? <Button variant="ghost" size="sm" icon={<Clock3 size={15} />} onClick={onReschedule}>Reagendar</Button> : null}
      {canModifySchedule ? <Button variant="ghost" size="sm" icon={<XCircle size={15} />} onClick={onCancel}>Cancelar</Button> : null}
    </div>
  </article>;
}

function AppointmentEditor({ open, appointment, patients, professionals, services, onOpenChange, onSaved }: { open: boolean; appointment: Appointment | null; patients: Patient[]; professionals: Professional[]; services: ServiceOffering[]; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [form, setForm] = useState(() => appointment ? { patientId: appointment.patientId, professionalId: appointment.professionalId, serviceId: appointment.serviceId, scheduledStart: toDateTimeLocal(appointment.scheduledStart), notes: appointment.notes ?? "" } : { patientId: patients[0]?.id ?? "", professionalId: professionals[0]?.id ?? "", serviceId: "", scheduledStart: nextHourValue(), notes: "" });
  const [error, setError] = useState<string | null>(null);
  const selectedProfessional = professionals.find((item) => item.id === form.professionalId);
  const compatibleServices = services.filter((item) => !selectedProfessional || item.specialtyId === selectedProfessional.specialtyId);
  const mutation = useMutation({
    mutationFn: () => apiRequest<Appointment>(appointment ? `/appointments/${appointment.id}/schedule` : "/appointments", { method: appointment ? "PUT" : "POST", body: JSON.stringify(appointment ? { scheduledStart: toOffsetDateTime(form.scheduledStart) } : { patientId: form.patientId, professionalId: form.professionalId, serviceId: form.serviceId, scheduledStart: toOffsetDateTime(form.scheduledStart), notes: form.notes || null }) }),
    onSuccess: onSaved,
    onError: (requestError) => setError(getErrorMessage(requestError)),
  });
  return <Dialog open={open} onOpenChange={onOpenChange} title={appointment ? "Reagendar atendimento" : "Novo agendamento"} description={appointment ? `${appointment.patientName} · ${appointment.serviceName}` : "A duração final e os conflitos são validados automaticamente."} width="lg" footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="appointment-form" isLoading={mutation.isPending}>{appointment ? "Confirmar novo horário" : "Criar agendamento"}</Button></>}>
    <form id="appointment-form" className="form-grid" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
      {error ? <div className="form-grid__full"><FeedbackMessage type="error">{error}</FeedbackMessage></div> : null}
      {!appointment ? <><Field label="Paciente" htmlFor="appointment-patient" required><Select id="appointment-patient" value={form.patientId} onChange={(event) => setForm({ ...form, patientId: event.target.value })} required><option value="">Selecione</option>{patients.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select></Field><Field label="Profissional" htmlFor="appointment-professional" required><Select id="appointment-professional" value={form.professionalId} onChange={(event) => setForm({ ...form, professionalId: event.target.value, serviceId: "" })} required><option value="">Selecione</option>{professionals.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.specialtyName}</option>)}</Select></Field><Field label="Serviço" htmlFor="appointment-service" required><Select id="appointment-service" value={form.serviceId} onChange={(event) => setForm({ ...form, serviceId: event.target.value })} required><option value="">Selecione</option>{compatibleServices.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.durationMinutes} min</option>)}</Select></Field></> : null}
      <Field label="Data e horário" htmlFor="appointment-time" required><Input id="appointment-time" type="datetime-local" min={toDateTimeLocal(new Date().toISOString())} value={form.scheduledStart} onChange={(event) => setForm({ ...form, scheduledStart: event.target.value })} required /></Field>
      {!appointment ? <div className="form-grid__full"><Field label="Observações" htmlFor="appointment-notes" hint="Opcional"><Textarea id="appointment-notes" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} maxLength={1000} rows={3} /></Field></div> : null}
    </form>
  </Dialog>;
}

function CancellationDialog({ appointment, onOpenChange, onSaved }: { appointment: Appointment; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({ mutationFn: () => apiRequest<Appointment>(`/appointments/${appointment.id}/cancellation`, { method: "POST", body: JSON.stringify({ reason }) }), onSuccess: onSaved, onError: (requestError) => setError(getErrorMessage(requestError)) });
  return <Dialog open onOpenChange={onOpenChange} title="Cancelar agendamento" description={`${appointment.patientName} · ${formatDateTime(appointment.scheduledStart)}`} footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Voltar</Button><Button variant="danger" type="submit" form="cancellation-form" isLoading={mutation.isPending}>Confirmar cancelamento</Button></>}><form id="cancellation-form" className="form-stack" onSubmit={(event) => { event.preventDefault(); mutation.mutate(); }}>{error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}<Field label="Motivo" htmlFor="cancellation-reason" required><Textarea id="cancellation-reason" value={reason} onChange={(event) => setReason(event.target.value)} maxLength={500} rows={4} required /></Field></form></Dialog>;
}

function todayValue(): string {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 10);
}

function nextHourValue(): string {
  const date = new Date();
  date.setHours(date.getHours() + 1, 0, 0, 0);
  return toDateTimeLocal(date.toISOString());
}

function dayRange(value: string): { from: string; to: string } {
  const start = new Date(`${value}T00:00:00`);
  const end = new Date(start);
  end.setDate(end.getDate() + 1);
  return { from: start.toISOString(), to: end.toISOString() };
}

function formatDayTitle(value: string): string {
  return new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "2-digit", month: "long" }).format(new Date(`${value}T12:00:00`));
}
