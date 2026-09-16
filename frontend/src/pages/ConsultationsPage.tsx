import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, ClipboardPlus, Pencil } from "lucide-react";
import { useDeferredValue, useState, type FormEvent } from "react";
import { apiRequest, toQueryString } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { Button } from "../components/ui/Button";
import { DataCollection, type DataColumn } from "../components/ui/DataCollection";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/DataState";
import { Dialog } from "../components/ui/Dialog";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Select, Textarea } from "../components/ui/FormField";
import { PageHeader } from "../components/ui/PageHeader";
import { Pagination } from "../components/ui/Pagination";
import { ResourceToolbar } from "../components/ui/ResourceToolbar";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { Appointment, Consultation, PageResponse, Professional } from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { formatDateTime } from "../utils/format";

export function ConsultationsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [professionalId, setProfessionalId] = useState("");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Consultation | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const canDocument = user?.role === "ADMIN" || user?.role === "DOCTOR";

  const consultationsQuery = useQuery({
    queryKey: ["consultations", deferredSearch, professionalId, page],
    queryFn: () => apiRequest<PageResponse<Consultation>>(`/consultations${toQueryString({ search: deferredSearch, professionalId: professionalId || undefined, page, size: 20, sort: "createdAt,desc" })}`),
  });
  const professionalsQuery = useQuery({ queryKey: ["professionals", "options"], queryFn: () => apiRequest<PageResponse<Professional>>("/professionals?active=true&size=100&sort=name,asc") });
  const candidatesQuery = useQuery({
    queryKey: ["appointments", "consultation-candidates"],
    queryFn: async () => {
      const results = await Promise.all(["SCHEDULED", "CONFIRMED", "IN_PROGRESS"].map((status) => apiRequest<PageResponse<Appointment>>(`/appointments?status=${status}&size=100&sort=scheduledStart,asc`)));
      return results.flatMap((result) => result.content).sort((left, right) => left.scheduledStart.localeCompare(right.scheduledStart));
    },
    enabled: canDocument,
  });
  const completeMutation = useMutation({
    mutationFn: (item: Consultation) => apiRequest<Consultation>(`/consultations/${item.id}/completion`, { method: "POST" }),
    onSuccess: () => { setActionError(null); void queryClient.invalidateQueries({ queryKey: ["consultations"] }); void queryClient.invalidateQueries({ queryKey: ["appointments"] }); void queryClient.invalidateQueries({ queryKey: ["dashboard"] }); },
    onError: (requestError) => setActionError(getErrorMessage(requestError)),
  });

  const columns: DataColumn<Consultation>[] = [
    { key: "patient", header: "Paciente", render: (item) => <div className="primary-cell"><strong>{item.patientName}</strong><span>{formatDateTime(item.scheduledStart)}</span></div> },
    { key: "professional", header: "Profissional", render: (item) => item.professionalName },
    { key: "status", header: "Status", render: (item) => <StatusBadge status={item.appointmentStatus} /> },
    { key: "observations", header: "Registro", render: (item) => <span className="clamped-text">{item.observations}</span> },
    { key: "actions", header: "Ações", align: "right", render: (item) => canDocument ? <div className="row-actions"><button className="icon-button" aria-label="Editar observações" onClick={() => { setEditing(item); setDialogOpen(true); }}><Pencil size={16} /></button>{item.appointmentStatus === "IN_PROGRESS" ? <button className="icon-button icon-button--success" aria-label="Concluir atendimento" onClick={() => completeMutation.mutate(item)}><CheckCircle2 size={17} /></button> : null}</div> : <span className="muted-text">Somente leitura</span> },
  ];

  return <div className="page-stack">
    <PageHeader eyebrow="Registro assistencial básico" title="Atendimentos" description="Registre observações essenciais e conclua o fluxo iniciado na agenda." action={canDocument ? <Button icon={<ClipboardPlus size={17} />} onClick={() => { setEditing(null); setDialogOpen(true); }}>Iniciar atendimento</Button> : undefined} />
    {actionError ? <FeedbackMessage type="error">{actionError}</FeedbackMessage> : null}
    <section className="resource-panel" aria-label="Atendimentos registrados">
      <ResourceToolbar search={search} searchLabel="Buscar paciente ou profissional" onSearchChange={(value) => { setSearch(value); setPage(0); }}><Select aria-label="Filtrar por profissional" value={professionalId} onChange={(event) => { setProfessionalId(event.target.value); setPage(0); }}><option value="">Todos os profissionais</option>{professionalsQuery.data?.content.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select></ResourceToolbar>
      {consultationsQuery.isLoading ? <LoadingState /> : null}
      {consultationsQuery.isError ? <ErrorState description="Não foi possível carregar os atendimentos." onRetry={() => void consultationsQuery.refetch()} /> : null}
      {consultationsQuery.data?.content.length === 0 ? <EmptyState title="Nenhum atendimento registrado" description="Inicie o registro a partir de um agendamento válido." action={canDocument ? <Button onClick={() => setDialogOpen(true)}>Iniciar atendimento</Button> : undefined} /> : null}
      {consultationsQuery.data?.content.length ? <><DataCollection items={consultationsQuery.data.content} columns={columns} getKey={(item) => item.id} caption="Atendimentos registrados" renderMobile={(item) => <><div className="mobile-record__header"><div className="primary-cell"><strong>{item.patientName}</strong><span>{formatDateTime(item.scheduledStart)}</span></div><StatusBadge status={item.appointmentStatus} /></div><p className="clamped-text">{item.observations}</p><div className="mobile-record__actions">{canDocument ? <Button variant="secondary" size="sm" icon={<Pencil size={15} />} onClick={() => { setEditing(item); setDialogOpen(true); }}>Editar</Button> : null}{canDocument && item.appointmentStatus === "IN_PROGRESS" ? <Button size="sm" icon={<CheckCircle2 size={15} />} onClick={() => completeMutation.mutate(item)}>Concluir</Button> : null}</div></>} /><Pagination page={consultationsQuery.data.page} totalPages={consultationsQuery.data.totalPages} totalElements={consultationsQuery.data.totalElements} onPageChange={setPage} /></> : null}
    </section>
    {dialogOpen ? <ConsultationDialog consultation={editing} appointments={candidatesQuery.data ?? []} open onOpenChange={setDialogOpen} onSaved={() => { setDialogOpen(false); void queryClient.invalidateQueries({ queryKey: ["consultations"] }); void queryClient.invalidateQueries({ queryKey: ["appointments"] }); }} /> : null}
  </div>;
}

