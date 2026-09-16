import { Search } from "lucide-react";
import type { ReactNode } from "react";
import { Input } from "./FormField";

interface ResourceToolbarProps {
  search: string;
  onSearchChange: (value: string) => void;
  searchLabel: string;
  children?: ReactNode;
}

export function ResourceToolbar({
  search,
  onSearchChange,
  searchLabel,
  children,
}: ResourceToolbarProps) {
  return (
    <div className="resource-toolbar">
      <label className="search-field">
        <span className="sr-only">{searchLabel}</span>
        <Search size={17} aria-hidden="true" />
        <Input
          type="search"
          value={search}
          placeholder={searchLabel}
          onChange={(event) => onSearchChange(event.target.value)}
        />
      </label>
      {children ? <div className="resource-toolbar__filters">{children}</div> : null}
    </div>
  );
}
