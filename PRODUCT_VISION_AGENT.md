# OddWise — Visão: Agente de recomendação multi-mercado

**Status:** Phase 1.75 ✅ · Sprint 11 (EV+ hub + `match-insights`) · próximo 1.77  
**Última atualização:** 2026-05-26  
**Relacionado:** [SportsBetAnalysisPlatform.md](SportsBetAnalysisPlatform.md) · [PHASE_1_8_DRAFT.md](PHASE_1_8_DRAFT.md) · ADR-003 · ADR-013

**Espelho frontend:** `../frontend/PRODUCT_VISION_AGENT.md`

---

## Objetivo do produto (alvo)

O operador recebe, por **data/rodada**, recomendações do **agente OddWise**:

- Qual(is) jogo(s) das 6 ligas têm **maior probabilidade modelada** de acerto;
- Em **qual mercado** apostar naquele jogo: gols (Over 2.5, BTTS, outras linhas), vitória mandante/visitante, empate — conforme modelo e odds disponíveis;
- **Parecer** em linguagem natural (fase, médias, contexto), com **revisão humana** antes de virar aposta registrada.

**Não é meta da Phase 1:** substituir o operador sem backtest nem volume histórico local.

### Regra de negócio

> Sugerir o **jogo + mercado** com maior **probabilidade modelada** (e, quando houver odd, **EV+ > 0**), não apenas Over 2.5.

**Piloto narrow-and-deep (v1 do agente):** Brasileirão + Champions League, ou apenas mercados com backtest positivo no histórico local.

---

## Estado atual (maio/2026)

| Capacidade | Hoje | Alvo |
|------------|------|------|
| Sync 6 ligas + país | ✅ | ✅ |
| Odds capturadas (The Odds API) | ✅ totals / Over 2.5 | + 1X2 quando API permitir |
| `MatchStats` (forma, médias, tabela) | ✅ após **enrich** (`POST /api/admin/enrich/fixtures`) | Manter rotina pós-sync |
| EV+ (`/api/analysis/value-bets`) | ✅ com `statsIncomplete` + hint | Requer enrich + odds |
| Modelo 1X2 / empate | Non-goal Phase 1 (ADR-003) | Estratégia + backtest (1.76) |
| Modelo BTTS | Config only | Idem (1.76) |
| Melhor mercado por jogo | Não | Pick engine (1.77) |
| Fila propostas semanais | Rascunho 1.8 | Após 1.75 + 1.77 |
| Agente LLM + MCP | Não | 1.85 |
| n8n semanal | Phase 2 (uma linha no charter) | 2.0 |

**Operação válida hoje:** sync → capturar odds → **Nova sugestão manual** (Over 2.5) → liquidar → backtest (com histórico).

---

## Evolução: hoje → próximo → alvo

```
Hoje              Próximo (doc + build)           Alvo
────              ─────────────────────           ────
Over 2.5 only     Match Intelligence (1.75)       Agente multi-mercado
EV+ vazio         Modelos por mercado (1.76)      “Melhor aposta da rodada”
Manual            Pick ranking (1.77)             % por mercado + parecer
                  Propostas 3–5/sem (1.8)
React             MCP tools (1.85)                Você revisa e aceita
                  n8n (2.0)
```

**Princípio (charter):** dados → **features reais** → backtest por mercado → automação/agente. Não pular direto para “agente que sugere tudo”.

---

## Roadmap revisado (1.75 → 2.0)

| # | Fase | Entrega | Por que nesta ordem |
|---|------|---------|---------------------|
| 1 | **1.75 — Match Intelligence** | Médias de gols, forma, posição (API-Football); UI detalhe partida | Sem stats, qualquer % é chute |
| 2 | **1.76 — Multi-market engine** | Modelos separados: Over 2.5, BTTS, 1X2 — score + confiança cada um | “Quem ganha” ≠ “muitos gols” |
| 3 | **1.77 — Pick of the day** | Ranquear jogos da data + `bestPick` por jogo | Responde “onde tenho mais chance” |
| 4 | **1.8 — Propostas semanais** | Fila accept/reject; 3–5/semana | Automação com humano no loop |
| 5 | **1.85 — Analyst Agent** | LLM + MCP: narrativa sobre JSON do motor | Explica; não inventa odds |
| 6 | **2.0 — n8n** | sync → enrich → picks → notificar | Scheduler do charter |

