import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "./Button";

interface PaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
}

export function Pagination({
  page,
  totalPages,
  totalElements,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) {
    return totalElements ? (
      <p className="pagination__summary">{totalElements} registro(s)</p>
    ) : null;
  }

  return (
    <nav className="pagination" aria-label="Paginar resultados">
      <p>
        Página {page + 1} de {totalPages} · {totalElements} registros
      </p>
      <div>
        <Button
          variant="secondary"
          size="sm"
          aria-label="Página anterior"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          icon={<ChevronLeft size={16} />}
        >
          Anterior
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Próxima
          <ChevronRight size={16} />
        </Button>
      </div>
    </nav>
  );
}
