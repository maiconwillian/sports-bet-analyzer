# Sprint 9 — Entregas (2026-05-26)

## Backend

### Operação
- `SuggestionSettlementService` — liquida PENDING Over 2.5 (FT/AET + placar; VOID em PST/CANC/ABD/SUSP)
- `POST /api/suggestions/settle-pending`
- Sync de fixtures chama liquidação ao final; `SyncResult` inclui `settled`, `won`, `lost`, `voided`, `message`

### Phase 1.5c
- `ValueBetDetectionService` + `GET /api/analysis/value-bets?date=&league=`
- `GET /api/analysis/value-bets/match/{matchId}`
- Config: `app.value-bet` (min-ev, min-confidence, kelly-fraction, bankroll)

### Phase 1.7
- `BacktestResultDTO.lowSample` + `minSampleBets` (default 30 em `app.backtest.min-sample-bets`)

### Phase 1.8+
- Rascunho: [PHASE_1_8_DRAFT.md](PHASE_1_8_DRAFT.md)
- Visão agente multi-mercado: [PRODUCT_VISION_AGENT.md](PRODUCT_VISION_AGENT.md) · ADR-013

## Testes
- `SuggestionSettlementServiceTest` (3–2 win, 1–0 loss, PST void)
- `MarketUtilsTest`

## Frontend (repo `../frontend`)
- `/value-bets`, liquidação UI, cache invalidation, backtest lowSample banner
- Docs: PROJECT_CHARTER, DECISIONS, OPERATIONS, oddwise-vision
