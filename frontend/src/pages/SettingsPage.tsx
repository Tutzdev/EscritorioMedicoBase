import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Building2, Palette, Save } from "lucide-react";
import { useState, type FormEvent } from "react";
import { apiRequest } from "../api/client";
import { Button } from "../components/ui/Button";
import { ErrorState, LoadingState } from "../components/ui/DataState";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Input, Select, Textarea } from "../components/ui/FormField";
import { PageHeader } from "../components/ui/PageHeader";
import type { ClinicSettings } from "../types/api";
import { getErrorMessage } from "../utils/errors";

type SettingsForm = Omit<ClinicSettings, "id" | "updatedAt">;

export function SettingsPage() {
  const query = useQuery({ queryKey: ["clinic-settings"], queryFn: () => apiRequest<ClinicSettings>("/clinic-settings") });

  if (query.isLoading) return <LoadingState rows={6} />;
  if (query.isError) return <ErrorState description="Não foi possível carregar as configurações da clínica." onRetry={() => void query.refetch()} />;
  if (!query.data) return null;

  return <SettingsEditor key={query.data.id} settings={query.data} />;
}

function SettingsEditor({ settings }: { settings: ClinicSettings }) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<SettingsForm>(() => ({
    displayName: settings.displayName,
    legalName: settings.legalName,
    document: settings.document,
    phone: settings.phone,
    email: settings.email,
    address: settings.address,
    timeZone: settings.timeZone,
    primaryColor: settings.primaryColor,
    logoUrl: settings.logoUrl,
  }));
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const mutation = useMutation({
    mutationFn: () => apiRequest<ClinicSettings>("/clinic-settings", { method: "PUT", body: JSON.stringify(form) }),
    onSuccess: (updatedSettings) => { setForm({ displayName: updatedSettings.displayName, legalName: updatedSettings.legalName, document: updatedSettings.document, phone: updatedSettings.phone, email: updatedSettings.email, address: updatedSettings.address, timeZone: updatedSettings.timeZone, primaryColor: updatedSettings.primaryColor, logoUrl: updatedSettings.logoUrl }); setError(null); setSaved(true); void queryClient.invalidateQueries({ queryKey: ["clinic-brand"] }); },
    onError: (requestError) => { setSaved(false); setError(getErrorMessage(requestError)); },
  });

  const update = (field: keyof SettingsForm, value: string) => setForm((current) => ({ ...current, [field]: value || null }));
  return <div className="page-stack">
    <PageHeader eyebrow="Identidade compartilhada" title="Configurações da clínica" description="Esses dados identificam a instalação e personalizam a navegação para cada cliente." />
    <form className="settings-layout" onSubmit={(event: FormEvent) => { event.preventDefault(); setSaved(false); mutation.mutate(); }}>
      <section className="settings-section" aria-labelledby="clinic-data-title"><header><Building2 size={19} /><div><h2 id="clinic-data-title">Dados institucionais</h2><p>Informações gerais usadas pela operação.</p></div></header><div className="form-grid">
        <div className="form-grid__full"><Field label="Nome de exibição" htmlFor="settings-display-name" required><Input id="settings-display-name" value={form.displayName} onChange={(event) => setForm({ ...form, displayName: event.target.value })} maxLength={120} required /></Field></div>
        <Field label="Razão social" htmlFor="settings-legal-name" hint="Opcional"><Input id="settings-legal-name" value={form.legalName ?? ""} onChange={(event) => update("legalName", event.target.value)} maxLength={160} /></Field>
        <Field label="CNPJ ou documento" htmlFor="settings-document" hint="Opcional"><Input id="settings-document" value={form.document ?? ""} onChange={(event) => update("document", event.target.value)} maxLength={20} /></Field>
        <Field label="Telefone" htmlFor="settings-phone" hint="Opcional"><Input id="settings-phone" value={form.phone ?? ""} onChange={(event) => update("phone", event.target.value)} maxLength={20} /></Field>
        <Field label="E-mail" htmlFor="settings-email" hint="Opcional"><Input id="settings-email" type="email" value={form.email ?? ""} onChange={(event) => update("email", event.target.value)} maxLength={160} /></Field>
        <div className="form-grid__full"><Field label="Endereço" htmlFor="settings-address" hint="Opcional"><Textarea id="settings-address" value={form.address ?? ""} onChange={(event) => update("address", event.target.value)} maxLength={300} rows={3} /></Field></div>
        <Field label="Fuso horário" htmlFor="settings-time-zone" required><Select id="settings-time-zone" value={form.timeZone} onChange={(event) => setForm({ ...form, timeZone: event.target.value })}><option value="America/Sao_Paulo">Brasília · São Paulo</option><option value="America/Manaus">Manaus</option><option value="America/Cuiaba">Cuiabá</option><option value="America/Rio_Branco">Rio Branco</option><option value="America/Noronha">Fernando de Noronha</option></Select></Field>
      </div></section>
      <section className="settings-section" aria-labelledby="clinic-brand-title"><header><Palette size={19} /><div><h2 id="clinic-brand-title">Marca da instalação</h2><p>A cor e a assinatura visual aparecem no acesso e na navegação.</p></div></header><div className="brand-editor">
        <div className="brand-preview" style={{ borderTopColor: form.primaryColor }}><span className="brand-mark" style={{ backgroundColor: form.primaryColor }}>{initials(form.displayName)}</span><div>{form.logoUrl ? <img src={form.logoUrl} alt="Prévia do logotipo" /> : null}<strong>{form.displayName || "Nome da clínica"}</strong><span>Gestão clínica</span></div></div>
        <Field label="Cor principal" htmlFor="settings-color" required><div className="color-field"><Input id="settings-color-picker" type="color" value={form.primaryColor} onChange={(event) => setForm({ ...form, primaryColor: event.target.value })} aria-label="Selecionar cor principal" /><Input id="settings-color" value={form.primaryColor} pattern="#[0-9A-Fa-f]{6}" onChange={(event) => setForm({ ...form, primaryColor: event.target.value })} required /></div></Field>
        <Field label="URL do logotipo" htmlFor="settings-logo" hint="Opcional · use uma URL HTTPS"><Input id="settings-logo" type="url" value={form.logoUrl ?? ""} onChange={(event) => update("logoUrl", event.target.value)} maxLength={500} /></Field>
      </div></section>
      <div className="settings-actions">{error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}{saved ? <FeedbackMessage type="success">Configurações salvas.</FeedbackMessage> : null}<Button type="submit" icon={<Save size={16} />} isLoading={mutation.isPending}>Salvar configurações</Button></div>
    </form>
  </div>;
}

function initials(value: string): string {
  return value.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join("") || "CM";
}
