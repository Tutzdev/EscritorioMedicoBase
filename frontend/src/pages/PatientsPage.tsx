import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import {
  useDeferredValue,
  useState,
  type FormEvent,
} from "react";
import { apiRequest, toQueryString } from "../api/client";
import { useAuth } from "../auth/AuthContext";
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
import { Button } from "../components/ui/Button";
import type { PageResponse, Patient } from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { formatCpf, formatDate } from "../utils/format";

interface PatientFormValue {
  name: string;
  cpf: string;
  birthDate: string;
  phone: string;
  email: string;
}

const emptyPatient: PatientFormValue = {
  name: "",
  cpf: "",
  birthDate: "",
  phone: "",
  email: "",
};

export function PatientsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [activeFilter, setActiveFilter] = useState("true");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<Patient | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const canManage = user?.role === "ADMIN" || user?.role === "RECEPTIONIST";

  const patientsQuery = useQuery({
    queryKey: ["patients", deferredSearch, activeFilter, page],
    queryFn: () =>
      apiRequest<PageResponse<Patient>>(
        `/patients${toQueryString({
          search: deferredSearch,
          active: activeFilter || undefined,
          page,
          size: 20,
          sort: "name,asc",
        })}`,
      ),
  });

  const statusMutation = useMutation({
    mutationFn: (patient: Patient) =>
      apiRequest<Patient>(`/patients/${patient.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ active: !patient.active }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["patients"] }),
  });

  const openCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const openEdit = (patient: Patient) => {
    setEditing(patient);
    setDialogOpen(true);
  };

  const columns: DataColumn<Patient>[] = [
    {
      key: "patient",
      header: "Paciente",
      render: (patient) => (
        <div className="primary-cell">
          <strong>{patient.name}</strong>
          <span>{patient.email || "E-mail não informado"}</span>
        </div>
      ),
    },
    { key: "cpf", header: "CPF", render: (patient) => formatCpf(patient.cpf) },
    {
      key: "birthDate",
      header: "Nascimento",
      render: (patient) => formatDate(patient.birthDate),
    },
    { key: "phone", header: "Telefone", render: (patient) => patient.phone },
    {
      key: "status",
      header: "Status",
      render: (patient) => <ActiveBadge active={patient.active} />,
    },
    {
      key: "actions",
      header: "Ações",
      align: "right",
      render: (patient) => (
        <ResourceActions
          active={patient.active}
          canManage={canManage}
          onEdit={() => openEdit(patient)}
          onToggleStatus={() => statusMutation.mutate(patient)}
        />
      ),
    },
  ];

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Cadastro compartilhado"
        title="Pacientes"
        description="Dados de contato e identificação usados em todos os fluxos da clínica."
        action={
          canManage ? (
            <Button icon={<Plus size={17} />} onClick={openCreate}>
              Novo paciente
            </Button>
          ) : undefined
        }
      />

      <section className="resource-panel" aria-label="Lista de pacientes">
        <ResourceToolbar
          search={search}
          searchLabel="Buscar por nome ou CPF"
          onSearchChange={(value) => {
            setSearch(value);
            setPage(0);
          }}
        >
          <Select
            aria-label="Filtrar por status"
            value={activeFilter}
            onChange={(event) => {
              setActiveFilter(event.target.value);
              setPage(0);
            }}
          >
            <option value="">Todos os status</option>
            <option value="true">Ativos</option>
            <option value="false">Inativos</option>
          </Select>
        </ResourceToolbar>

        {patientsQuery.isLoading ? <LoadingState /> : null}
        {patientsQuery.isError ? (
          <ErrorState
            description="A lista de pacientes não respondeu."
            onRetry={() => void patientsQuery.refetch()}
          />
        ) : null}
        {patientsQuery.data?.content.length === 0 ? (
          <EmptyState
            title="Nenhum paciente encontrado"
            description={search ? "Tente ajustar a busca ou os filtros." : "Cadastre o primeiro paciente para iniciar a agenda."}
            action={canManage && !search ? <Button onClick={openCreate}>Cadastrar paciente</Button> : undefined}
          />
        ) : null}
        {patientsQuery.data?.content.length ? (
          <>
            <DataCollection
              items={patientsQuery.data.content}
              columns={columns}
              getKey={(patient) => patient.id}
              caption="Pacientes cadastrados"
              renderMobile={(patient) => (
                <>
                  <div className="mobile-record__header">
                    <div className="primary-cell">
                      <strong>{patient.name}</strong>
                      <span>{formatCpf(patient.cpf)}</span>
                    </div>
                    <ActiveBadge active={patient.active} />
                  </div>
                  <dl>
                    <div><dt>Nascimento</dt><dd>{formatDate(patient.birthDate)}</dd></div>
                    <div><dt>Telefone</dt><dd>{patient.phone}</dd></div>
                  </dl>
                  <ResourceActions
                    active={patient.active}
                    canManage={canManage}
                    onEdit={() => openEdit(patient)}
                    onToggleStatus={() => statusMutation.mutate(patient)}
                  />
                </>
              )}
            />
            <Pagination
              page={patientsQuery.data.page}
              totalPages={patientsQuery.data.totalPages}
              totalElements={patientsQuery.data.totalElements}
              onPageChange={setPage}
            />
          </>
        ) : null}
      </section>

      {dialogOpen ? (
        <PatientDialog
          patient={editing}
          open
          onOpenChange={setDialogOpen}
          onSaved={() => {
            setDialogOpen(false);
            void queryClient.invalidateQueries({ queryKey: ["patients"] });
          }}
        />
      ) : null}
    </div>
  );
}

function PatientDialog({
  patient,
  open,
  onOpenChange,
  onSaved,
}: {
  patient: Patient | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSaved: () => void;
}) {
  const [form, setForm] = useState<PatientFormValue>(() =>
    patient
      ? {
          name: patient.name,
          cpf: formatCpf(patient.cpf),
          birthDate: patient.birthDate,
          phone: patient.phone,
          email: patient.email ?? "",
        }
      : emptyPatient,
  );
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (value: PatientFormValue) =>
      apiRequest<Patient>(patient ? `/patients/${patient.id}` : "/patients", {
        method: patient ? "PUT" : "POST",
        body: JSON.stringify({ ...value, email: value.email || null }),
      }),
    onSuccess: onSaved,
    onError: (requestError) => setError(getErrorMessage(requestError)),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    mutation.mutate({ ...form, cpf: form.cpf.replace(/\D/g, "") });
  };

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title={patient ? "Editar paciente" : "Novo paciente"}
      description="Mantenha os dados que a equipe usa para identificar e contatar o paciente."
      footer={
        <>
          <Button variant="secondary" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="submit" form="patient-form" isLoading={mutation.isPending}>
            Salvar paciente
          </Button>
        </>
      }
    >
      <form id="patient-form" className="form-grid" onSubmit={handleSubmit}>
        {error ? <div className="form-grid__full"><FeedbackMessage type="error">{error}</FeedbackMessage></div> : null}
        <div className="form-grid__full">
          <Field label="Nome completo" htmlFor="patient-name" required>
            <Input id="patient-name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} maxLength={120} required />
          </Field>
        </div>
        <Field label="CPF" htmlFor="patient-cpf" required>
          <Input id="patient-cpf" value={form.cpf} onChange={(event) => setForm({ ...form, cpf: formatCpf(event.target.value) })} inputMode="numeric" maxLength={14} required />
        </Field>
        <Field label="Data de nascimento" htmlFor="patient-birth" required>
          <Input id="patient-birth" type="date" value={form.birthDate} onChange={(event) => setForm({ ...form, birthDate: event.target.value })} required />
        </Field>
        <Field label="Telefone" htmlFor="patient-phone" required>
          <Input id="patient-phone" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} maxLength={20} required />
        </Field>
        <Field label="E-mail" htmlFor="patient-email" hint="Opcional">
          <Input id="patient-email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} maxLength={160} />
        </Field>
      </form>
    </Dialog>
  );
}
