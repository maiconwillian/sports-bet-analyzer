# OddWise — Sports Bet Analyzer | PROJECT CHARTER

**Última Atualização:** 2026-05-26  
**Status:** Sprint 13 ✅ · Phase 1.8 propostas · EV+ hub · picks · enrich  
**Versão:** 3.1 (operação Over 2.5 end-to-end local)

**Roadmap (fonte única):** [ROADMAP_STATUS.md](ROADMAP_STATUS.md) · [../frontend/ROADMAP_STATUS.md](../frontend/ROADMAP_STATUS.md)  
**Documentos relacionados:** [DECISIONS.md](DECISIONS.md) · [OPERATIONS.md](OPERATIONS.md) · [GLOSSARY.md](GLOSSARY.md) · [PRODUCT_VISION_AGENT.md](PRODUCT_VISION_AGENT.md)  
**Frontend:** https://github.com/maiconwillian/oddwise-frontend

---

## Visão

Plataforma full-stack (Java 21 + React) para análise quantitativa de apostas esportivas: captura de odds, extração de features, avaliação de estratégias, backtesting e registro operacional de sugestões — com foco em rastreabilidade, EV+ e validação estatística antes de automatizar.

---

## Objetivo final

Gerar de forma automatizada entre **3 e 5 sugestões de apostas semanais** com expectativa de ROI positivo e sustentável no longo prazo.

### Metas iniciais

- ROI mensal > 8%
- Drawdown máximo < 15%
- Winrate sustentável > 55%
- CLV (Closing Line Value) positivo
- Processo 100% automatizado *(meta — hoje semi-manual)*

### Estado atual (maio/2026)

- ✅ Pipeline técnico: sync → features → estratégia → odds → sugestão → relatório
- ✅ UI operacional para uso diário local
- 🔄 Automação 3–5/semana ainda não implementada
- 🔄 Backtest existe, mas precisa de volume histórico acumulado para confiar nos números

---

## Roadmap

### Concluído (Sprints 1–8)

**Phase 1.1 — API-Football + Core Domain**
- Sync de fixtures e partidas
- Entidades Match, League, Team, MatchStats
- Flyway + PostgreSQL 16
- Clean Architecture (controller → application → domain → infrastructure)

**Phase 1.5a — Feature Layer Básica**
- `MatchFeatureContext` DTO
- `Over25FeatureExtractor`
- `FeatureCalculationService`

**Phase 1.5b — Analysis Snapshot**
- `MatchAnalysisSnapshot` (JSONB)
- `AnalysisSnapshotService` (serialização versionada)
- Auditoria de decisões + testes E2E

**Phase 1.6 — Strategy Engine**
- `BettingStrategy`, `Over25Strategy`, `StrategyEvaluationService`
- Testes unitários

**Phase 1.4 — The Odds API Integration**
- Integração The Odds API (`regions: us,eu`)
- Captura odds Over 2.5, histórico persistido, CLV
- Liga não suportada → erro 400

**Phase 1.5 — League Filter Enrichment**
- `SupportedLeague` com 6 ligas + mapeamento nome + país
- Serie A (Itália) vs Serie A (Brasil)
- `GET /api/matches/supported-league/{enum}`

**Phase 1.7 — Backtesting V1** *(parcial)*
- `HistoricalReplayService`, `BacktestingService`, `MetricsCalculationService`
- `POST /api/backtesting/run` (modo `FIXED_STAKE` apenas)
- UI Backtesting no frontend
- ⚠️ Pendente: validação com volume real de histórico local

**Frontend React (OddWise)**
- Repo: `oddwise-frontend` (React 18 + Vite 6 + TypeScript)
- Proxy dev: `:5173` → `/api` → backend `:8080`

### Concluído (Sprint 9)

- Phase 1.5c — Value Bet Detection + `GET /api/analysis/value-bets`
- Liquidação automática Over 2.5 pós-sync
- Sync filtra 6 ligas (nome + país)
- Backtest `lowSample` (mín. 30 apostas)

### Próximo (roadmap agente — [PRODUCT_VISION_AGENT.md](PRODUCT_VISION_AGENT.md))

| Fase | Escopo |
|------|--------|
| ~~**1.75**~~ ✅ | Match Intelligence — enrich stats (API-Football); UI análise + EV+ com meta (Sprint 10) |
| ~~**Sprint 11**~~ ✅ | `match-insights` batch; EV+ hub — [SPRINT_11.md](SPRINT_11.md) |
| ~~**1.77**~~ ✅ | Pick ranking — [SPRINT_12.md](SPRINT_12.md) |
| ~~**1.8**~~ ✅ | Propostas semanais — [SPRINT_13.md](SPRINT_13.md) |
| **1.76** | Multi-market models — BTTS, 1X2 + backtest por mercado |
| **1.85** | Analyst Agent + MCP tools sobre API OddWise |
| **1.9** | Sync inteligente / scheduler local |

