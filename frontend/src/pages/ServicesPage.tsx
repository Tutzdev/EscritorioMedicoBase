import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { useDeferredValue, useState, type FormEvent } from "react";
import { apiRequest, toQueryString } from "../api/client";
import { Button } from "../components/ui/Button";
import { DataCollection, type DataColumn } from "../components/ui/DataCollection";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/DataState";
import { Dialog } from "../components/ui/Dialog";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Input, Select, Textarea } from "../components/ui/FormField";
import { PageHeader } from "../components/ui/PageHeader";
import { Pagination } from "../components/ui/Pagination";
import { ResourceActions } from "../components/ui/ResourceActions";
import { ResourceToolbar } from "../components/ui/ResourceToolbar";
import { ActiveBadge } from "../components/ui/StatusBadge";
import type { PageResponse, ServiceOffering, Specialty } from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { formatCurrency } from "../utils/format";

interface ServiceFormValue { specialtyId: string; name: string; description: string; durationMinutes: string; price: string }
const emptyService: ServiceFormValue = { specialtyId: "", name: "", description: "", durationMinutes: "30", price: "" };

export function ServicesPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [activeFilter, setActiveFilter] = useState("");
  const [specialtyFilter, setSpecialtyFilter] = useState("");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<ServiceOffering | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const specialtiesQuery = useQuery({ queryKey: ["specialties", "options"], queryFn: () => apiRequest<PageResponse<Specialty>>("/specialties?active=true&size=100&sort=name,asc") });
  const query = useQuery({ queryKey: ["services", deferredSearch, activeFilter, specialtyFilter, page], queryFn: () => apiRequest<PageResponse<ServiceOffering>>(`/services${toQueryString({ search: deferredSearch, active: activeFilter || undefined, specialtyId: specialtyFilter || undefined, page, size: 20, sort: "name,asc" })}`) });
  const statusMutation = useMutation({ mutationFn: (item: ServiceOffering) => apiRequest<ServiceOffering>(`/services/${item.id}/status`, { method: "PATCH", body: JSON.stringify({ active: !item.active }) }), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["services"] }) });
  const openDialog = (item: ServiceOffering | null) => { setEditing(item); setDialogOpen(true); };
  const columns: DataColumn<ServiceOffering>[] = [
    { key: "name", header: "Serviço", render: (item) => <div className="primary-cell"><strong>{item.name}</strong><span>{item.specialtyName}</span></div> },
    { key: "duration", header: "Duração", render: (item) => `${item.durationMinutes} min` },
    { key: "price", header: "Valor", render: (item) => formatCurrency(item.price) },
    { key: "status", header: "Status", render: (item) => <ActiveBadge active={item.active} /> },
    { key: "actions", header: "Ações", align: "right", render: (item) => <ResourceActions active={item.active} canManage onEdit={() => openDialog(item)} onToggleStatus={() => statusMutation.mutate(item)} /> },
  ];

  return <div className="page-stack">
    <PageHeader eyebrow="Catálogo de atendimento" title="Serviços" description="Defina duração, especialidade e valor dos tipos de atendimento." action={<Button icon={<Plus size={17} />} onClick={() => openDialog(null)}>Novo serviço</Button>} />
    <section className="resource-panel">
      <ResourceToolbar search={search} searchLabel="Buscar serviço" onSearchChange={(value) => { setSearch(value); setPage(0); }}>
        <Select aria-label="Filtrar por especialidade" value={specialtyFilter} onChange={(event) => { setSpecialtyFilter(event.target.value); setPage(0); }}><option value="">Todas as especialidades</option>{specialtiesQuery.data?.content.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select>
        <Select aria-label="Filtrar por status" value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); setPage(0); }}><option value="">Todos os status</option><option value="true">Ativos</option><option value="false">Inativos</option></Select>
      </ResourceToolbar>
      {query.isLoading ? <LoadingState /> : null}
      {query.isError ? <ErrorState description="Não foi possível carregar os serviços." onRetry={() => void query.refetch()} /> : null}
      {query.data?.content.length === 0 ? <EmptyState title="Nenhum serviço encontrado" description="Cadastre um tipo de atendimento para calcular a duração da agenda." action={<Button onClick={() => openDialog(null)}>Cadastrar serviço</Button>} /> : null}
      {query.data?.content.length ? <><DataCollection items={query.data.content} columns={columns} getKey={(item) => item.id} caption="Serviços cadastrados" renderMobile={(item) => <><div className="mobile-record__header"><div className="primary-cell"><strong>{item.name}</strong><span>{item.specialtyName}</span></div><ActiveBadge active={item.active} /></div><dl><div><dt>Duração</dt><dd>{item.durationMinutes} min</dd></div><div><dt>Valor</dt><dd>{formatCurrency(item.price)}</dd></div></dl><ResourceActions active={item.active} canManage onEdit={() => openDialog(item)} onToggleStatus={() => statusMutation.mutate(item)} /></>} /><Pagination page={query.data.page} totalPages={query.data.totalPages} totalElements={query.data.totalElements} onPageChange={setPage} /></> : null}
    </section>
    {dialogOpen ? <ServiceDialog item={editing} specialties={specialtiesQuery.data?.content ?? []} open onOpenChange={setDialogOpen} onSaved={() => { setDialogOpen(false); void queryClient.invalidateQueries({ queryKey: ["services"] }); }} /> : null}
  </div>;
}

