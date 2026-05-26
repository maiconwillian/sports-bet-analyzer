Estou em modo Ask — não consigo gravar o arquivo no disco. Se quiser que eu aplique direto, mude para Agent mode. Abaixo está o conteúdo completo atualizado para substituir backend/SportsBetAnalysisPlatform.md (ou copiar/colar).

# 🎯 OddWise — Sports Bet Analyzer | PROJECT CHARTER
**Última Atualização:** 2026-05-25  
**Status:** 🚀 Phase 1.7 Backtesting V1 ✅ + Frontend operacional ✅  
**Versão:** 3.0 (6 ligas, odds integradas, UI completa, operação local)
---
## 📊 ROADMAP DE IMPLEMENTAÇÃO
### ✅ CONCLUÍDO (Sprints 1–8)
**Phase 1.1 — API-Football + Core Domain**
- Sync de fixtures e partidas
- Entidades Match, League, Team, MatchStats
- Flyway + PostgreSQL 16
- Clean Architecture (controller → application → domain → infrastructure)
  **Phase 1.5a — Feature Layer Básica ✅**
- `MatchFeatureContext` DTO
- `Over25FeatureExtractor`
- `FeatureCalculationService`
- `@Mapping` em MatchStats → Features
  **Phase 1.5b — Analysis Snapshot ✅**
- `MatchAnalysisSnapshot` (JSONB)
- `AnalysisSnapshotService` (serialização versionada)
- Auditoria de decisões
- Testes E2E
  **Phase 1.6 — Strategy Engine ✅**
- `BettingStrategy` interface
- `Over25Strategy` (lógica avançada)
- `StrategyResult` (Record Java 21)
- `StrategyEvaluationService` (multi-strategy)
- Testes unitários
  **Phase 1.4 — The Odds API Integration ✅**
- Integração The Odds API (`regions: us,eu`)
- Captura de odds Over 2.5 por partida
- Histórico persistido + CLV (Closing Line Value)
- `OddsController` consolidado
- Validação: liga não suportada → erro 400 (não retorno vazio)
- Testes unitários (`OddsHistoryServiceTest`, etc.)
  **Phase 1.5 — League Filter Enrichment ✅**
- `SupportedLeague` com **6 ligas** + mapeamento **nome + país**
- Diferenciação Serie A (Itália) vs Serie A (Brasil)
- Endpoint `GET /api/matches/supported-league/{enum}`
- `MatchResponseDTO.leagueCountry`
- Filtro de partidas apenas em ligas suportadas
  **Phase 1.7 — Backtesting V1 ✅ (parcial)**
- `HistoricalReplayService` — replay por partida
- `BacktestingService` — agregação de resultados
- `MetricsCalculationService` — ROI, winrate, drawdown
- `POST /api/backtesting/run` (modo `FIXED_STAKE` apenas)
- UI Backtesting no frontend
- ⚠️ Pendente: validação com **volume real** de histórico acumulado localmente
  **Frontend React (OddWise) ✅**
- Monorepo: `../frontend` (React 18 + Vite 6 + TypeScript)
- TanStack Query + React Router + Tailwind + shadcn-style UI
- Proxy dev: `:5173` → `/api` → backend `:8080`
---
### 🔄 PRÓXIMO (Sprints 9–11)
**Phase 1.5c — Value Bet Detection 🔄 PRÓXIMA**
- Detectar bets com EV+ (probabilidade modelo vs mercado)
- Kelly Criterion / gestão de stake
- Integração automática com `StrategyEvaluationService`
  **Phase 1.8 — Automação de sugestões**
- Gerar 3–5 sugestões/semana (meta do charter)
- Hoje: fluxo **manual** via UI (liga → partida → odd → sugestão)
- Critérios: confiança mínima, EV+, liga validada por backtest
  **Phase 1.9 — Sync inteligente**
