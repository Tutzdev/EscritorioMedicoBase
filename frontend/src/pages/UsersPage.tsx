import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { KeyRound, Plus } from "lucide-react";
import { useDeferredValue, useState, type FormEvent } from "react";
import { apiRequest, toQueryString } from "../api/client";
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
import type { PageResponse, Role, UserAccount } from "../types/api";
import { getErrorMessage } from "../utils/errors";
import { roleLabels } from "../utils/format";

const roles = Object.keys(roleLabels) as Role[];

export function UsersPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [role, setRole] = useState("");
  const [active, setActive] = useState("");
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState<UserAccount | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [passwordUser, setPasswordUser] = useState<UserAccount | null>(null);
  const query = useQuery({ queryKey: ["users", deferredSearch, role, active, page], queryFn: () => apiRequest<PageResponse<UserAccount>>(`/users${toQueryString({ search: deferredSearch, role: role || undefined, active: active || undefined, page, size: 20, sort: "name,asc" })}`) });
  const statusMutation = useMutation({ mutationFn: (item: UserAccount) => apiRequest<UserAccount>(`/users/${item.id}/status`, { method: "PATCH", body: JSON.stringify({ active: !item.active }) }), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }) });
  const openEditor = (item: UserAccount | null) => { setEditing(item); setDialogOpen(true); };
  const columns: DataColumn<UserAccount>[] = [
    { key: "user", header: "Usuário", render: (item) => <div className="primary-cell"><strong>{item.name}</strong><span>{item.email}</span></div> },
    { key: "role", header: "Perfil", render: (item) => roleLabels[item.role] },
    { key: "status", header: "Status", render: (item) => <ActiveBadge active={item.active} /> },
    { key: "password", header: "Senha", render: (item) => <Button variant="ghost" size="sm" icon={<KeyRound size={15} />} onClick={() => setPasswordUser(item)}>Redefinir</Button> },
    { key: "actions", header: "Ações", align: "right", render: (item) => <ResourceActions active={item.active} canManage onEdit={() => openEditor(item)} onToggleStatus={() => statusMutation.mutate(item)} /> },
  ];
  return <div className="page-stack">
    <PageHeader eyebrow="Acesso e permissões" title="Usuários" description="Controle quem acessa a operação e o nível de permissão de cada pessoa." action={<Button icon={<Plus size={17} />} onClick={() => openEditor(null)}>Novo usuário</Button>} />
    <section className="resource-panel" aria-label="Usuários do sistema">
      <ResourceToolbar search={search} searchLabel="Buscar por nome ou e-mail" onSearchChange={(value) => { setSearch(value); setPage(0); }}><Select aria-label="Filtrar por perfil" value={role} onChange={(event) => { setRole(event.target.value); setPage(0); }}><option value="">Todos os perfis</option>{roles.map((item) => <option key={item} value={item}>{roleLabels[item]}</option>)}</Select><Select aria-label="Filtrar por status" value={active} onChange={(event) => { setActive(event.target.value); setPage(0); }}><option value="">Todos os status</option><option value="true">Ativos</option><option value="false">Inativos</option></Select></ResourceToolbar>
      {query.isLoading ? <LoadingState /> : null}
      {query.isError ? <ErrorState description="Não foi possível carregar os usuários." onRetry={() => void query.refetch()} /> : null}
      {query.data?.content.length === 0 ? <EmptyState title="Nenhum usuário encontrado" description="Cadastre um acesso ou ajuste os filtros." action={<Button onClick={() => openEditor(null)}>Cadastrar usuário</Button>} /> : null}
      {query.data?.content.length ? <><DataCollection items={query.data.content} columns={columns} getKey={(item) => item.id} caption="Usuários cadastrados" renderMobile={(item) => <><div className="mobile-record__header"><div className="primary-cell"><strong>{item.name}</strong><span>{item.email}</span></div><ActiveBadge active={item.active} /></div><p>{roleLabels[item.role]}</p><div className="mobile-record__actions"><Button variant="secondary" size="sm" icon={<KeyRound size={15} />} onClick={() => setPasswordUser(item)}>Senha</Button><ResourceActions active={item.active} canManage onEdit={() => openEditor(item)} onToggleStatus={() => statusMutation.mutate(item)} /></div></>} /><Pagination page={query.data.page} totalPages={query.data.totalPages} totalElements={query.data.totalElements} onPageChange={setPage} /></> : null}
    </section>
    {dialogOpen ? <UserDialog user={editing} open onOpenChange={setDialogOpen} onSaved={() => { setDialogOpen(false); void queryClient.invalidateQueries({ queryKey: ["users"] }); }} /> : null}
    {passwordUser ? <PasswordDialog user={passwordUser} onOpenChange={(open) => { if (!open) setPasswordUser(null); }} /> : null}
  </div>;
}

