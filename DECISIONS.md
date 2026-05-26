# OddWise — Log de Decisões (ADR)

Decisões arquiteturais e de produto. Formato curto: **contexto → decisão → consequência**.

**Charter canônico:** [SportsBetAnalysisPlatform.md](SportsBetAnalysisPlatform.md)

---

## ADR-001 — Repos separados (backend + frontend)

**Data:** 2026-05  
**Contexto:** Monorepo vs repos independentes.  
**Decisão:** Dois repos Git separados.  
- Backend: `sports-bet-analyzer`  
- Frontend: `oddwise-frontend`  
**Consequência:** Charter canônico no backend; frontend espelha visão em `PROJECT_CHARTER.md`. Rules Cursor em cada repo.

---

## ADR-002 — Infra local-only na Phase 1

**Data:** 2026-05  
**Contexto:** Deploy Vercel/Render/Neon avaliado.  
**Decisão:** Manter 100% local (Docker Postgres + back `:8080` + front `:5173`).  
**Consequência:** Sem CI/CD cloud por enquanto; foco em acumular dados e validar estratégia.

---

## ADR-003 — Mercado Over 2.5 (não Winner)

**Data:** 2026-04  
**Contexto:** Escolha de mercado inicial modelável estatisticamente.  
**Decisão:** Foco em **Over 2.5 Goals**. Não analisar Winner na Phase 1.  
**Consequência:** Estratégia `Over25Strategy`, odds The Odds API filtradas para esse mercado.

---

## ADR-004 — 6 ligas cadastradas, operação estreita

**Data:** 2026-05  
**Contexto:** Expandir cobertura vs manter narrow-and-deep.  
**Decisão:** Cadastrar **6 ligas** em `SupportedLeague` para dados/odds; **operar subset** validado por backtest (ex.: Premier + Champions + Brasileirão).  
**Consequência:** Sync pode trazer mais ligas; apostas só onde backtest for positivo.

---

## ADR-005 — The Odds API com regions us,eu

**Data:** 2026-05  
**Contexto:** Cobertura de bookmakers (Pinnacle, etc.).  
**Decisão:** `api.odds.regions: us,eu` em `application.yml`.  
**Consequência:** Mais bookmakers disponíveis; consumo de quota ligeiramente maior.

---

## ADR-006 — Liga por nome + país (API-Football)

**Data:** 2026-05  
**Contexto:** "Serie A" existe no Brasil e na Itália.  
**Decisão:** `SupportedLeague.matches(name, country)` + `leagueCountry` no DTO.  
**Consequência:** Badge e filtros corretos; nunca mapear liga só pelo nome.

---

## ADR-007 — Resultado de sugestão via `actualResult`

**Data:** 2026-05  
**Contexto:** Front enviava `{ status }`; backend esperava `{ actualResult }`.  
**Decisão:** Contrato oficial: `PATCH /api/suggestions/{id}/result` com `{ "actualResult": "WIN" | "LOSS" | "VOID" }`.  
**Consequência:** UI e testes devem usar `actualResult`, não `status`.

---

## ADR-008 — Sugestões manuais na Phase 1

**Data:** 2026-05  
**Contexto:** Meta de 3–5 sugestões/semana automatizadas.  
**Decisão:** Phase 1 opera com fluxo **manual** (liga → partida → odd → sugestão). Automação = Phase 1.8.  
**Consequência:** UI de nova sugestão é crítica; value bet detection vem antes da automação.

---

## ADR-013 — Roadmap agente multi-mercado (MCP + picks)

**Data:** 2026-05-26  
**Contexto:** EV+ vazio com `MatchStats` 0/TBD; visão de agente que sugere jogo + mercado (gols, vitória, empate) com parecer técnico.  
**Decisão:**

- **Phase 1** mantém operação **Over 2.5** manual (ADR-003).
- Alvo documentado em [PRODUCT_VISION_AGENT.md](PRODUCT_VISION_AGENT.md): ordem **1.75 → 1.76 → 1.77 → 1.8 → 1.85 → 2.0 (n8n)**.
- **1.8** (`generate-weekly`) só após **1.75** (stats reais) e **1.77** (pick ranking).
- **Backend = fonte da verdade**; MCP expõe tools REST; LLM não substitui motor numérico nem inventa odds.
- BTTS / 1X2 em **1.76** exigem backtest por mercado antes de operar.

**Consequência:** Implementar LLM/MCP antes de enrich stats é non-goal; espelhar ADR no frontend `DECISIONS.md`.

---

## Ritual de manutenção

Após cada sprint relevante:

1. Nova ADR se houve decisão irreversível ou importante.
2. Atualizar status no [SportsBetAnalysisPlatform.md](SportsBetAnalysisPlatform.md).
3. Espelhar ADRs relevantes no frontend `DECISIONS.md`.
