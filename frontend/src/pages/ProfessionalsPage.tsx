import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarRange, Plus } from "lucide-react";
import { useDeferredValue, useState, type FormEvent } from "react";
import { apiRequest, toQueryString } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { Button } from "../components/ui/Button";
import { DataCollection, type DataColumn } from "../components/ui/DataCollection";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/DataState";
import { Dialog } from "../components/ui/Dialog";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Input, Select } from "../components/ui/FormField";
import { PageHeader } from "../components/ui/PageHeader";
import { Pagination } from "../components/ui/Pagination";
import { ResourceActions } from "../components/ui/ResourceActions";
import { ResourceToolbar } from "../components/ui/ResourceToolbar";
import { ActiveBadge } from "../components/ui/StatusBadge";
import type {
  Availability,
  DayOfWeek,
  PageResponse,
  Professional,
  Specialty,
  UserAccount,
} from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { dayLabels } from "../utils/format";

const days = Object.keys(dayLabels) as DayOfWeek[];

interface ProfessionalForm {
  userId: string;
  specialtyId: string;
  name: string;
  registrationNumber: string;
  registrationState: string;
  phone: string;
  email: string;
}

const emptyProfessional: ProfessionalForm = {
  userId: "",
  specialtyId: "",
  name: "",
  registrationNumber: "",
  registrationState: "",
  phone: "",
  email: "",
};

