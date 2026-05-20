# 🎯 Sports Bet Analyzer - PROJECT CHARTER

**Última Atualização:** 2026-05-20
**Status:** ✅ Phase 1.2 - Persistence + Historical Data (Concluído)
**Versão:** 2.2

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
* **PostgreSQL 16** - Primary database (histórico = ouro)
* **Redis** - Cache + rate limit (Futuro Phase 3)
* **MongoDB** - IA + histórico RAW (Futuro Phase 4)

### Integrações Externas
* **API-Football** - Stats + Fixtures ✅ **COMPLETO (Phase 1.1)**
* **Persistence + Historical Data** ✅ **COMPLETO (Phase 1.2)**
* **The Odds API** - Odds + Market Data 🔄 **A INICIAR (Phase 1.3)**
* **n8n** - Automação + Scheduler (Futuro Phase 2)
* **Telegram Bot API** - Notificações (Futuro Phase 5)

### Infraestrutura
* **Docker** + **Docker Compose** - Containerização
* **GitHub** - Versionamento
* **GitHub Actions** - CI/CD (Futuro)
* **PostgreSQL 16** - Local + Production

### Padrões Arquiteturais
* **Clean Architecture** - Separação clara de camadas
* **SOLID Principles** - Código robusto
* **Modular Monolith** - Pronto para evoluir
* **Domain-Oriented Design** - Negócio-centric
* **Event-Driven Ready** - Para kafka/rabbitmq futuro

---

## 🧱 ARQUITETURA CORE
