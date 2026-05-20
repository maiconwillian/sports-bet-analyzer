# Sports Bet Analyzer - Phase 1.2: Persistence + Historical Data

## 📋 Visão Geral
Esta fase focou na implementação da camada de persistência e na estruturação dos dados históricos capturados da API-Football. A arquitetura segue os princípios da Clean Architecture, garantindo separação de responsabilidades e facilidade de manutenção.

## 🚀 Implementações Realizadas

### 1. Esquema de Banco de Dados (Flyway)
Foram criadas migrations para estruturar o banco de dados PostgreSQL 16:
- `leagues`: Armazena informações sobre as ligas e competições.
- `teams`: Armazena dados dos times, incluindo logos e países.
- `matches`: Tabela central que relaciona ligas, times e armazena resultados, datas e status.
- **Índices**: Otimização de buscas por data, liga, status e ID da API.
- **Constraints**: Garantia de integridade referencial e validações de dados (ex: gols não negativos).

### 2. Entidades de Domínio e Persistência
- Entidades JPA (`League`, `Team`, `Match`) configuradas com Lombok e Hibernate.
- Relacionamentos `@ManyToOne` e `@OneToMany` mapeados para navegação eficiente.
- Enum `MatchStatus` expandido para suportar todos os estados da API (NS, FT, LIVE, etc.).

### 3. Camada de Mapeamento (MapStruct)
- Mappers altamente performáticos para converter DTOs da API em Entidades e Entidades em DTOs de Resposta.
- Lógica de conversão de datas (ZonedDateTime para LocalDateTime) centralizada.

### 4. Sincronização de Fixtures (`FixtureSyncService`)
- **Lógica de Upsert**: O sistema identifica se uma partida já existe (via `api_id`) e decide entre criar um novo registro ou atualizar o existente.
- **Sincronização por Data**: Endpoint para capturar jogos de um dia específico.
- **Sincronização por Range**: Processamento em lote para carregar históricos de datas passadas.
- **Validação**: `MatchSyncValidator` garante que dados inconsistentes da API não corrompam o banco de dados.

### 5. Endpoints Administrativos
- `POST /api/admin/sync/fixtures?date=YYYY-MM-DD`: Sincroniza um dia.
- `POST /api/admin/sync/fixtures/range?from=YYYY-MM-DD&to=YYYY-MM-DD`: Sincroniza um período.
- `GET /api/admin/sync/status`: Status geral da base de dados sincronizada.

## 🛠 Configurações
- **Flyway**: Ativado para migrações automáticas.
- **JPA**: Configurado para `validate`, garantindo que o código esteja sempre em sincronia com as migrations.

## 📂 Estrutura de Pastas (Novas Adições)
```
src/main/resources/db/migration/  -> Scripts SQL de migração
src/main/java/com/betanalyzer/
  ├── application/
  │     ├── FixtureSyncService.java
  │     └── MatchSyncValidator.java
  ├── domain/
  │     ├── model/ (League, Team, Match)
  │     └── enums/ (MatchStatus)
  └── infrastructure/
        └── persistence/ (Repositories)
```

---
*Este projeto faz parte da Phase 1.2 do Sports Bet Analyzer.*