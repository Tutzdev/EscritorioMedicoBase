import { Pencil, Power } from "lucide-react";

export function ResourceActions({
  onEdit,
  onToggleStatus,
  active,
  canManage,
}: {
  onEdit: () => void;
  onToggleStatus: () => void;
  active: boolean;
  canManage: boolean;
}) {
  if (!canManage) {
    return <span className="muted-text">Somente leitura</span>;
  }

  return (
    <div className="row-actions">
      <button className="icon-button" onClick={onEdit} aria-label="Editar">
        <Pencil size={16} />
      </button>
      <button
        className="icon-button"
        onClick={onToggleStatus}
        aria-label={active ? "Inativar" : "Ativar"}
      >
        <Power size={16} />
      </button>
    </div>
  );
}
