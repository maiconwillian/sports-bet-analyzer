# Sprint 11 — EV+ utilizável + análise na rodada

**Data:** 2026-05-26  
**Repos:** `sports-bet-analyzer` (backend) · `oddwise-frontend`

Espelho operacional: ver também [SPRINT_11.md](https://github.com/maiconwillian/oddwise-frontend/blob/main/SPRINT_11.md) no frontend (mesmo conteúdo de teste).

## Objetivo

Batch `match-insights` para radar da rodada; EV exibido sempre com odd Over 2.5 capturada quando existir.

## API nova

- `GET /api/analysis/match-insights?date=YYYY-MM-DD&league=` (opcional `SupportedLeague`)
- `POST /api/admin/enrich/fixtures/range?from=&to=` (opcional P1)

## DTO `MatchInsightRowDTO`

`matchId`, times, liga, horário, status, `statsReady`, `hasOdds`, `confidence`, `expectedValue`, `referenceOdd`, `bookmaker`, `passesEvFilters`, `signalTier` (`EV_PLUS` | `NEAR` | `WEAK` | `NO_STATS`)

Ordenação: `confidence` desc.

## Teste manual

```http
POST /api/admin/sync/fixtures?date=2026-05-30
POST /api/admin/enrich/fixtures?date=2026-05-30
GET  /api/analysis/match-insights?date=2026-05-30
GET  /api/analysis/value-bets?date=2026-05-30
```

PSG x Arsenal (médias 2,80 + 2,10 → combined 2,45 → conf ~61%): aparece em `match-insights`, não em `value-bets` opportunities.

## Testes unitários

`MatchInsightsServiceTest` — tiers de sinal.

## ADR

[ADR-015](DECISIONS.md) — página EV+ = oportunidades + ranking do dia.