### Futuro (Phase 2+)

| Phase | Escopo |
|-------|--------|
| Phase 2 | **n8n** — pipeline semanal sync → enrich → picks → notificar (ver PRODUCT_VISION_AGENT) |
| Phase 3 | Redis — cache + rate limit |
| Phase 4 | MongoDB — histórico RAW / ML |
| Phase 5 | Telegram Bot — notificações |
| Phase 6 | Deploy cloud *(Phase 1: local-only)* |

---

## Filosofia

- **Data-driven** — dados acima de opinião
- **Histórico acima de opinião** — backtest antes de escalar stake
- **EV+ acima de winrate** — expected value é a métrica real
- **Narrow and deep** — poucas ligas, um mercado principal
- **Automação first** — meta de longo prazo; Phase 1 aceita operação manual disciplinada

### Ligas suportadas (6) — `SupportedLeague`

| Enum | Liga | País | Score | The Odds API key |
|------|------|------|-------|------------------|
| `CHAMPIONS_LEAGUE` | UEFA Champions League | World | 97 | `soccer_uefa_champs_league` |
| `PREMIER_LEAGUE` | Premier League | England | 98 | `soccer_epl` |
| `LA_LIGA` | La Liga | Spain | 95 | `soccer_spain_la_liga` |
| `BUNDESLIGA` | Bundesliga | Germany | 96 | `soccer_germany_bundesliga` |
| `SERIE_A` | Serie A | Italy | 95 | `soccer_italy_serie_a` |
| `BRASILEIRAO` | Serie A | Brazil | 90 | `soccer_brazil_campeonato` |

> API-Football usa "Serie A" para Brasil e Itália. Resolver via `matches(name, country)` — nunca confiar só no nome.

### Mercado principal

- **Over 2.5 Goals** — foco operacional
- BTTS em `supported-markets`, mas não é foco ainda

### Evitar

- Mercado Winner
- Ligas fora do enum (ex.: Libertadores)
- Amistosos / pré-temporada / estaduais
- Backtest misturando ligas sem filtro

### Estratégia operacional

- **Cadastro amplo (6 ligas)** para dados e odds
- **Operação estreita (subset)** — apostar só ligas com backtest positivo
- Foco inicial sugerido: Premier + Champions + Brasileirão *(ajustável após backtest real)*

---

## Stack

### Backend

- Java 21, Spring Boot 3.3.x, Maven, Lombok, MapStruct
- PostgreSQL 16 + Flyway + Docker Compose

### Frontend

- React 18, Vite 6, TypeScript, TanStack Query/Table, Tailwind

### Integrações

| Serviço | Status |
|---------|--------|
| API-Football | ✅ Fixtures, stats, resultados |
| The Odds API | ✅ Odds Over 2.5 (`regions: us,eu`) |
| n8n | Phase 2 |
| Telegram | Phase 5 |

### Infraestrutura

- **100% local:** Docker (Postgres) + backend `:8080` + frontend `:5173`
- Sem deploy cloud na Phase 1

---

## API — Endpoints principais

### Matches — `/api/matches`

- `GET /`, `GET /{id}`, `GET /date/{date}`, `GET /supported-league/{league}`, `POST /`

### Odds — `/api/odds`

- `POST /capture/{matchId}`, `GET /match/{matchId}`, `GET /match/{matchId}/latest`, `GET /{matchId}/clv`

### Suggestions — `/api/suggestions`

- `GET /`, `GET /{id}`, `POST /`
- `PATCH /{id}/result` — body: `{ "actualResult": "WIN" | "LOSS" | "VOID" }`

### Backtesting — `/api/backtesting`

- `POST /run`

### Reports — `/api/reports`

- `GET /roi`, `/roi/daily`, `/roi/monthly`, `/status-summary`

### Admin — `/api/admin/sync`

- `POST /fixtures`, `POST /fixtures/range`, `GET /status`

---

## Como rodar (local)

```bash
# 1. Postgres
docker compose up -d

# 2. Backend
./mvnw spring-boot:run
# → http://localhost:8080

# 3. Frontend (repo oddwise-frontend)
npm install && npm run dev
# → http://localhost:5173  (proxy /api → :8080)
```

---

## Checklist próximos passos

- [ ] Acumular histórico local (sync semanal por liga)
- [ ] Backtest real por liga → definir subset operacional
- [ ] Phase 1.5c (Value Bet Detection + Kelly)
- [ ] Automatizar 3–5 sugestões/semana
- [ ] Sync restrito a `supported-leagues`

---

*Charter vivo — atualizar a cada sprint. Ver ritual em DECISIONS.md.*
