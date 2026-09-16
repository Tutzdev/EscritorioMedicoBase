import { ArrowRight, CalendarCheck2, ShieldCheck } from "lucide-react";
import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { Button } from "../components/ui/Button";
import { FeedbackMessage } from "../components/ui/FeedbackMessage";
import { Field, Input } from "../components/ui/FormField";
import { useClinicBrand } from "../hooks/useClinicBrand";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const brandQuery = useClinicBrand();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await login(email, password);
      navigate("/", { replace: true });
    } catch (requestError) {
      setError(
        requestError instanceof ApiError
          ? requestError.message
          : "Não foi possível entrar. Verifique a conexão e tente novamente.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const clinicName = brandQuery.data?.displayName ?? "Clínica Modelo";

  return (
    <main className="login-page">
      <section className="login-brand" aria-labelledby="login-brand-title">
        <div className="login-brand__top">
          <span className="brand-mark brand-mark--light" aria-hidden="true">CM</span>
          <span>{clinicName}</span>
        </div>
        <div className="login-brand__message">
          <p className="eyebrow">Rotina organizada</p>
          <h1 id="login-brand-title">O cuidado começa antes da consulta.</h1>
          <p>
            Agenda, pacientes e atendimentos em uma rotina única para a equipe.
          </p>
        </div>
        <div className="login-dayline" aria-label="Exemplo da agenda do dia">
          <div><time>08:30</time><span>Primeiro atendimento</span></div>
          <div><time>10:00</time><span>Retorno confirmado</span></div>
          <div><time>14:30</time><span>Horário disponível</span></div>
        </div>
        <div className="login-brand__trust">
          <span><ShieldCheck size={17} /> Acesso protegido</span>
          <span><CalendarCheck2 size={17} /> Agenda consistente</span>
        </div>
      </section>

      <section className="login-form-section" aria-labelledby="login-title">
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="login-form__heading">
            <p className="eyebrow">Acesso da equipe</p>
            <h2 id="login-title">Entre na sua rotina</h2>
            <p>Use o e-mail e a senha cadastrados pelo administrador.</p>
          </div>

          {error ? <FeedbackMessage type="error">{error}</FeedbackMessage> : null}

          <Field label="E-mail" htmlFor="email" required>
            <Input
              id="email"
              type="email"
              autoComplete="username"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </Field>

          <Field label="Senha" htmlFor="password" required>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              minLength={8}
              required
            />
          </Field>

          <Button
            type="submit"
            isLoading={isSubmitting}
            disabled={!email || !password}
            icon={<ArrowRight size={17} />}
          >
            Entrar
          </Button>
        </form>
      </section>
    </main>
  );
}