export function ProfessionalsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [specialtyFilter, setSpecialtyFilter] = useState("");
  const [activeFilter, setActiveFilter] = useState("true");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Professional | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [availabilityProfessional, setAvailabilityProfessional] = useState<Professional | null>(null);
  const canManage = user?.role === "ADMIN";

  const specialtiesQuery = useQuery({
    queryKey: ["specialties", "options"],
    queryFn: () => apiRequest<PageResponse<Specialty>>("/specialties?active=true&size=100&sort=name,asc"),
  });
  const usersQuery = useQuery({
    queryKey: ["users", "doctor-options"],
    queryFn: () => apiRequest<PageResponse<UserAccount>>("/users?role=DOCTOR&active=true&size=100&sort=name,asc"),
    enabled: canManage,
  });
  const professionalsQuery = useQuery({
    queryKey: ["professionals", deferredSearch, specialtyFilter, activeFilter, page],
    queryFn: () => apiRequest<PageResponse<Professional>>(`/professionals${toQueryString({
      search: deferredSearch,
      specialtyId: specialtyFilter || undefined,
      active: activeFilter || undefined,
      page,
      size: 20,
      sort: "name,asc",
    })}`),
  });
  const statusMutation = useMutation({
    mutationFn: (professional: Professional) => apiRequest<Professional>(`/professionals/${professional.id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ active: !professional.active }),
    }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["professionals"] }),
  });

  const openEditor = (professional: Professional | null) => {
    setEditing(professional);
    setDialogOpen(true);
  };

  const columns: DataColumn<Professional>[] = [
    { key: "name", header: "Profissional", render: (item) => <div className="primary-cell"><strong>{item.name}</strong><span>{item.email || "E-mail não informado"}</span></div> },
    { key: "specialty", header: "Especialidade", render: (item) => item.specialtyName },
    { key: "registration", header: "Registro", render: (item) => `${item.registrationNumber} · ${item.registrationState}` },
    { key: "status", header: "Status", render: (item) => <ActiveBadge active={item.active} /> },
    { key: "schedule", header: "Horários", render: (item) => <Button variant="ghost" size="sm" icon={<CalendarRange size={16} />} onClick={() => setAvailabilityProfessional(item)}>Ver horários</Button> },
    { key: "actions", header: "Ações", align: "right", render: (item) => <ResourceActions active={item.active} canManage={canManage} onEdit={() => openEditor(item)} onToggleStatus={() => statusMutation.mutate(item)} /> },
  ];

  return (
    <div className="page-stack">
      <PageHeader eyebrow="Equipe clínica" title="Profissionais" description="Cadastre a equipe, seus registros e os períodos disponíveis para atendimento." action={canManage ? <Button icon={<Plus size={17} />} onClick={() => openEditor(null)}>Novo profissional</Button> : undefined} />
      <section className="resource-panel" aria-label="Lista de profissionais">
        <ResourceToolbar search={search} searchLabel="Buscar profissional ou registro" onSearchChange={(value) => { setSearch(value); setPage(0); }}>
          <Select aria-label="Filtrar por especialidade" value={specialtyFilter} onChange={(event) => { setSpecialtyFilter(event.target.value); setPage(0); }}><option value="">Todas as especialidades</option>{specialtiesQuery.data?.content.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select>
          <Select aria-label="Filtrar por status" value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); setPage(0); }}><option value="">Todos os status</option><option value="true">Ativos</option><option value="false">Inativos</option></Select>
        </ResourceToolbar>
        {professionalsQuery.isLoading ? <LoadingState /> : null}
        {professionalsQuery.isError ? <ErrorState description="Não foi possível carregar os profissionais." onRetry={() => void professionalsQuery.refetch()} /> : null}
        {professionalsQuery.data?.content.length === 0 ? <EmptyState title="Nenhum profissional encontrado" description="Cadastre a equipe clínica ou ajuste os filtros." action={canManage ? <Button onClick={() => openEditor(null)}>Cadastrar profissional</Button> : undefined} /> : null}
        {professionalsQuery.data?.content.length ? <><DataCollection items={professionalsQuery.data.content} columns={columns} getKey={(item) => item.id} caption="Profissionais cadastrados" renderMobile={(item) => <><div className="mobile-record__header"><div className="primary-cell"><strong>{item.name}</strong><span>{item.specialtyName}</span></div><ActiveBadge active={item.active} /></div><dl><div><dt>Registro</dt><dd>{item.registrationNumber} · {item.registrationState}</dd></div><div><dt>Telefone</dt><dd>{item.phone || "Não informado"}</dd></div></dl><div className="mobile-record__actions"><Button variant="secondary" size="sm" icon={<CalendarRange size={16} />} onClick={() => setAvailabilityProfessional(item)}>Horários</Button><ResourceActions active={item.active} canManage={canManage} onEdit={() => openEditor(item)} onToggleStatus={() => statusMutation.mutate(item)} /></div></>} /><Pagination page={professionalsQuery.data.page} totalPages={professionalsQuery.data.totalPages} totalElements={professionalsQuery.data.totalElements} onPageChange={setPage} /></> : null}
      </section>
      {dialogOpen ? <ProfessionalDialog professional={editing} specialties={specialtiesQuery.data?.content ?? []} users={usersQuery.data?.content ?? []} open onOpenChange={setDialogOpen} onSaved={() => { setDialogOpen(false); void queryClient.invalidateQueries({ queryKey: ["professionals"] }); }} /> : null}
      {availabilityProfessional ? <AvailabilityDialog professional={availabilityProfessional} canManage={canManage} onOpenChange={(open) => { if (!open) setAvailabilityProfessional(null); }} /> : null}
    </div>
  );
}

function ProfessionalDialog({ professional, specialties, users, open, onOpenChange, onSaved }: { professional: Professional | null; specialties: Specialty[]; users: UserAccount[]; open: boolean; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [form, setForm] = useState<ProfessionalForm>(() => professional ? { userId: professional.userId ?? "", specialtyId: professional.specialtyId, name: professional.name, registrationNumber: professional.registrationNumber, registrationState: professional.registrationState, phone: professional.phone ?? "", email: professional.email ?? "" } : { ...emptyProfessional, specialtyId: specialties[0]?.id ?? "" });
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({
    mutationFn: () => apiRequest<Professional>(professional ? `/professionals/${professional.id}` : "/professionals", { method: professional ? "PUT" : "POST", body: JSON.stringify({ ...form, userId: form.userId || null, phone: form.phone || null, email: form.email || null, registrationState: form.registrationState.toUpperCase() }) }),
    onSuccess: onSaved,
    onError: (requestError) => setError(getErrorMessage(requestError)),
  });
  return <Dialog open={open} onOpenChange={onOpenChange} title={professional ? "Editar profissional" : "Novo profissional"} description="Vincule o acesso ao sistema apenas quando o profissional também precisar entrar na plataforma." width="lg" footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="professional-form" isLoading={mutation.isPending}>Salvar profissional</Button></>}>
    <form id="professional-form" className="form-grid" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
      {error ? <div className="form-grid__full"><FeedbackMessage type="error">{error}</FeedbackMessage></div> : null}
      <div className="form-grid__full"><Field label="Nome completo" htmlFor="professional-name" required><Input id="professional-name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} maxLength={120} required /></Field></div>
      <Field label="Especialidade" htmlFor="professional-specialty" required><Select id="professional-specialty" value={form.specialtyId} onChange={(event) => setForm({ ...form, specialtyId: event.target.value })} required><option value="">Selecione</option>{specialties.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select></Field>
      <Field label="Usuário vinculado" htmlFor="professional-user" hint="Opcional"><Select id="professional-user" value={form.userId} onChange={(event) => setForm({ ...form, userId: event.target.value })}><option value="">Sem acesso vinculado</option>{users.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.email}</option>)}</Select></Field>
      <Field label="Número do registro" htmlFor="professional-registration" required><Input id="professional-registration" value={form.registrationNumber} onChange={(event) => setForm({ ...form, registrationNumber: event.target.value })} maxLength={40} required /></Field>
      <Field label="UF do registro" htmlFor="professional-state" required><Input id="professional-state" value={form.registrationState} onChange={(event) => setForm({ ...form, registrationState: event.target.value.slice(0, 2).toUpperCase() })} maxLength={2} required /></Field>
      <Field label="Telefone" htmlFor="professional-phone" hint="Opcional"><Input id="professional-phone" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} maxLength={20} /></Field>
      <Field label="E-mail" htmlFor="professional-email" hint="Opcional"><Input id="professional-email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} maxLength={160} /></Field>
    </form>
  </Dialog>;
}

function AvailabilityDialog({ professional, canManage, onOpenChange }: { professional: Professional; canManage: boolean; onOpenChange: (open: boolean) => void }) {
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState<Availability | null>(null);
  const [form, setForm] = useState({ dayOfWeek: "MONDAY" as DayOfWeek, startTime: "08:00", endTime: "12:00" });
  const [error, setError] = useState<string | null>(null);
  const query = useQuery({ queryKey: ["availabilities", professional.id], queryFn: () => apiRequest<PageResponse<Availability>>(`/availabilities/professional/${professional.id}?size=100`) });
  const saveMutation = useMutation({
    mutationFn: () => apiRequest<Availability>(editing ? `/availabilities/${editing.id}` : "/availabilities", { method: editing ? "PUT" : "POST", body: JSON.stringify(editing ? form : { ...form, professionalId: professional.id }) }),
    onSuccess: () => { setEditing(null); setError(null); void queryClient.invalidateQueries({ queryKey: ["availabilities", professional.id] }); },
    onError: (requestError) => setError(getErrorMessage(requestError)),
  });
  const statusMutation = useMutation({ mutationFn: (item: Availability) => apiRequest<Availability>(`/availabilities/${item.id}/status`, { method: "PATCH", body: JSON.stringify({ active: !item.active }) }), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["availabilities", professional.id] }) });
  const selectAvailability = (item: Availability) => { setEditing(item); setForm({ dayOfWeek: item.dayOfWeek, startTime: item.startTime.slice(0, 5), endTime: item.endTime.slice(0, 5) }); setError(null); };
  return <Dialog open onOpenChange={onOpenChange} title={`Disponibilidade · ${professional.name}`} description="Os agendamentos precisam caber integralmente em um período ativo." width="lg" footer={<Button variant="secondary" onClick={() => onOpenChange(false)}>Fechar</Button>}>
    {canManage ? <form className="availability-form" onSubmit={(event) => { event.preventDefault(); saveMutation.mutate(); }}>
      <Field label="Dia" htmlFor="availability-day"><Select id="availability-day" value={form.dayOfWeek} onChange={(event) => setForm({ ...form, dayOfWeek: event.target.value as DayOfWeek })}>{days.map((day) => <option key={day} value={day}>{dayLabels[day]}</option>)}</Select></Field>
      <Field label="Início" htmlFor="availability-start"><Input id="availability-start" type="time" value={form.startTime} onChange={(event) => setForm({ ...form, startTime: event.target.value })} required /></Field>
      <Field label="Fim" htmlFor="availability-end"><Input id="availability-end" type="time" value={form.endTime} onChange={(event) => setForm({ ...form, endTime: event.target.value })} required /></Field>
      <Button type="submit" isLoading={saveMutation.isPending}>{editing ? "Atualizar" : "Adicionar"}</Button>
      {editing ? <Button variant="ghost" onClick={() => setEditing(null)}>Cancelar edição</Button> : null}
    </form> : null}
    {error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}
    {query.isLoading ? <LoadingState rows={3} /> : null}
    {query.data?.content.length === 0 ? <EmptyState title="Sem horários disponíveis" description="Adicione os períodos regulares de atendimento deste profissional." /> : null}
    {query.data?.content.length ? <div className="availability-list">{query.data.content.map((item) => <div className="availability-item" key={item.id}><div><strong>{dayLabels[item.dayOfWeek]}</strong><span>{item.startTime.slice(0, 5)} — {item.endTime.slice(0, 5)}</span></div><ActiveBadge active={item.active} />{canManage ? <ResourceActions active={item.active} canManage onEdit={() => selectAvailability(item)} onToggleStatus={() => statusMutation.mutate(item)} /> : null}</div>)}</div> : null}
  </Dialog>;
}