function ConsultationDialog({ consultation, appointments, open, onOpenChange, onSaved }: { consultation: Consultation | null; appointments: Appointment[]; open: boolean; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [appointmentId, setAppointmentId] = useState(consultation?.appointmentId ?? appointments[0]?.id ?? "");
  const [observations, setObservations] = useState(consultation?.observations ?? "");
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({ mutationFn: () => apiRequest<Consultation>(consultation ? `/consultations/${consultation.id}` : "/consultations", { method: consultation ? "PUT" : "POST", body: JSON.stringify(consultation ? { observations } : { appointmentId, observations }) }), onSuccess: onSaved, onError: (requestError) => setError(getErrorMessage(requestError)) });
  return <Dialog open={open} onOpenChange={onOpenChange} title={consultation ? "Editar registro" : "Iniciar atendimento"} description="Este núcleo registra observações gerais; especialidades podem evoluir em módulos próprios." width="lg" footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="consultation-form" isLoading={mutation.isPending}>Salvar registro</Button></>}>
    <form id="consultation-form" className="form-stack" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
      {error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}
      {!consultation ? <Field label="Agendamento" htmlFor="consultation-appointment" required><Select id="consultation-appointment" value={appointmentId} onChange={(event) => setAppointmentId(event.target.value)} required><option value="">Selecione</option>{appointments.map((item) => <option key={item.id} value={item.id}>{formatDateTime(item.scheduledStart)} · {item.patientName} · {item.professionalName}</option>)}</Select></Field> : null}
      <Field label="Observações do atendimento" htmlFor="consultation-observations" required><Textarea id="consultation-observations" value={observations} onChange={(event) => setObservations(event.target.value)} maxLength={5000} rows={10} required /></Field>
    </form>
  </Dialog>;
}
