# OddWise — Operações (rotina local)

Guia operacional semanal para uso da plataforma em ambiente local.

**Pré-requisitos:** Docker (Postgres), backend `:8080`, frontend `:5173`.  
**Charter:** [SportsBetAnalysisPlatform.md](SportsBetAnalysisPlatform.md)

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

### 2. Captura de odds

- Abrir partida relevante (NS, LIVE ou pré-jogo)
- `POST /api/odds/capture/{matchId}` ou botão na UI
- Confirmar histórico em `/odds` ou detalhe da partida
- Preferir bookmakers líquidos (ex.: Pinnacle)

### 3. Nova sugestão (manual)

Fluxo UI: `/suggestions/new`

1. Selecionar liga suportada
2. Escolher partida elegível (NS, TBD, LIVE, HT, ONE_H, TWO_H)
3. Selecionar odd capturada (combobox: odd — bookmaker — mercado)
4. Preencher EV, confiança, stake, notas
5. Salvar

### 4. Acompanhar resultados

- Após jogo: marcar **WIN**, **LOSS** ou **VOID**
- API: `PATCH /api/suggestions/{id}/result` com `{ "actualResult": "..." }`
- Conferir relatórios em `/reports`

### 5. Backtest antes de escalar

- UI: `/backtesting`
- Escolher liga, período, versão da estratégia, stake fixo
- Analisar ROI, drawdown, winrate
- **Só aumentar stake ou expandir ligas** se backtest com volume real for positivo

---

## Checklist pré-aposta

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

---

## Quotas de API

- API-Football: sync sob demanda; evitar sync de ligas não suportadas (Phase 1.9)
- The Odds API: uma captura por partida quando necessário; monitorar uso mensal
