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
import type { PageResponse, Specialty } from "../types/api";
import { getErrorMessage } from "../utils/errors";

export function SpecialtiesPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [activeFilter, setActiveFilter] = useState("");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Specialty | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  const query = useQuery({
    queryKey: ["specialties", deferredSearch, activeFilter, page],
    queryFn: () =>
      apiRequest<PageResponse<Specialty>>(
        `/specialties${toQueryString({
          search: deferredSearch,
          active: activeFilter || undefined,
          page,
          size: 20,
          sort: "name,asc",
        })}`,
      ),
  });

  const statusMutation = useMutation({
    mutationFn: (specialty: Specialty) =>
      apiRequest<Specialty>(`/specialties/${specialty.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ active: !specialty.active }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["specialties"] }),
  });

  const openDialog = (specialty: Specialty | null) => {
    setEditing(specialty);
    setDialogOpen(true);
  };

  const columns: DataColumn<Specialty>[] = [
    {
      key: "name",
      header: "Especialidade",
      render: (item) => <strong>{item.name}</strong>,
    },
    {
      key: "description",
      header: "Descrição",
      render: (item) => item.description || <span className="muted-text">Não informada</span>,
    },
    { key: "status", header: "Status", render: (item) => <ActiveBadge active={item.active} /> },
    {
      key: "actions",
      header: "Ações",
      align: "right",
      render: (item) => (
        <ResourceActions
          active={item.active}
          canManage
          onEdit={() => openDialog(item)}
          onToggleStatus={() => statusMutation.mutate(item)}
        />
      ),
    },
  ];

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Estrutura da clínica"
        title="Especialidades"
        description="Organize profissionais e serviços por área de atuação."
        action={<Button icon={<Plus size={17} />} onClick={() => openDialog(null)}>Nova especialidade</Button>}
      />
      <section className="resource-panel">
        <ResourceToolbar search={search} searchLabel="Buscar especialidade" onSearchChange={(value) => { setSearch(value); setPage(0); }}>
          <Select aria-label="Filtrar por status" value={activeFilter} onChange={(event) => { setActiveFilter(event.target.value); setPage(0); }}>
            <option value="">Todos os status</option>
            <option value="true">Ativas</option>
            <option value="false">Inativas</option>
          </Select>
        </ResourceToolbar>
        {query.isLoading ? <LoadingState /> : null}
        {query.isError ? <ErrorState description="Não foi possível carregar as especialidades." onRetry={() => void query.refetch()} /> : null}
        {query.data?.content.length === 0 ? <EmptyState title="Nenhuma especialidade encontrada" description="Cadastre as áreas atendidas pela clínica." action={<Button onClick={() => openDialog(null)}>Cadastrar especialidade</Button>} /> : null}
        {query.data?.content.length ? (
          <>
            <DataCollection items={query.data.content} columns={columns} getKey={(item) => item.id} caption="Especialidades cadastradas" renderMobile={(item) => (
              <>
                <div className="mobile-record__header"><strong>{item.name}</strong><ActiveBadge active={item.active} /></div>
                <p>{item.description || "Sem descrição."}</p>
                <ResourceActions active={item.active} canManage onEdit={() => openDialog(item)} onToggleStatus={() => statusMutation.mutate(item)} />
              </>
            )} />
            <Pagination page={query.data.page} totalPages={query.data.totalPages} totalElements={query.data.totalElements} onPageChange={setPage} />
          </>
        ) : null}
      </section>
      {dialogOpen ? <SpecialtyDialog specialty={editing} open onOpenChange={setDialogOpen} onSaved={() => { setDialogOpen(false); void queryClient.invalidateQueries({ queryKey: ["specialties"] }); }} /> : null}
    </div>
  );
}

function SpecialtyDialog({ specialty, open, onOpenChange, onSaved }: { specialty: Specialty | null; open: boolean; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [name, setName] = useState(specialty?.name ?? "");
  const [description, setDescription] = useState(specialty?.description ?? "");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () => apiRequest<Specialty>(specialty ? `/specialties/${specialty.id}` : "/specialties", { method: specialty ? "PUT" : "POST", body: JSON.stringify({ name, description: description || null }) }),
    onSuccess: onSaved,
    onError: (requestError) => setError(getErrorMessage(requestError)),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange} title={specialty ? "Editar especialidade" : "Nova especialidade"} description="Use um nome reconhecido pela equipe e pelos pacientes." footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="specialty-form" isLoading={mutation.isPending}>Salvar especialidade</Button></>}>
      <form id="specialty-form" className="form-stack" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
        {error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}
        <Field label="Nome" htmlFor="specialty-name" required><Input id="specialty-name" value={name} onChange={(event) => setName(event.target.value)} maxLength={120} required /></Field>
        <Field label="Descrição" htmlFor="specialty-description" hint="Opcional"><Textarea id="specialty-description" value={description} onChange={(event) => setDescription(event.target.value)} maxLength={500} rows={4} /></Field>
      </form>
    </Dialog>
  );
}
