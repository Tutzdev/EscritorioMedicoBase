import type { AppointmentStatus } from "../../types/api";
import { statusLabels } from "../../utils/format";

export function StatusBadge({ status }: { status: AppointmentStatus }) {
  return (
    <span className={`status status--${status.toLowerCase()}`}>
      <span className="status__dot" aria-hidden="true" />
      {statusLabels[status]}
    </span>
  );
}

export function ActiveBadge({ active }: { active: boolean }) {
  return (
    <span className={`status ${active ? "status--active" : "status--inactive"}`}>
      <span className="status__dot" aria-hidden="true" />
      {active ? "Ativo" : "Inativo"}
    </span>
  );
}
