import { AlertCircle, Inbox, RefreshCw } from "lucide-react";
import type { ReactNode } from "react";
import { Button } from "./Button";

export function LoadingState({ rows = 5 }: { rows?: number }) {
  return (
    <div className="loading-list" aria-label="Carregando" aria-busy="true">
      {Array.from({ length: rows }, (_, index) => (
        <div className="skeleton-row" key={index} />
      ))}
    </div>
  );
}

interface StateProps {
  title: string;
  description: string;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: StateProps) {
  return (
    <div className="data-state">
      <Inbox size={24} aria-hidden="true" />
      <h2>{title}</h2>
      <p>{description}</p>
      {action}
    </div>
  );
}

export function ErrorState({
  title = "Não foi possível carregar",
  description,
  onRetry,
}: {
  title?: string;
  description: string;
  onRetry: () => void;
}) {
  return (
    <div className="data-state data-state--error" role="alert">
      <AlertCircle size={24} aria-hidden="true" />
      <h2>{title}</h2>
      <p>{description}</p>
      <Button variant="secondary" icon={<RefreshCw size={16} />} onClick={onRetry}>
        Tentar novamente
      </Button>
    </div>
  );
}