function UserDialog({ user, open, onOpenChange, onSaved }: { user: UserAccount | null; open: boolean; onOpenChange: (open: boolean) => void; onSaved: () => void }) {
  const [form, setForm] = useState(() => user ? { name: user.name, email: user.email, password: "", role: user.role } : { name: "", email: "", password: "", role: "RECEPTIONIST" as Role });
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({ mutationFn: () => apiRequest<UserAccount>(user ? `/users/${user.id}` : "/users", { method: user ? "PUT" : "POST", body: JSON.stringify(user ? { name: form.name, role: form.role } : form) }), onSuccess: onSaved, onError: (requestError) => setError(getErrorMessage(requestError)) });
  return <Dialog open={open} onOpenChange={onOpenChange} title={user ? "Editar usuário" : "Novo usuário"} description="As permissões são aplicadas no servidor de acordo com o perfil." footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Cancelar</Button><Button type="submit" form="user-form" isLoading={mutation.isPending}>Salvar usuário</Button></>}>
    <form id="user-form" className="form-stack" onSubmit={(event: FormEvent) => { event.preventDefault(); mutation.mutate(); }}>
      {error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}
      <Field label="Nome" htmlFor="user-name" required><Input id="user-name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} minLength={3} maxLength={120} required /></Field>
      <Field label="E-mail" htmlFor="user-email" required><Input id="user-email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} disabled={Boolean(user)} maxLength={160} required /></Field>
      {!user ? <Field label="Senha inicial" htmlFor="user-password" hint="Entre 8 e 72 caracteres" required><Input id="user-password" type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} minLength={8} maxLength={72} autoComplete="new-password" required /></Field> : null}
      <Field label="Perfil" htmlFor="user-role" required><Select id="user-role" value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value as Role })}>{roles.map((item) => <option key={item} value={item}>{roleLabels[item]}</option>)}</Select></Field>
    </form>
  </Dialog>;
}

function PasswordDialog({ user, onOpenChange }: { user: UserAccount; onOpenChange: (open: boolean) => void }) {
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const mutation = useMutation({ mutationFn: () => apiRequest<void>(`/users/${user.id}/password`, { method: "PATCH", body: JSON.stringify({ newPassword: password }) }), onSuccess: () => { setSaved(true); setPassword(""); }, onError: (requestError) => setError(getErrorMessage(requestError)) });
  return <Dialog open onOpenChange={onOpenChange} title="Redefinir senha" description={`Defina uma nova senha para ${user.name}.`} footer={<><Button variant="secondary" onClick={() => onOpenChange(false)}>Fechar</Button><Button type="submit" form="password-form" isLoading={mutation.isPending}>Atualizar senha</Button></>}><form id="password-form" className="form-stack" onSubmit={(event) => { event.preventDefault(); mutation.mutate(); }}>{error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}{saved ? <FeedbackMessage type="success">Senha atualizada com segurança.</FeedbackMessage> : null}<Field label="Nova senha" htmlFor="new-password" hint="Entre 8 e 72 caracteres" required><Input id="new-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} minLength={8} maxLength={72} autoComplete="new-password" required /></Field></form></Dialog>;
}