**Phase 1.8** depende de **1.75 + 1.77** — ver [PHASE_1_8_DRAFT.md](PHASE_1_8_DRAFT.md).

**BTTS / 1X2:** incluir em 1.76 somente com **backtest por mercado/liga**; ADR-003 mantém Over 2.5 como operação principal até validação.

---

## Contrato de API (alvo — 1.77+)

### Entrada

- Partidas do dia (6 ligas, `SupportedLeague`);
- Stats enriquecidos (1.75);
- Odds capturadas (totals; 1X2 quando disponível).

### Saída (exemplo por partida)

```json
{
  "matchId": "uuid",
  "home": "Paris Saint Germain",
  "away": "Arsenal",
  "recommendations": [
    { "market": "OVER_2_5", "probability": 0.58, "ev": 0.06, "odd": 2.07, "confidence": 72 },
    { "market": "HOME_WIN", "probability": 0.42, "ev": -0.03, "odd": 2.40, "confidence": 55 },
    { "market": "DRAW", "probability": 0.22, "ev": null, "odd": 3.50, "confidence": 40 }
  ],
  "bestPick": { "market": "OVER_2_5", "reason": "Maior prob. modelada com EV+ positivo." },
  "narrative": null
}
```

Endpoints planejados:

- `GET /api/analysis/match/{matchId}/picks`
- `GET /api/analysis/round-picks?date=YYYY-MM-DD&league=`

`narrative` preenchido pelo **Analyst Agent (1.85)**, não pelo motor numérico.

---

## Arquitetura

| Camada | Papel |
|--------|--------|
| **Backend (Spring)** | Fonte da verdade: enrich, modelos, picks, propostas, liquidação |
| **React** | Operador: rodada, aceitar proposta, relatórios |
| **MCP** | Tools sobre REST OddWise (sem duplicar regras) |
| **Agente (LLM)** | Parecer técnico + síntese; usa tools |
| **RAG** | Charter, critérios, histórico de sugestões — contexto estável |
| **n8n** | Orquestração temporal (Phase 2.0) |

```
Backend API  ←── React UI
     ↑
     └── MCP Server (tools)
              ↑
         Agente + skills (+ RAG opcional)
              ↑
         n8n (cron semanal) ──► notificação
```

---

## MCP tools (piloto 1.85)

| Tool | Descrição |
|------|-----------|
| `oddwise_list_matches` | `date`, `league?` |
| `oddwise_enrich_match` | Dispara/atualiza stats (ou automático pós-sync) |
| `oddwise_get_match_picks` | Contrato JSON acima |
| `oddwise_get_round_picks` | Ranking da rodada |
| `oddwise_capture_odds` | Igual botão na UI |
| `oddwise_propose_suggestion` | Cria proposta (pré-1.8) |
| `oddwise_accept_proposal` | Aceita fila 1.8 |

---

## Non-goals (até 1.85)

- Multi-mercado em produção **sem** backtest por mercado.
- Agente inventar odds/placar **sem** tool.
- Winner em ligas fora das 6.
- Auto-aposta sem revisão humana.
- Substituir backtest por “opinião” do LLM.

---

## Checklist implementação (próximo sprint)

- [x] **1.75** `EnrichMatchAnalysisService` + popular `match_stats` da API-Football (Sprint 10)
- [x] **Sprint 11** `MatchInsightsService` + EV+ hub (oportunidades + radar rodada)
- [x] UI: detalhe partida sem TBD/0 após enrich
- [ ] **1.76** estratégias BTTS / 1X2 + liquidação por mercado
- [ ] **1.77** `PickRankingService` + endpoints picks
- [x] EV+ usa stats reais + meta `statsIncomplete`
- [ ] **1.8** entidades `PROPOSED` + UI fila
- [ ] **1.85** MCP server + prompt Analyst
- [ ] **2.0** workflow n8n documentado em OPERATIONS.md

---

*Revisar após cada sprint. ADR-013 registra decisão de roadmap.*
