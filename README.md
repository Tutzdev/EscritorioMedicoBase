# Escritório Médico Base

Base reutilizável para clínicas e consultórios, com API Spring Boot e aplicação web React. O núcleo cobre a rotina compartilhada entre especialidades: pacientes, profissionais, serviços, disponibilidade, agenda, atendimento básico, usuários e identidade da clínica.

O projeto é deliberadamente genérico. Exames, radiologia, imagens médicas, laudos especializados e fluxos equivalentes não fazem parte do núcleo. Uma necessidade clínica concreta deve entrar como módulo de domínio próprio, sem adicionar campos opcionais ou regras específicas às entidades compartilhadas.

## Funcionalidades

- autenticação JWT com tokens de acesso e renovação;
- perfis de administrador, recepção e profissional;
- pacientes com validação de CPF e ativação/inativação;
- especialidades, profissionais e catálogo de serviços;
- disponibilidade semanal por profissional;
- agenda com duração calculada pelo serviço;
- bloqueio de conflito de horários e agendamento fora da disponibilidade;
- confirmação, início, falta, reagendamento e cancelamento com motivo;
- registro básico de atendimento e conclusão da consulta;
- painel diário com métricas e próximos horários;
- configuração por clínica: nome, dados institucionais, fuso, cor e logotipo;
- interface responsiva com estados de carregamento, vazio, erro e confirmação.

## Permissões

| Recurso | Administrador | Recepção | Profissional |
| --- | --- | --- | --- |
| Usuários, especialidades, serviços e configurações | Gerencia | — | — |
| Profissionais e disponibilidade | Gerencia | Consulta | Consulta |
| Pacientes | Gerencia | Gerencia | Consulta |
| Agenda | Gerencia | Gerencia | Consulta e atualiza fluxo |
| Atendimentos | Registra | Consulta | Registra |

As permissões são aplicadas na API. Ocultar uma ação no frontend não substitui autorização no servidor.

## Tecnologias

Backend:

- Java 21 e Spring Boot 4.1;
- Spring Web MVC, Security, Data JPA e Bean Validation;
- PostgreSQL e Flyway;
- Maven Wrapper, JUnit 5, Mockito e H2 para testes.

Frontend:

- React 19 e TypeScript estrito;
- Vite 8;
- TanStack Query e React Router;
- Radix Dialog e Lucide;
- CSS responsivo e fontes locais Manrope/Newsreader.

## Organização

```text
src/main/java/.../
├── appointment/       # agenda e transições
├── auth/              # sessão e segurança
├── availability/      # períodos semanais
├── clinic/            # identidade da instalação
├── consultation/      # registro assistencial básico
├── dashboard/         # visão operacional
├── patient/
├── professional/
├── servicecatalog/
├── specialty/
├── user/
└── shared/

frontend/src/
├── api/
├── app/
├── auth/
├── components/
├── hooks/
├── pages/
├── types/
└── utils/
```

## Configuração local

Pré-requisitos:

- JDK 21;
- PostgreSQL;
- Node.js 22 ou superior e npm.

Crie um banco vazio:

```sql
CREATE DATABASE medical_office;
```

Configure as variáveis a partir de [.env.example](.env.example). No PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/medical_office"
$env:DB_USERNAME="medical_user"
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="uma-chave-aleatoria-com-pelo-menos-32-caracteres"
```

Para criar o primeiro administrador em uma instalação controlada, habilite o bootstrap somente na primeira inicialização:

```powershell
$env:BOOTSTRAP_ADMIN_ENABLED="true"
$env:BOOTSTRAP_ADMIN_NAME="Administrador"
$env:BOOTSTRAP_ADMIN_EMAIL="admin@clinica.local"
$env:BOOTSTRAP_ADMIN_PASSWORD="uma-senha-forte"
```

Depois do primeiro acesso, desabilite `BOOTSTRAP_ADMIN_ENABLED`.

## Execução

API no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

API no Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Frontend, em outro terminal:

```powershell
cd frontend
npm install
npm run dev
```

A API usa `http://localhost:8080` e o Vite usa `http://localhost:5173`. Durante o desenvolvimento, o frontend encaminha `/api` para a API. Para outro endereço, defina `VITE_API_URL` conforme [frontend/.env.example](frontend/.env.example).

## Banco e migrações

O Hibernate opera com `ddl-auto=validate`: a estrutura é criada e evoluída somente pelo Flyway.

- `V1__create_users_table.sql` permanece como histórico original;
- `V2__create_shared_clinic_core.sql` adiciona o núcleo clínico sem apagar dados existentes;
- novas alterações de schema devem receber uma nova versão;
- não edite uma migração já aplicada em ambientes compartilhados.

## Verificação

Backend:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
cd frontend
npm run lint
npm run build
```

Os testes cobrem inicialização com migrações, CPF, tokens, disponibilidade, conflitos e transições essenciais da agenda.

## Segurança e dados

- não versione senhas, segredos JWT ou credenciais de banco;
- use um segredo JWT aleatório e diferente em cada instalação;
- restrinja `CORS_ALLOWED_ORIGINS` aos endereços reais do frontend;
- não use dados reais de pacientes em demonstrações públicas;
- HTTPS, backup, observabilidade e rotação de segredos devem ser definidos no ambiente de implantação.

## Extensão por especialidade

O núcleo expõe identificadores estáveis de paciente, profissional, especialidade, agendamento e atendimento. Um módulo futuro deve se apoiar nesses contratos e possuir suas próprias tabelas, serviços, endpoints, permissões e telas. Isso evita transformar o atendimento básico em uma entidade genérica cheia de campos sem significado para outras clínicas.

## Autor

Desenvolvido por [Tutzdev](https://github.com/Tutzdev).
