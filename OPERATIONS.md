# OddWise — Operações (rotina local)

Guia operacional semanal para uso da plataforma em ambiente local.

**Pré-requisitos:** Docker (Postgres), backend `:8080`, frontend `:5173`.  
**Charter:** [SportsBetAnalysisPlatform.md](SportsBetAnalysisPlatform.md) · **Roadmap:** [ROADMAP_STATUS.md](ROADMAP_STATUS.md)

---

## Subir ambiente

```bash
# Postgres
docker compose up -d

# Backend
./mvnw spring-boot:run

# Frontend (repo oddwise-frontend)
npm run dev
```

Variáveis sensíveis em `.env` (nunca commitar): `API_FOOTBALL_KEY`, `ODDS_API_KEY`, credenciais DB.

---

## Rotina semanal sugerida

### 1. Sync de fixtures (Admin)

- UI: `/admin/sync` ou `POST /api/admin/sync/fixtures/range`
- Sincronizar datas da semana para ligas suportadas
- Verificar partidas em `/matches` com filtro de liga

### 1b. Enriquecer análise (Phase 1.75 — Sprint 10/11)

- `POST /api/admin/enrich/fixtures?date=YYYY-MM-DD` (recomendado após sync)
- `POST /api/admin/enrich/fixtures/range?from=&to=` (intervalo — Sprint 11)
- `POST /api/matches/{id}/enrich` (partida única)
- `app.enrich.auto-after-sync=false` por padrão (ADR-014)
- `GET /api/matches/{id}/analysis` — stats + `modelInsight` (EV com odd capturada quando existir)

### 2. Captura de odds

- Apenas partidas **não finalizadas**: `NS`, `TBD` ou ao vivo (`LIVE`, `HT`, `1H`, `2H`, `ET`).
- `FT`, `AET`, `CANC`, etc. → `400` com mensagem de negócio (não chama The Odds API).
- `POST /api/odds/capture/{matchId}` ou botão na UI
- Confirmar histórico em `/odds` ou detalhe da partida
- Preferir bookmakers líquidos (ex.: Pinnacle)

### 3. EV+ hub da rodada (Sprint 11)

Rotina: **enrich → odds → EV+** (duas visões).

- `GET /api/analysis/value-bets?date=` — oportunidades que passam limiares (`statsIncomplete`, `hint`)
- `GET /api/analysis/match-insights?date=` — radar do dia (todos NS/TBD elegíveis, ordenado por confiança)
- Limiares: `app.value-bet.min-confidence` (65), `min-ev` (0.05)
- Por partida: `GET /api/analysis/value-bets/match/{matchId}` · `GET /api/matches/{id}/analysis`
- Teste piloto: [SPRINT_11.md](SPRINT_11.md) (`2026-05-30`, PSG x Arsenal ~61% no radar, vazio em value-bets)

### 3b. Picks da rodada (1.77 — Sprint 12)

- `GET /api/analysis/round-picks?date=` — `topPick`, `rankedPicks`, `actionablePicks`
- `GET /api/analysis/match/{matchId}/picks` — `bestPick` + `recommendations`
- Pick recomendado ≠ oportunidade EV+ automática (pode ter 61% conf. e EV+)
- Ver [SPRINT_12.md](SPRINT_12.md)

### 4. Nova sugestão (manual)

Fluxo UI: `/suggestions/new`

1. Selecionar liga suportada
2. Escolher partida elegível (NS, TBD, LIVE, HT, ONE_H, TWO_H)
3. Selecionar odd capturada (combobox: odd — bookmaker — mercado)
4. Preencher EV, confiança, stake, notas
5. Salvar

### 5. Acompanhar resultados

- Após jogo: marcar **WIN**, **LOSS** ou **VOID**
- API: `PATCH /api/suggestions/{id}/result` com `{ "actualResult": "..." }`
- Conferir relatórios em `/reports`

### 6. Backtest antes de escalar

- UI: `/backtesting`
- Escolher liga, período, versão da estratégia, stake fixo
- Analisar ROI, drawdown, winrate
- **Só aumentar stake ou expandir ligas** se backtest com volume real for positivo

---

## Checklist pré-aposta

- [ ] Stats enriquecidas (`stats_enriched` / `GET .../analysis`)
- [ ] Liga está em `SupportedLeague` e tem odds capturadas
- [ ] EV+ e confiança dentro dos critérios pessoais
- [ ] Liga validada por backtest recente (subset operacional)
- [ ] Stake alinhado à gestão de risco (Kelly = Phase 1.5c)

---

## Troubleshooting rápido

| Problema | Ação |
|----------|------|
| Liga errada no badge (Serie A IT vs BR) | Verificar `leagueCountry` no DTO |
| Captura odds vazia | Liga não suportada na The Odds API → 400 esperado |
| Ganho/Perdido não salva | Usar `actualResult`, não `status` |
| Backtest sem partidas | Sync fixtures no período + liga correta |
| EV+ vazio com odds | `POST /api/admin/enrich/fixtures?date=` antes do scan |

---

## Quotas de API

- API-Football: sync + **enrich** sob demanda (2× chamadas por time em enrich); ver [SPRINT_10.md](SPRINT_10.md)
- The Odds API: uma captura por partida quando necessário; monitorar uso mensal