- Sync API-Football **somente ligas suportadas** (evitar ruído no banco)
- Scheduler local ou cron manual
---
### 🔮 FUTURO (Phase 2+)
| Phase | Escopo |
|-------|--------|
| Phase 2 | n8n — automação, scheduler, pipelines |
| Phase 3 | Redis — cache + rate limit |
| Phase 4 | MongoDB — histórico RAW / features para ML |
| Phase 5 | Telegram Bot — notificações de sugestões |
| Phase 6 | Deploy cloud (decisão atual: **manter tudo local** na Phase 1) |
---
## 📌 VISÃO
Plataforma full-stack (Java 21 + React) para análise quantitativa de apostas esportivas: captura de odds, extração de features, avaliação de estratégias, backtesting e registro operacional de sugestões — com foco em rastreabilidade, EV+ e validação estatística antes de automatizar.
---
## 🎯 OBJETIVO FINAL
Gerar de forma automatizada entre **3 e 5 sugestões de apostas semanais** com expectativa de ROI positivo e sustentável no longo prazo.
### Metas Iniciais
- ROI mensal > 8%
- Drawdown máximo < 15%
- Winrate sustentável > 55%
- CLV (Closing Line Value) positivo
- Processo 100% automatizado *(meta — hoje semi-manual)*
### Estado atual (maio/2026)
- ✅ Pipeline técnico completo: sync → features → estratégia → odds → sugestão → relatório
- ✅ UI operacional para uso diário local
- 🔄 Automação 3–5/semana ainda não implementada
- 🔄 Backtest existe, mas precisa de dados históricos acumulados para confiar nos números
---
## 🧠 FILOSOFIA DO SISTEMA
- **Data-driven decisions** — dados acima de opinião
- **Histórico acima de opinião** — backtest obrigatório antes de escalar stake
- **EV+ acima de winrate** — expected value é a métrica real
- **Automação first** — meta de longo prazo; Phase 1 aceita operação manual disciplinada
- **Estratégias testáveis** — cada estratégia tem ROI documentado
- **Gestão de risco integrada** — Kelly Criterion (Phase 1.5c)
- **Consistência acima de lucro rápido** — longo prazo
- **Narrow and deep** — poucas ligas, um mercado principal, qualidade > quantidade
---
## 🔥 FILOSOFIA: NARROW AND DEEP
### Princípio Core
Em vez de analisar tudo, **especializamos em nichos validáveis**.
### Ligas suportadas (6) — `SupportedLeague`
| Enum | Liga | País | Score | The Odds API key |
|------|------|------|-------|------------------|
| `CHAMPIONS_LEAGUE` | UEFA Champions League | World | 97 | `soccer_uefa_champs_league` |
| `PREMIER_LEAGUE` | Premier League | England | 98 | `soccer_epl` |
| `LA_LIGA` | La Liga | Spain | 95 | `soccer_spain_la_liga` |
| `BUNDESLIGA` | Bundesliga | Germany | 96 | `soccer_germany_bundesliga` |
| `SERIE_A` | Serie A | **Italy** | 95 | `soccer_italy_serie_a` |
| `BRASILEIRAO` | Serie A | **Brazil** | 90 | `soccer_brazil_campeonato` |
> **Nota:** API-Football usa "Serie A" para Brasil e Itália. O sistema resolve ambiguidade via **`matches(name, country)`** — nunca confiar só no nome da liga.
### Mercado principal
- **Over 2.5 Goals** — mercado inicial (95% modelável, estatístico)
- BTTS configurado em `supported-markets`, mas não é foco operacional ainda
### ❌ Evitar
- Mercado "Winner" (alta variância emocional)
- Ligas fora do enum (ex.: Libertadores → sem odds The Odds API)
- Amistosos / pré-temporada / estaduais (filtrados por `DataQualityValidator`)
- Misturar ligas de países diferentes no mesmo backtest sem filtro
### Estratégia operacional (atualizada)
- **Cadastro amplo (6 ligas)** para captura de dados e odds
- **Operação estreita (subset)** — apostar só nas ligas com backtest positivo
- Exemplo inicial de foco: Premier + Champions + Brasileirão (ajustável após backtest real)
---
## 🛠 STACK TECH
### Backend
- **Java 21 LTS** — records, pattern matching
- **Spring Boot 3.3.x**
- **Maven**, **Lombok**, **MapStruct**
- **PostgreSQL 16** + **Flyway** ✅
- **Docker Compose** ✅
### Frontend (`oddwise-frontend`)
- **React 18** + **Vite 6** + **TypeScript**
- **TanStack Query** (server state) + **TanStack Table**
- **React Hook Form** + **Zod**
- **Tailwind CSS** + componentes UI (shadcn-style)
- **Recharts** (gráficos), **Sonner** (toasts), **Zustand** (tema)
### Integrações Externas
| Serviço | Status | Uso |
|---------|--------|-----|
| API-Football | ✅ Integrado | Fixtures, stats, resultados |
| The Odds API | ✅ Integrado | Odds Over 2.5, múltiplos bookmakers |
| n8n | 🔮 Phase 2 | Automação |
| Telegram Bot | 🔮 Phase 5 | Alertas |
### Infraestrutura (decisão Phase 1)
- **100% local:** Docker (Postgres) + backend `:8080` + frontend `:5173`
- Sem deploy cloud por enquanto (Vercel/Render/Neon avaliados, não adotados)
- GitHub + CI/CD — futuro
### Padrões Arquiteturais
- Clean Architecture ✅
- SOLID ✅
- Modular Monolith ✅
- Domain-Oriented Design ✅
- Event-Driven Ready (futuro)
---
## 🖥 FRONTEND — Módulos e Rotas
| Rota | Página | Função |
|------|--------|--------|
| `/` | Dashboard | Visão geral, métricas |
| `/matches` | Partidas | Lista filtrada (liga, status, ao vivo) |
| `/matches/:id` | Detalhe | Stats, captura odds, link para sugestão |
| `/suggestions` | Sugestões | Listagem, Ganho/Perdido/Void |
| `/suggestions/new?matchId=` | Nova sugestão | Fluxo liga → partida → odd (combobox) |
| `/odds` | Odds | Histórico e consulta |
| `/backtesting` | Backtesting | Formulário + resultados + gráfico |
| `/reports` | Relatórios | ROI diário/mensal, status summary |
| `/admin/sync` | Admin Sync | Sync fixtures (manual) |
### Fluxos operacionais validados
1. **Sync** — Admin → sync fixtures por data/range
2. **Partidas** — filtro liga suportada + badge com `leagueCountry`
3. **Odds** — captura por partida (ex.: Coritiba vs Bahia → 22 odds Pinnacle/etc.)
4. **Sugestão manual** — escolher odd real capturada, EV, confiança, stake
5. **Resultado** — marcar WIN/LOSS/VOID via `actualResult` (não `status`)
6. **Backtest** — período + liga + estratégia + stake fixo
### Status "Ao vivo" (frontend)
Inclui: `LIVE`, `HT`, `ONE_H`, `TWO_H`, `ET`, `1H`, `2H` (`IN_PLAY_MATCH_STATUSES`)
---
## 🔌 API — Endpoints principais
### Matches — `/api/matches`
- `GET /` — listar (filtros)
- `GET /{id}` — detalhe
- `GET /date/{date}` — por data
- `GET /supported-league/{league}` — por enum suportado
- `POST /` — criar
### Odds — `/api/odds`
- `POST /capture/{matchId}` — capturar odds The Odds API
- `GET /match/{matchId}` — histórico da partida
- `GET /match/{matchId}/latest` — última captura
- `GET /{matchId}/clv` — Closing Line Value
### Suggestions — `/api/suggestions`
- `GET /`, `GET /{id}`, `POST /`
- `PATCH /{id}/result` — body: `{ "actualResult": "WIN" | "LOSS" | "VOID" }`
### Backtesting — `/api/backtesting`
- `POST /run` — body: datas, liga, strategyVersion, stake, minimumConfidence, simulationMode
### Reports — `/api/reports`
- `GET /roi`, `/roi/daily`, `/roi/monthly`, `/status-summary`
### Admin — `/api/admin/sync`
- `POST /fixtures`, `POST /fixtures/range`, `GET /status`
---
## 🧪 TESTES E VALIDAÇÃO
### Backend
- Testes unitários: `SupportedLeagueTest`, `OddsHistoryServiceTest`, `MatchServiceLeagueFilterTest`, `HistoricalReplayServiceTest`, `BacktestingServiceValidationTest`
- E2E de análise/snapshot (Phase 1.5b)
### Fluxos testados manualmente ✅
- Captura odds Brasileirão (The Odds API key correta)
- Badge liga Brasil vs Itália
- Nova sugestão com odd do bookmaker
- Atualização resultado sugestão (WIN/LOSS)
- Listagem enriquecida (`homeTeamName`, `awayTeamName`, `leagueName`)
### Bugs corrigidos (maio/2026)
- Sport keys The Odds API (Champions, Brasileirão)
- `MatchDetailPage` usando endpoint errado de odds
- `capturedAt` serialização Jackson → parser frontend
- Payload Ganho/Perdido (`actualResult` vs `status`)
- `useState` import em `NewSuggestionPage`
---
## 🚀 COMO RODAR (local)
```bash
# 1. Postgres (Docker)
docker compose up -d
# 2. Backend
cd backend
./mvnw spring-boot:run
# → http://localhost:8080
# 3. Frontend
cd frontend
npm install
npm run dev
# → http://localhost:5173  (proxy /api → :8080)
