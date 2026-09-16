CREATE TABLE specialties (
    id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_specialties PRIMARY KEY (id),
    CONSTRAINT uk_specialties_name UNIQUE (name)
);

CREATE TABLE patients (
    id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    birth_date DATE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_patients PRIMARY KEY (id),
    CONSTRAINT uk_patients_cpf UNIQUE (cpf)
);

CREATE TABLE professionals (
    id UUID NOT NULL,
    user_id UUID,
    specialty_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    registration_number VARCHAR(40) NOT NULL,
    registration_state VARCHAR(2) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_professionals PRIMARY KEY (id),
    CONSTRAINT uk_professionals_user UNIQUE (user_id),
    CONSTRAINT uk_professionals_registration UNIQUE (registration_number, registration_state),
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_professionals_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id)
);

CREATE TABLE services (
    id UUID NOT NULL,
    specialty_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    duration_minutes INTEGER NOT NULL,
    price NUMERIC(12, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_services PRIMARY KEY (id),
    CONSTRAINT uk_services_specialty_name UNIQUE (specialty_id, name),
    CONSTRAINT fk_services_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id),
    CONSTRAINT ck_services_duration CHECK (duration_minutes BETWEEN 5 AND 480),
    CONSTRAINT ck_services_price CHECK (price IS NULL OR price >= 0)
);

CREATE TABLE professional_availabilities (
    id UUID NOT NULL,
    professional_id UUID NOT NULL,
    day_of_week VARCHAR(12) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_professional_availabilities PRIMARY KEY (id),
    CONSTRAINT fk_availabilities_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
    CONSTRAINT ck_availabilities_interval CHECK (start_time < end_time),
    CONSTRAINT ck_availabilities_day CHECK (
        day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    )
);

CREATE TABLE appointments (
    id UUID NOT NULL,
    patient_id UUID NOT NULL,
    professional_id UUID NOT NULL,
    service_id UUID NOT NULL,
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(1000),
    cancellation_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_appointments PRIMARY KEY (id),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT ck_appointments_interval CHECK (scheduled_start < scheduled_end),
    CONSTRAINT ck_appointments_status CHECK (
        status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED', 'NO_SHOW')
    )
);

CREATE TABLE consultations (
    id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    observations VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_consultations PRIMARY KEY (id),
    CONSTRAINT uk_consultations_appointment UNIQUE (appointment_id),
    CONSTRAINT fk_consultations_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
);

CREATE TABLE clinic_settings (
    id UUID NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    legal_name VARCHAR(160),
    document VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(160),
    address VARCHAR(300),
    time_zone VARCHAR(60) NOT NULL,
    primary_color VARCHAR(7) NOT NULL,
    logo_url VARCHAR(500),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_clinic_settings PRIMARY KEY (id)
);

CREATE INDEX idx_patients_name ON patients (name);
CREATE INDEX idx_professionals_name ON professionals (name);
CREATE INDEX idx_services_name ON services (name);
CREATE INDEX idx_availabilities_professional_day
    ON professional_availabilities (professional_id, day_of_week, active);
CREATE INDEX idx_appointments_professional_period
    ON appointments (professional_id, scheduled_start, scheduled_end);
CREATE INDEX idx_appointments_patient ON appointments (patient_id);
CREATE INDEX idx_appointments_status_start ON appointments (status, scheduled_start);

INSERT INTO clinic_settings (
    id,
    display_name,
    time_zone,
    primary_color,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Clínica Modelo',
    'America/Sao_Paulo',
    '#256B5B',
    CURRENT_TIMESTAMP
);
