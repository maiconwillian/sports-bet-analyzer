# Sprint 10 — Phase 1.75 Match Intelligence

**Data:** 2026-05-26  
**Espelho UI:** [../frontend/SPRINT_10.md](../frontend/SPRINT_10.md)

## Endpoints

| Método | Path | Uso |
|--------|------|-----|
| POST | `/api/admin/enrich/fixtures?date=YYYY-MM-DD` | Enriquecer todas as partidas syncadas da data |
| POST | `/api/matches/{id}/enrich` | Enriquecer uma partida |
| GET | `/api/matches/{id}/analysis` | Stats + `modelInsight` (confiança/EV do motor) |
| GET | `/api/analysis/value-bets?date=&league=` | Oportunidades + meta `statsIncomplete` |
| GET | `/api/analysis/value-bets/match/{matchId}` | Oportunidade única (404 se não passar filtros) |

## Config

```yaml
app:
  enrich:
    auto-after-sync: false   # Opção B recomendada (quota API-Football)
    last-fixtures-for-form: 10
```

## Testes

```bash
./mvnw test -Dtest=EnrichMatchAnalysisServiceTest,ValueBetDetectionServiceTest
```

`ValueBetDetectionServiceTest`: stats mock → `combinedGoalAverage` 3.0 → oportunidade com confiança 75% e EV ≥ 5%.

## ADR

**ADR-014** — enrich via endpoint admin (não automático no sync por padrão). Ver [DECISIONS.md](DECISIONS.md).