function ServiceDialog({ item, specialties, open, onOpenChange, onSaved }: { item: ServiceOffering | null; specialties: Specialty[]; open: boolean; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [form, setForm] = useState<ServiceFormValue>(() => item ? { specialtyId: item.specialtyId, name: item.name, description: item.description ?? "", durationMinutes: String(item.durationMinutes), price: item.price === null ? "" : String(item.price) } : { ...emptyService, specialtyId: specialties[0]?.id ?? "" });
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({ mutationFn: () => apiRequest<ServiceOffering>(item ? `/services/${item.id}` : "/services", { method: item ? "PUT" : "POST", body: JSON.stringify({ specialtyId: form.specialtyId, name: form.name, description: form.description || null, durationMinutes: Number(form.durationMinutes), price: form.price ? Number(form.price) : null }) }), onSuccess: onSaved, onError: (requestError) => setError(getErrorMessage(requestError)) });
  return <Dialog open={open} onOpenChange={onOpenChange} title={item ? "Editar serviço" : "Novo serviço"} description="A duração define automaticamente o horário final dos agendamentos." footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="service-form" isLoading={mutation.isPending}>Salvar serviço</Button></>}>
    <form id="service-form" className="form-grid" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
      {error ? <div className="form-grid__full"><FeedbackMessage type="error">{error}</FeedbackMessage></div> : null}
      <div className="form-grid__full"><Field label="Nome" htmlFor="service-name" required><Input id="service-name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required /></Field></div>
      <Field label="Especialidade" htmlFor="service-specialty" required><Select id="service-specialty" value={form.specialtyId} onChange={(event) => setForm({ ...form, specialtyId: event.target.value })} required><option value="">Selecione</option>{specialties.map((specialty) => <option key={specialty.id} value={specialty.id}>{specialty.name}</option>)}</Select></Field>
      <Field label="Duração (minutos)" htmlFor="service-duration" required><Input id="service-duration" type="number" min={5} max={480} step={5} value={form.durationMinutes} onChange={(event) => setForm({ ...form, durationMinutes: event.target.value })} required /></Field>
      <Field label="Valor" htmlFor="service-price" hint="Opcional"><Input id="service-price" type="number" min={0} step="0.01" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} /></Field>
      <div className="form-grid__full"><Field label="Descrição" htmlFor="service-description" hint="Opcional"><Textarea id="service-description" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} maxLength={500} rows={3} /></Field></div>
    </form>
  </Dialog>;
}
