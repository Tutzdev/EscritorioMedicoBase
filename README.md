# Medical Office API

API REST para gerenciamento de escritórios médicos, desenvolvida com Java e Spring Boot.

> Projeto de portfólio em desenvolvimento. O objetivo é centralizar usuários, médicos,
> pacientes, disponibilidades e agendamentos em uma API segura e organizada.

## Status

Em desenvolvimento.

Atualmente, o projeto possui a estrutura inicial da aplicação, configuração do PostgreSQL,
migrações com Flyway e a base do módulo de usuários.

## Funcionalidades planejadas

- [x] Estrutura inicial do projeto
- [x] Entidade, repositório e DTO de usuários
- [x] Migração inicial do banco de dados
- [ ] Autenticação e autorização
- [ ] Gerenciamento de usuários e perfis
- [ ] Cadastro de médicos
- [ ] Cadastro de pacientes
- [ ] Disponibilidade dos médicos
- [ ] Agendamento e cancelamento de consultas
- [ ] Prontuários e documentos médicos
- [ ] Exames e imagens médicas
- [ ] Auditoria e relatórios

## Tecnologias

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Jakarta Validation
- PostgreSQL
- Flyway
- Maven
- JUnit 5

## Estrutura

```text
src/
├── main/
│   ├── java/io/github/officemed/medical_office_api/
│   │   ├── appointment/
│   │   ├── auth/
│   │   ├── availability/
│   │   ├── doctor/
│   │   ├── patient/
│   │   ├── shared/
│   │   └── user/
│   └── resources/
│       ├── db/migration/
│       └── application.properties
└── test/
```

## Pré-requisitos

- JDK 21
- PostgreSQL
- Git

Não é necessário instalar o Maven globalmente, pois o projeto inclui o Maven Wrapper.

## Configuração

Crie o banco de dados PostgreSQL:

```sql
CREATE DATABASE medical_office;
```

Defina as credenciais do banco como variáveis de ambiente. No PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/medical_office"
$env:DB_USERNAME="medical_user"
$env:DB_PASSWORD="sua_senha_local"
```

No Bash:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/medical_office"
export DB_USERNAME="medical_user"
export DB_PASSWORD="sua_senha_local"
```

## Execução

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux ou macOS:

```bash
./mvnw spring-boot:run
```

A aplicação utiliza por padrão a porta `8080`.

## Segurança

- Credenciais não devem ser adicionadas ao Git.
- Use apenas variáveis de ambiente ou um gerenciador de segredos.
- Nunca utilize dados reais de pacientes no ambiente público de demonstração.
- Arquivos `.env`, certificados e configurações locais estão ignorados pelo Git.

## Autor

Desenvolvido por [Tutzdev](https://github.com/Tutzdev).
