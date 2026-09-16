export type Role = "ADMIN" | "RECEPTIONIST" | "DOCTOR";

export type AppointmentStatus =
  | "SCHEDULED"
  | "CONFIRMED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "CANCELED"
  | "NO_SHOW";

export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface CurrentUser {
  id: string;
  name: string;
  email: string;
  role: Role;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresInSeconds: number;
}

export interface LoginResponse {
  tokens: TokenResponse;
  user: CurrentUser;
}

export interface PublicClinicSettings {
  displayName: string;
  primaryColor: string;
  logoUrl: string | null;
}

export interface ClinicSettings extends PublicClinicSettings {
  id: string;
  legalName: string | null;
  document: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  timeZone: string;
  updatedAt: string;
}

export interface Patient {
  id: string;
  name: string;
  cpf: string;
  birthDate: string;
  phone: string;
  email: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Specialty {
  id: string;
  name: string;
  description: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Professional {
  id: string;
  userId: string | null;
  specialtyId: string;
  specialtyName: string;
  name: string;
  registrationNumber: string;
  registrationState: string;
  phone: string | null;
  email: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceOffering {
  id: string;
  specialtyId: string;
  specialtyName: string;
  name: string;
  description: string | null;
  durationMinutes: number;
  price: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Availability {
  id: string;
  professionalId: string;
  professionalName: string;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Appointment {
  id: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  serviceId: string;
  serviceName: string;
  durationMinutes: number;
  scheduledStart: string;
  scheduledEnd: string;
  status: AppointmentStatus;
  notes: string | null;
  cancellationReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Consultation {
  id: string;
  appointmentId: string;
  patientId: string;
  patientName: string;
  professionalId: string;
  professionalName: string;
  scheduledStart: string;
  appointmentStatus: AppointmentStatus;
  observations: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserAccount {
  id: string;
  name: string;
  email: string;
  role: Role;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Dashboard {
  referenceDate: string;
  activePatients: number;
  appointmentsToday: number;
  pendingToday: number;
  completedToday: number;
  nextAppointments: Appointment[];
}

export interface FieldValidationError {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: FieldValidationError[];
}
