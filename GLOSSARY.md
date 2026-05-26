# OddWise — Glossário

Termos de domínio usados no backend, frontend e documentação.

---

## Apostas e métricas

| Termo | Significado |
|-------|-------------|
| **EV (Expected Value)** | Valor esperado da aposta. EV+ = aposta com valor estatístico positivo no longo prazo. |
| **CLV (Closing Line Value)** | Diferença entre odd capturada e odd de fechamento. CLV+ indica edge sobre o mercado. |
| **ROI** | Return on Investment — retorno sobre stake investido. |
| **Drawdown** | Queda máxima do bankroll em relação ao pico. Meta Phase 1: < 15%. |
| **Winrate** | Percentual de apostas ganhas. Secundário a EV+ no sistema. |
| **Kelly Criterion** | Fórmula de sizing de stake baseada em edge e odd. Phase 1.5c. |
| **VOID** | Aposta anulada (jogo cancelado, regra da casa, etc.). |
| **Stake** | Valor apostado em cada pick. |

---

## Entidades e enums

| Termo | Significado |
|-------|-------------|
| **SupportedLeague** | Enum das 6 ligas operacionais com score de qualidade e key The Odds API. |
| **SupportedMarket** | Mercados permitidos (Over 2.5 principal; BTTS configurado). |
| **MatchFeatureContext** | DTO com features calculadas para análise Over 2.5. |
| **MatchAnalysisSnapshot** | Snapshot versionado da análise (JSONB) para auditoria. |
| **StrategyResult** | Resultado da avaliação de uma estratégia para uma partida. |
| **actualResult** | Campo do PATCH de resultado: `WIN`, `LOSS` ou `VOID`. Não confundir com `status` interno. |

---

## Status de partida

| Status | Significado |
|--------|-------------|
| **NS** | Not Started — jogo não iniciado |
| **TBD** | To Be Determined |
| **LIVE / HT / 1H / 2H / ET** | Jogo em andamento (ver `IN_PLAY_MATCH_STATUSES` no front) |
| **FT** | Full Time — jogo encerrado |

---

## Integrações

| Termo | Significado |
|-------|-------------|
| **API-Football** | Fonte de fixtures, stats e resultados. |
| **The Odds API** | Fonte de odds de mercado; sport keys por liga (`soccer_epl`, etc.). |
| **regions us,eu** | Parâmetro The Odds API para bookmakers US + EU. |

---

## Arquitetura

| Termo | Significado |
|-------|-------------|
| **Clean Architecture** | Camadas: controller → application → domain → infrastructure. |
| **DTO** | Data Transfer Object exposto na API; nunca expor entidades JPA diretamente. |
| **Flyway** | Migrations de schema PostgreSQL. |

---

## Fases do produto

| Fase | Foco |
|------|------|
| **1.5c** | Value Bet Detection + Kelly |
| **1.7** | Backtesting V1 (FIXED_STAKE) |
| **1.8** | Automação 3–5 sugestões/semana |
| **2+** | n8n, Redis, Telegram, cloud |
