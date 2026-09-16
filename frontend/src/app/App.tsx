import { Navigate, Route, Routes } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "../auth/AuthContext";
import { AppShell } from "../components/layout/AppShell";
import { AppointmentsPage } from "../pages/AppointmentsPage";
import { ConsultationsPage } from "../pages/ConsultationsPage";
import { DashboardPage } from "../pages/DashboardPage";
import { LoginPage } from "../pages/LoginPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { PatientsPage } from "../pages/PatientsPage";
import { ProfessionalsPage } from "../pages/ProfessionalsPage";
import { ServicesPage } from "../pages/ServicesPage";
import { SettingsPage } from "../pages/SettingsPage";
import { SpecialtiesPage } from "../pages/SpecialtiesPage";
import { UsersPage } from "../pages/UsersPage";

export function App() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="app-loading" aria-label="Carregando sessão" aria-busy="true">
        <span className="brand-mark" aria-hidden="true">CM</span>
        <span className="button__spinner" aria-hidden="true" />
      </div>
    );
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={user ? <Navigate to="/" replace /> : <LoginPage />}
      />
      <Route
        element={user ? <AppShell /> : <Navigate to="/login" replace />}
      >
        <Route index element={<DashboardPage />} />
        <Route path="agenda" element={<AppointmentsPage />} />
        <Route path="pacientes" element={<PatientsPage />} />
        <Route path="profissionais" element={<ProfessionalsPage />} />
        <Route path="especialidades" element={<RequireAdmin><SpecialtiesPage /></RequireAdmin>} />
        <Route path="servicos" element={<RequireAdmin><ServicesPage /></RequireAdmin>} />
        <Route path="atendimentos" element={<ConsultationsPage />} />
        <Route path="usuarios" element={<RequireAdmin><UsersPage /></RequireAdmin>} />
        <Route path="configuracoes" element={<RequireAdmin><SettingsPage /></RequireAdmin>} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { user } = useAuth();

  return user?.role === "ADMIN" ? children : <Navigate to="/" replace />;
}
