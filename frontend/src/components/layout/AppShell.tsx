import * as RadixDialog from "@radix-ui/react-dialog";
import {
  BriefcaseMedical,
  CalendarDays,
  ClipboardPlus,
  Gauge,
  LogOut,
  Menu,
  Settings,
  Stethoscope,
  Tag,
  Users,
  UsersRound,
  X,
} from "lucide-react";
import { useState, type CSSProperties } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { useClinicBrand } from "../../hooks/useClinicBrand";
import type { Role } from "../../types/api";
import { roleLabels } from "../../utils/format";

interface NavigationItem {
  to: string;
  label: string;
  icon: typeof Gauge;
  roles?: Role[];
}

const navigation: NavigationItem[] = [
  { to: "/", label: "Visão do dia", icon: Gauge },
  { to: "/agenda", label: "Agenda", icon: CalendarDays },
  { to: "/pacientes", label: "Pacientes", icon: UsersRound },
  { to: "/profissionais", label: "Profissionais", icon: Stethoscope },
  { to: "/atendimentos", label: "Atendimentos", icon: ClipboardPlus },
  {
    to: "/especialidades",
    label: "Especialidades",
    icon: BriefcaseMedical,
    roles: ["ADMIN"],
  },
  { to: "/servicos", label: "Serviços", icon: Tag, roles: ["ADMIN"] },
  { to: "/usuarios", label: "Usuários", icon: Users, roles: ["ADMIN"] },
  {
    to: "/configuracoes",
    label: "Configurações",
    icon: Settings,
    roles: ["ADMIN"],
  },
];

const pageTitles: Record<string, string> = {
  "/": "Visão do dia",
  "/agenda": "Agenda",
  "/pacientes": "Pacientes",
  "/profissionais": "Profissionais",
  "/atendimentos": "Atendimentos",
  "/especialidades": "Especialidades",
  "/servicos": "Serviços",
  "/usuarios": "Usuários",
  "/configuracoes": "Configurações",
};

export function AppShell() {
  const { user, logout } = useAuth();
  const brandQuery = useClinicBrand();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  if (!user) {
    return null;
  }

  const brand = brandQuery.data;
  const visibleNavigation = navigation.filter(
    (item) => !item.roles || item.roles.includes(user.role),
  );
  const brandStyle = brand?.primaryColor
    ? ({ "--brand-primary": brand.primaryColor } as CSSProperties)
    : undefined;

  return (
    <div className="app-shell" style={brandStyle}>
      <aside className="sidebar">
        <Brand
          name={brand?.displayName ?? "Clínica Modelo"}
          logoUrl={brand?.logoUrl}
        />
        <Navigation items={visibleNavigation} />
        <UserSummary name={user.name} role={user.role} onLogout={logout} />
      </aside>

      <div className="app-shell__main">
        <header className="mobile-header">
          <RadixDialog.Root open={mobileOpen} onOpenChange={setMobileOpen}>
            <RadixDialog.Trigger className="icon-button" aria-label="Abrir menu">
              <Menu size={20} />
            </RadixDialog.Trigger>
            <RadixDialog.Portal>
              <RadixDialog.Overlay className="dialog-overlay" />
              <RadixDialog.Content className="mobile-nav">
                <div className="mobile-nav__header">
                  <RadixDialog.Title className="sr-only">Navegação</RadixDialog.Title>
                  <Brand
                    name={brand?.displayName ?? "Clínica Modelo"}
                    logoUrl={brand?.logoUrl}
                  />
                  <RadixDialog.Close className="icon-button" aria-label="Fechar menu">
                    <X size={20} />
                  </RadixDialog.Close>
                </div>
                <Navigation
                  items={visibleNavigation}
                  onNavigate={() => setMobileOpen(false)}
                />
                <UserSummary name={user.name} role={user.role} onLogout={logout} />
              </RadixDialog.Content>
            </RadixDialog.Portal>
          </RadixDialog.Root>
          <span>{pageTitles[location.pathname] ?? "Clínica"}</span>
          <span className="mobile-header__avatar" aria-hidden="true">
            {getInitials(user.name)}
          </span>
        </header>
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function Brand({ name, logoUrl }: { name: string; logoUrl?: string | null }) {
  return (
    <div className="brand">
      {logoUrl ? (
        <img src={logoUrl} alt="" className="brand__logo" />
      ) : (
        <span className="brand-mark" aria-hidden="true">{getInitials(name)}</span>
      )}
      <div>
        <strong>{name}</strong>
        <span>Gestão clínica</span>
      </div>
    </div>
  );
}

function Navigation({
  items,
  onNavigate,
}: {
  items: NavigationItem[];
  onNavigate?: () => void;
}) {
  return (
    <nav className="navigation" aria-label="Navegação principal">
      <p className="navigation__label">Rotina</p>
      {items.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            onClick={onNavigate}
            className={({ isActive }) =>
              `navigation__link ${isActive ? "navigation__link--active" : ""}`
            }
          >
            <Icon size={18} strokeWidth={1.8} aria-hidden="true" />
            <span>{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
}

function UserSummary({
  name,
  role,
  onLogout,
}: {
  name: string;
  role: Role;
  onLogout: () => void;
}) {
  return (
    <div className="user-summary">
      <span className="user-summary__avatar" aria-hidden="true">
        {getInitials(name)}
      </span>
      <div>
        <strong>{name}</strong>
        <span>{roleLabels[role]}</span>
      </div>
      <button className="icon-button" onClick={onLogout} aria-label="Sair">
        <LogOut size={17} />
      </button>
    </div>
  );
}

function getInitials(value: string): string {
  return value
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}
