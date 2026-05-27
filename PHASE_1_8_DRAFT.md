# Phase 1.8 — Automação de sugestões (rascunho)

**Status:** implementado · Sprint 13 — [SPRINT_13.md](SPRINT_13.md)  
**Visão completa:** [PRODUCT_VISION_AGENT.md](PRODUCT_VISION_AGENT.md)

## Pré-requisitos (obrigatórios antes de `generate-weekly`)

| Fase | Entrega | Status |
|------|---------|--------|
| Phase 1.5c | Value bet detection (endpoint) | ✅ |
| Phase 1.7 | Backtest com amostra ≥ 30 apostas/liga (onde for operar) | 🔄 operador |
| **Phase 1.75** | Match Intelligence — stats reais (não 0/TBD) | ✅ Sprint 10 |
| **Phase 1.77** | Pick ranking — `bestPick` por jogo / rodada | ✅ [SPRINT_12.md](SPRINT_12.md) |

Sem **1.75 + 1.77**, `generate-weekly` repetiria o problema do EV+ vazio (motor sem dados).

## Meta

3–5 sugestões/semana com revisão humana antes de virar aposta operacional.

## Modelo proposto

1. `POST /api/suggestions/generate-weekly` — ranqueia picks (1.77): prob. modelada + EV+ por mercado, subset de ligas operacionais.
2. Entidade ou status `PROPOSED` — não mistura com `PENDING` operacional até aceite.
3. `GET /api/suggestions/proposed` — fila na UI.
4. `POST /api/suggestions/proposed/{id}/accept` — cria `BetSuggestion` PENDING com stake/odd da proposta.
5. `POST /api/suggestions/proposed/{id}/reject` — descarta.

## UI (frontend)

- Aba ou `/suggestions/proposed` com Aceitar / Rejeitar.
- Dashboard: widget `Propostas esta semana (N/5)`.

## Fora de escopo (1.8)

- Auto-aposta sem revisão.
- Narrativa LLM completa (→ Phase 1.85).
- Telegram / n8n (→ Phase 2.0).
