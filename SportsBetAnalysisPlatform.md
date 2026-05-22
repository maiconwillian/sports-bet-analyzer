# 🎯 Sports Bet Analyzer - PROJECT CHARTER

**Última Atualização:** 2026-05-20
**Status:** 🚀 Phase 1.6 - Strategy Engine (COMPLETO) ✅ TESTADO
**Versão:** 2.6 (Strategy Engine implementada com Records e Lógica Avançada)

---

## 📊 ROADMAP DE IMPLEMENTAÇÃO

### ✅ CONCLUÍDO (Sprints 1-5):

AGORA (Sprint 1-2):
├── Phase 1.5a — Feature Layer Básica ✅
│   ├── MatchFeatureContext DTO ✅
│   ├── Over25FeatureExtractor ✅
│   ├── FeatureCalculationService ✅
│   └── @Mapping em MatchStats → Features ✅

DEPOIS (Sprint 3-4):
├── Phase 1.5b — Analysis Snapshot ✅
│   ├── MatchAnalysisSnapshot (entidade JSONB) ✅
│   ├── AnalysisSnapshotService (serialização) ✅
│   ├── Salvar análise versionada ✅
│   ├── Auditoria de decisões ✅
│   └── Testes E2E completos ✅

DEPOIS (Sprint 5-6):
├── Phase 1.6 — Strategy Engine ✅
│   ├── BettingStrategy interface ✅
│   ├── Over25Strategy implementation (Lógica Avançada) ✅
│   ├── StrategyResult (Record Java 21) ✅
│   ├── StrategyEvaluationService (Multi-strategy support) ✅
│   └── Testes Unitários de Estratégia ✅

---

### 🔄 PRÓXIMO (Sprints 7-8):

DEPOIS (Sprint 7-8):
├── Phase 1.7 — Backtesting 🔄 PRÓXIMA
│   ├── HistoricalReplay engine
│   ├── BacktestResults aggregation
│   ├── Strategy comparison by ROI
│   └── Performance metrics (Sharpe, Sortino, etc)

DEPOIS (Sprint 9-10):
├── Phase 1.4 — The Odds API Integration
│   ├── Integração com The Odds API
│   ├── CLV (Closing Line Value) calculation
│   ├── Market movement tracking
│   └── Odds arbitrage detection

FUTURO (Phase 2+):
├── Phase 2 — n8n Automation
├── Phase 3 — Redis Cache + Rate Limit
├── Phase 4 — MongoDB (Raw History)
├── Phase 5 — Telegram Bot Notifications

---

## 📌 VISÃO

Plataforma backend desenvolvida em Java 21 focada em análise quantitativa de apostas esportivas, identificação de value bets e validação estatística de estratégias através de histórico, automação e machine learning.

O sistema terá arquitetura enterprise-grade, orientada a dados, com foco em escalabilidade, rastreabilidade e evolução contínua dos modelos analíticos.

---

## 🎯 OBJETIVO FINAL

Gerar de forma automatizada entre 3 e 5 sugestões de apostas semanais com expectativa de ROI positivo e sustentável no longo prazo.

### Metas Iniciais:
* ROI mensal > 8%
* Drawdown máximo < 15%
* Winrate sustentável > 55%
* CLV (Closing Line Value) positivo
* Processo 100% automatizado

---

## 🧠 FILOSOFIA DO SISTEMA

* **Data-driven decisions** - Dados acima de opinião
* **Histórico acima de opinião** - Backtest obrigatório
* **EV+ acima de winrate** - Expected value é a métrica real
* **Automação first** - Sem emoção, sem erro humano
* **Estratégias testáveis** - Cada estratégia tem ROI documentado
* **Backtesting obrigatório** - Valida antes de usar
* **Gestão de risco integrada** - Kelly Criterion + Money Management
* **Consistência acima de lucro rápido** - Longo prazo

---

## 🛠 STACK TECH

### Backend
* **Java 21 LTS** - Latest stable, records, pattern matching
* **Spring Boot 3.3.x** - Latest stable
* **Maven** - Build management
* **Lombok** - Reduce boilerplate
* **MapStruct** - DTO mapping

### Banco de Dados
* **PostgreSQL 16** - Primary database (histórico = ouro) ✅ **ATIVO**
* **Flyway** - Database migrations ✅ **ATIVO**
* **Redis** - Cache + rate limit (Futuro Phase 3)
* **MongoDB** - IA + histórico RAW (Futuro Phase 4)

### Integrações Externas
* **API-Football** - Stats + Fixtures ✅ **INTEGRADO (Phase 1.1)**
* **The Odds API** - Odds + Market Data (Futuro Phase 1.4)
* **n8n** - Automação + Scheduler (Futuro Phase 2)
* **Telegram Bot API** - Notificações (Futuro Phase 5)

### Infraestrutura
* **Docker** + **Docker Compose** - Containerização ✅ **ATIVO**
* **GitHub** - Versionamento
* **GitHub Actions** - CI/CD (Futuro)
* **PostgreSQL 16** - Local + Production ✅ **ATIVO**

### Padrões Arquiteturais
* **Clean Architecture** - Separação clara de camadas ✅ **IMPLEMENTADO**
* **SOLID Principles** - Código robusto ✅ **IMPLEMENTADO**
* **Modular Monolith** - Pronto para evoluir ✅ **IMPLEMENTADO**
* **Domain-Oriented Design** - Negócio-centric ✅ **IMPLEMENTADO**
* **Event-Driven Ready** - Para kafka/rabbitmq futuro


## 🔥 FILOSOFIA: NARROW AND DEEP, NÃO BROAD AND SHALLOW

### Princípio Core
Em vez de tentar analisar TUDO, **especializamos em NICHOS**:

✅ **3 Ligas APENAS:**
- Brasileirão Série A (conhecimento local + 90 dados)
- Premier League (histórico rico + 98 qualidade)
- Champions League (relevância + 97 qualidade)

✅ **1 Mercado INICIAL:**
- Over 2.5 Goals (95% modelável, estatístico)
- (Futuro: BTTS - 92% modelável)

❌ **JAMAIS:**
- Analisar "Winner" (muito emocional, zebra destrói modelo)
- Analisar TODOS os mercados ao mesmo tempo
- Misturar Série B com Série A
- Incluir amistosos/pré-temporada

### Por quê?
- **Menos ruído** = Padrões mais claros
- **Menos API calls** = Mais barato
- **Modelo melhor** = ROI mais alto
- **Menos bugs** = Menos variáveis

### Implementação
- `SupportedLeague` enum (com data quality score)
- `SupportedMarket` enum (com reasoning)
- `DataQualityValidator` (filtra amistosos, ligas obscuras)
- Cada liga tem threshold mínimo de 85 de qualidade

---

## 🧪 TESTES E2E (Validação Completa)

### Fluxo Testado com Sucesso ✅
