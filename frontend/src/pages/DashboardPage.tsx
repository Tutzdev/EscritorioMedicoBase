import { useQuery } from "@tanstack/react-query";
import {
  ArrowRight,
  CalendarClock,
  CheckCircle2,
  Clock3,
  UsersRound,
} from "lucide-react";
import { Link } from "react-router-dom";
import { apiRequest } from "../api/client";
import { EmptyState, ErrorState, LoadingState } from "../components/ui/DataState";
import { PageHeader } from "../components/ui/PageHeader";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { Dashboard } from "../types/api";
import { formatDateTime, formatTime } from "../utils/format";

export function DashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ["dashboard"],
    queryFn: () => apiRequest<Dashboard>("/dashboard"),
    refetchInterval: 60_000,
  });

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Hoje na clínica"
        title="Visão do dia"
        description="Acompanhe o fluxo dos próximos atendimentos e o que precisa de atenção."
        action={
          <Link className="button button--primary button--md" to="/agenda">
            Abrir agenda <ArrowRight size={17} />
          </Link>
        }
      />

      {dashboardQuery.isLoading ? <LoadingState rows={4} /> : null}
      {dashboardQuery.isError ? (
        <ErrorState
          description="A visão do dia não respondeu. Tente atualizar os dados."
          onRetry={() => void dashboardQuery.refetch()}
        />
      ) : null}
      {dashboardQuery.data ? <DashboardContent data={dashboardQuery.data} /> : null}
    </div>
  );
}

function DashboardContent({ data }: { data: Dashboard }) {
  const metrics = [
    {
      label: "Agenda de hoje",
      value: data.appointmentsToday,
      detail: "atendimentos válidos",
      icon: CalendarClock,
    },
    {
      label: "Aguardando conclusão",
      value: data.pendingToday,
      detail: "na rotina de hoje",
      icon: Clock3,
    },
    {
      label: "Concluídos",
      value: data.completedToday,
      detail: "registros finalizados",
      icon: CheckCircle2,
    },
    {
      label: "Pacientes ativos",
      value: data.activePatients,
      detail: "na base da clínica",
      icon: UsersRound,
    },
  ];

  return (
    <>
      <section className="metric-row" aria-label="Resumo da rotina">
        {metrics.map((metric) => {
          const Icon = metric.icon;
          return (
            <article className="metric" key={metric.label}>
              <Icon size={18} aria-hidden="true" />
              <span>{metric.label}</span>
              <strong>{metric.value}</strong>
              <small>{metric.detail}</small>
            </article>
          );
        })}
      </section>

      <section className="agenda-panel" aria-labelledby="next-appointments-title">
        <header className="section-header">
          <div>
            <p className="eyebrow">Linha clínica</p>
            <h2 id="next-appointments-title">Próximos atendimentos</h2>
          </div>
          <span>{formatDateTime(`${data.referenceDate}T12:00:00`)}</span>
        </header>

        {data.nextAppointments.length === 0 ? (
          <EmptyState
            title="Nenhum atendimento a seguir"
            description="A agenda não possui próximos horários confirmados ou agendados."
            action={<Link to="/agenda">Ver agenda completa</Link>}
          />
        ) : (
          <ol className="dayline">
            {data.nextAppointments.map((appointment) => (
              <li key={appointment.id}>
                <time>{formatTime(appointment.scheduledStart)}</time>
                <span className="dayline__marker" aria-hidden="true" />
                <div className="dayline__content">
                  <strong>{appointment.patientName}</strong>
                  <span>
                    {appointment.serviceName} · {appointment.professionalName}
                  </span>
                </div>
                <StatusBadge status={appointment.status} />
              </li>
            ))}
          </ol>
        )}
      </section>
    </>
  );
}
