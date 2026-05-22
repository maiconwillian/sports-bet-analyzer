# Resumo atual do projeto — Sports Bet Analyzer
O projeto hoje já está em um estado bem interessante: você saiu de um CRUD/API simples e chegou em uma plataforma local de análise quantitativa com **ingestão de partidas**, **filtro de qualidade**, **captura de odds**, **estratégia Over 2.5**, **histórico de odds**, e agora **backtesting**. **CLV**
Considerando que:``` text
✅ APIs externas estão em modo free
✅ uso será local/pessoal
✅ objetivo não é publicar/SaaS
✅ foco é gerar decisão melhor para você mesmo
```

A arquitetura atual está adequada. Não precisa “enterprise demais” agora. O foco certo é: coletar bons dados, evitar ruído, testar estratégia e medir resultado.
 
1. Estado geral do projeto
Status atual``` text
Phase 1.5a — Feature Layer Básica ✅
Phase 1.5b — Analysis Snapshot ✅
Phase 1.6 — Strategy Engine ✅
Phase 1.4 — The Odds API Integration ✅
Phase 1.7 — Backtesting Engine ✅
Phase 1.5c — Value Bet Detection 🔄 Próxima
```

Em termos práticos, o sistema hoje consegue:``` text
1. Sincronizar partidas via API-Football
2. Salvar ligas, times e partidas
3. Filtrar ligas/partidas de melhor qualidade
4. Criar/usar MatchStats
5. Calcular features para Over 2.5
6. Executar estratégia
7. Capturar odds reais da The Odds API
8. Salvar histórico de odds
9. Calcular CLV
10. Simular apostas históricas via backtesting
11. Medir ROI, winrate, drawdown e profit factor
```

Isso já é uma base real de análise.
 
2. Arquitetura atual
A arquitetura está seguindo um estilo próximo de Clean Architecture / layered architecture.
Camadas principais``` text
controller
    ↓
application / services
    ↓
domain
    ↓
infrastructure
    ↓
database / APIs externas
```


2.1 Controller Layer
Responsável por expor endpoints HTTP.
Principais controllers:``` text
MatchController
OddsController
BacktestingController
```

Função
Recebem request, chamam services e devolvem DTOs.
Exemplo de responsabilidades:``` text
MatchController      → CRUD/listagem de partidas
OddsController       → captura, histórico, CLV e odds manuais
BacktestingController → execução de backtest
```


2.2 Application Layer
É onde está a maior parte da lógica do sistema.
Principais services:``` text
MatchService
FixtureSyncService
OddsService
OddsHistoryService
FeatureCalculationService
StrategyEvaluationService
AnalysisSnapshotService
BacktestingService
HistoricalReplayService
MetricsCalculationService
DataQualityValidator
```

Função
Essa camada orquestra:``` text
sincronização
validação
feature engineering
estratégias
odds
backtesting
métricas
```


2.3 Domain Layer
Representa as entidades e conceitos principais do negócio.
Principais entidades/modelos:``` text
Match
League
Team
Odds
MatchStats
BetSuggestion
MatchAnalysisSnapshot
```

Principais enums/conceitos:``` text
MatchStatus
SupportedLeague
SupportedMarket
BacktestBetStatus
SimulationMode
```

Função
Modela o coração do sistema:``` text
partida
liga
time
odd
estatísticas
mercado
estratégia
resultado
```

 
2.4 Infrastructure Layer
Responsável por comunicação externa e persistência.
Principais peças:``` text
ApiFootballClient
TheOddsApiClient
Repositories Spring Data JPA
Flyway migrations
PostgreSQL
Docker Compose
```

Função
Lida com:``` text
API-Football
The Odds API
PostgreSQL
consultas JPA
persistência
```

 
3. APIs externas e modo free
Você está usando APIs em modo free, então o projeto precisa respeitar limitações de chamadas e dados.
API-Football
Usada para:``` text
fixtures
ligas
times
dados de partidas
possivelmente estatísticas
```

Papel no sistema
A API-Football alimenta a base com:``` text
partidas
times
ligas
status dos jogos
placares
```

Limitação do modo free
Provavelmente você terá:``` text
limite diário/mensal de chamadas
restrição de endpoints
restrição de histórico
possível limitação de estatísticas detalhadas
```

Estratégia correta para uso local
Para uso local, o ideal é:``` text
sincronizar pouco
guardar tudo no banco
evitar chamar API repetidamente
usar ranges pequenos
priorizar ligas suportadas
```

 
The Odds API
Usada para:``` text
capturar odds reais
capturar mercado totals/Over 2.5
salvar histórico
calcular CLV quando possível
```

Limitação do modo free
Atenção especial:``` text
odds históricas podem ser limitadas
janela de dados pode ser curta
calls podem ser caras em quota
closing odds nem sempre estarão disponíveis
```

Por isso a decisão que você tomou está correta:``` text
CLV é opcional
closingOdd pode ser null
backtest usa apenas odds capturadas antes da partida
```


4. Banco de dados e entidades principais
   PostgreSQL
   O banco é o coração do projeto porque, em modo free, você precisa guardar histórico localmente.
   A ideia é:``` text
   API externa → coleta limitada → persistência local → análise offline
```

Esse é o caminho certo.
 
Entidades principais
League
Representa a liga.
Usada para:``` text
filtrar partidas
mapear SupportedLeague
separar país
evitar confusão de ligas com nomes parecidos
```


Team
Representa os times.
Usado para:``` text
homeTeam
awayTeam
normalização de partida
estatísticas
features
```

 
Match
Representa a partida.
Campos importantes:``` text
apiId
league
homeTeam
awayTeam
homeGoals
awayGoals
matchDate
status
venue
referee
```

É a entidade central do sistema.

Odds
Representa uma odd capturada.
Campos importantes:``` text
match
bookmaker
bookmakerKey
market
oddsValue
capturedAt
```

Essa entidade é essencial para:``` text
backtesting
CLV
histórico
value betting
```


MatchStats
Representa estatísticas usadas para features.
Hoje é uma das partes mais sensíveis.
Por quê?
Porque para backtesting perfeito, MatchStats precisa representar o estado antes da partida.
Se ele for atualizado depois com dados pós-jogo, pode contaminar o backtest.

5. Fluxo principal do projeto
   Fluxo 1 — Sincronizar partidas``` text
   API-Football
   ↓
   ApiFootballClient
   ↓
   FixtureSyncService / MatchService
   ↓
   LeagueRepository / TeamRepository / MatchRepository
   ↓
   PostgreSQL
```

O que acontece
O sistema busca fixtures por data ou range, cria/atualiza:``` text
League
Team
Match
MatchStats inicial
```

Observação importante
Hoje o FixtureSyncService cria MatchStats placeholder:``` text
homeTeamGoalsAvg = 0.0
awayTeamGoalsAvg = 0.0
homeTeamForm = TBD
awayTeamForm = TBD
```

Isso é ok como estrutura, mas para gerar sugestão real precisa tomar cuidado para não usar stats fake como se fossem dados bons.
 
Fluxo 2 — Filtrar qualidade dos dados``` text
FixtureDTO
    ↓
DataQualityValidator
    ↓
SupportedLeague / SupportedMarket
    ↓
somente partidas úteis
```

O projeto segue a filosofia:``` text
narrow and deep
```

Ou seja:``` text
poucas ligas
poucos mercados
mais qualidade
menos ruído
```

Isso é muito correto para apostas.
Melhor analisar bem:``` text
Premier League
Brasileirão Série A
Champions League
Over 2.5
```

do que tentar analisar 80 ligas e 20 mercados.
 
Fluxo 3 — Calcular features``` text
Match
    ↓
MatchStats
    ↓
FeatureCalculationService
    ↓
MatchFeatureContextDTO
```

As features alimentam a estratégia.
Exemplos:``` text
média de gols do mandante
média de gols sofridos pelo visitante
pressão esperada de gols
forma dos times
```

 
Fluxo 4 — Rodar estratégia``` text
MatchFeatureContextDTO
    ↓
StrategyEvaluationService
    ↓
Over25Strategy
    ↓
StrategyResult
```

A estratégia retorna algo como:``` text
shouldBet
market
confidence
expectedValue
stake sugerida
reasoning
```

Isso permite explicar por que uma aposta seria feita.
 
Fluxo 5 — Capturar odds``` text
Match
    ↓
OddsService
    ↓
OddsHistoryService
    ↓
TheOddsApiClient
    ↓
The Odds API
    ↓
OddsRepository
    ↓
PostgreSQL
```

O sistema busca odds reais e salva histórico.
Para backtest e CLV, o mais importante é:``` text
capturedAt
market
oddsValue
bookmaker
```

 
Fluxo 6 — Calcular CLV``` text
Odds escolhida
    ↓
Odd final / closing odd
    ↓
OddsHistoryService.calculateCLV()
    ↓
CLVResponseDTO
```

O CLV é uma métrica de qualidade da decisão.
Interpretação:``` text
CLV positivo → você pegou uma odd melhor que o fechamento
CLV negativo → mercado fechou melhor que você
```

No modo free, nem sempre você terá closing odd confiável. Por isso o sistema corretamente trata CLV como opcional no backtesting.
 
Fluxo 7 — Backtesting
Esse é o fluxo mais importante adicionado agora.``` text
POST /api/backtesting/run
    ↓
BacktestingController
    ↓
BacktestingService
    ↓
buscar partidas no período/liga
    ↓
HistoricalReplayService
    ↓
FeatureCalculationService
    ↓
StrategyEvaluationService
    ↓
OddsRepository
    ↓
simular resultado
    ↓
MetricsCalculationService
    ↓
BacktestResultDTO
```

O que o backtest faz hoje
Ele:``` text
1. recebe período, liga, estratégia, stake e confiança mínima
2. valida datas
3. valida simulationMode
4. busca partidas
5. ignora partidas não finalizadas
6. ignora partidas sem placar
7. calcula features
8. executa estratégia
9. aplica filtro de confidence
10. busca odds pré-jogo
11. filtra odds capturadas antes da partida
12. normaliza mercado
13. simula WIN/LOSS
14. calcula profit/loss
15. calcula CLV quando possível
16. agrega métricas
```

 
6. Endpoints principais disponíveis
Matches
Base provável:``` http
GET /api/matches
GET /api/matches/{id}
POST /api/matches
PUT /api/matches/{id}
DELETE /api/matches/{id}
GET /api/matches/date/{date}
GET /api/matches/league/{league}
```

Uso
Consultar e gerenciar partidas.

Odds``` http
POST /api/odds/capture/{matchId}
GET /api/odds/history/{matchId}
GET /api/odds/{matchId}/clv?pickedOddId={id}&finalOddId={id}
GET /api/odds/match/{matchId}
GET /api/odds/match/{matchId}/latest
GET /api/odds/bookmaker/{bookmaker}
POST /api/odds
```

Uso
Capturar odds reais, consultar histórico e calcular CLV.
 
Backtesting``` http
POST /api/backtesting/run
```

Request exemplo:``` json
{
"startDate": "2025-01-01",
"endDate": "2025-12-31",
"league": "PREMIER_LEAGUE",
"strategyVersion": "OVER25_V1",
"stake": 10,
"minimumConfidence": 70,
"simulationMode": "FIXED_STAKE"
}
```

Resposta esperada:``` json
{
  "strategyVersion": "OVER25_V1",
  "matchesAnalyzed": 120,
  "betsPlaced": 38,
  "wins": 22,
  "losses": 16,
  "voids": 0,
  "winRate": 57.89,
  "roi": 12.45,
  "profit": 47.30,
  "maxDrawdown": 18.20,
  "averageOdd": 1.84,
  "averageEV": 6.8,
  "averageCLV": 2.1,
  "profitFactor": 1.42,
  "bets": []
}
```


7. O que podemos fazer hoje com o sistema
   Você já consegue fazer
1. Montar base local de partidas
   Usando API-Football, você consegue popular o banco com partidas por data/range.

2. Restringir análise para ligas de qualidade
   Com SupportedLeague e DataQualityValidator, você evita muito ruído.
   Isso é uma vantagem enorme.

3. Capturar odds reais
   Você pode capturar odds do mercado Over 2.5 para partidas futuras/próximas, dentro das limitações do plano free.

4. Criar histórico local de odds
   Esse é um dos ativos mais valiosos do projeto.
   Com o tempo, seu banco local vira mais importante que a API.

5. Calcular CLV
   Mesmo que limitado, você já consegue comparar odds quando tiver histórico suficiente.

6. Rodar backtest
   Você consegue simular:``` text
   Se eu tivesse seguido a estratégia X nesse período, com stake Y e confiança mínima Z, o que teria acontecido?
```

 
7. Comparar thresholds
Você pode rodar o mesmo backtest mudando:``` text
minimumConfidence = 60
minimumConfidence = 70
minimumConfidence = 80
minimumConfidence = 90
```

E comparar:``` text
ROI
betsPlaced
winrate
drawdown
profit factor
```

Isso já gera insights muito bons.
 
8. Insights que você pode tirar agora
Insight 1 — Se a estratégia aposta demais ou de menos
Pelo backtest:``` text
matchesAnalyzed vs betsPlaced
```

Exemplo:``` text
500 partidas analisadas
20 apostas feitas
```

Talvez o filtro esteja muito rígido.
Ou:``` text
500 partidas analisadas
350 apostas feitas
```

Talvez esteja muito permissivo.

Insight 2 — Confidence ideal
Rodando várias vezes com confidence diferente:``` text
60
65
70
75
80
85
90
```

Você consegue ver onde a estratégia performa melhor.
Possíveis conclusões:``` text
confidence 60 → muitas bets, ROI baixo
confidence 75 → menos bets, ROI melhor
confidence 90 → poucas bets, amostra pequena
```


Insight 3 — ROI vs Winrate
Você pode descobrir algo importante:``` text
winrate alto não significa lucro
```

Exemplo:``` text
Winrate: 58%
Odd média: 1.55
ROI: negativo
```

Ou:``` text
Winrate: 48%
Odd média: 2.25
ROI: positivo
```

Por isso ROI é mais importante que winrate.
 
Insight 4 — Drawdown realista
O maxDrawdown te mostra quanto a estratégia pode sofrer em sequência ruim.
Isso ajuda a definir:``` text
stake
banca necessária
risco psicológico
viabilidade prática
```

Para uso local, isso é muito útil.

Insight 5 — Profit Factor
Profit factor acima de 1 indica que o lucro bruto supera as perdas brutas.
Referência simples:``` text
< 1.0  → ruim
1.0    → break-even
1.2+   → interessante
1.5+   → bom
2.0+   → excelente, mas cuidado com amostra pequena
```

 
Insight 6 — CLV como métrica de qualidade
Se com o tempo você capturar odds cedo e depois perto do jogo, pode medir:``` text
averageCLV
```

Interpretação:``` text
CLV positivo recorrente → estratégia pode estar encontrando valor antes do mercado ajustar
CLV negativo recorrente → você está entrando tarde ou sem edge
```

Mesmo que o ROI varie, CLV positivo é um sinal de processo saudável.
 
Insight 7 — Ligas diferentes podem ter comportamento diferente
Com filtros por liga, você pode comparar:``` text
Premier League
Brasileirão Série A
Champions League
```

Talvez a estratégia Over 2.5 seja boa em uma e ruim em outra.
Isso é muito valioso.

9. Pontos fortes atuais
1. Escopo estreito
   Você não tentou fazer tudo.
   Foco atual:``` text
   Over 2.5
   poucas ligas
   dados filtrados
   backtest
   odds reais
```

Isso é o certo.
 
2. Histórico local
Como o plano é free, salvar tudo no banco local é essencial.
Você está construindo seu próprio dataset.
 
3. Backtesting chegou cedo
Muita gente cria estratégia antes de backtest.
Você já tem backtest na base, então consegue validar antes de confiar.
 
4. Separação boa de camadas
A arquitetura está suficientemente limpa para evoluir sem virar caos.
 
5. Consciência de data leakage
Você já tratou odds pós-jogo e documentou a limitação das features.
Isso mostra maturidade do projeto.

10. Fragilidades atuais
1. MatchStats ainda é o maior risco
A maior fragilidade hoje é:``` text
features podem não ser snapshot pré-jogo
```

Para um backtest perfeito, você precisa garantir:``` text
as estatísticas usadas eram conhecidas antes da partida
```

Esse é o principal ponto futuro.
 
2. Stats placeholder podem contaminar estratégia
O FixtureSyncService cria stats com:``` text
0.0
TBD
```

Se a estratégia usar isso sem validar, pode gerar decisão ruim.
Sugestão futura:``` text
marcar MatchStats como INCOMPLETE
ignorar match com stats incompletas
ou criar DataQualityFlag
```

 
3. Odds dependem do plano free
Você provavelmente não terá histórico amplo de odds via API free.
Então o valor virá de:``` text
capturar diariamente
salvar localmente
acumular dataset
```

Não tente depender da API para histórico grande.

4. Controller de odds ainda tem regra demais
   O OddsController ainda chama repository/mapper/history diretamente.
   Para uso local, não é grave.
   Mas futuramente pode centralizar tudo no OddsService.

5. Não há persistência de backtests
   Hoje o backtest retorna resultado em memória.
   Para uso local, ok.
   Mas seria útil no futuro salvar:``` text
   data da execução
   parâmetros usados
   resultado agregado
   bets individuais
```

Assim você compara versões da estratégia.
 
11. Melhor fluxo de uso local
Considerando APIs free e uso pessoal, eu usaria assim:
Rotina diária ou semanal
1. Sincronizar partidas relevantes``` text
Buscar partidas das próximas datas somente das ligas suportadas.
```

Evitar range gigante.

2. Capturar odds antes dos jogos
   Idealmente em dois momentos:``` text
   24h antes
   2h antes
```

Ou, se a quota for curta:``` text
uma vez por dia
```


3. Rodar estratégia
   Ver quais jogos passam no threshold.

4. Salvar/capturar odds
   Manter histórico local.

5. Depois dos jogos, atualizar resultados
   Sincronizar placar/status FT.

6. Rodar backtest mensal
   Comparar:``` text
   thresholds
   ligas
   períodos
   estratégias
```

 
12. Próximos passos recomendados
Próximo passo natural: Phase 1.5c — Value Bet Detection
Agora que você tem:``` text
strategy probability/confidence
odds reais
backtest
EV
CLV
```

O próximo passo é detectar:``` text
quando sua probabilidade estimada > probabilidade implícita do mercado
```

Fórmula base:``` text
marketProbability = 1 / odd
edge = estimatedProbability - marketProbability
EV = (estimatedProbability * odd) - 1
```

Se:``` text
EV > 0
edge > threshold
confidence >= minimum
```

Então:``` text
value bet candidata
```


Depois: Snapshot pré-jogo
Para melhorar a confiabilidade do backtest:``` text
MatchFeatureSnapshot
```

Salvar:``` text
matchId
featuresJson
calculatedAt
strategyVersion
dataAvailableUntil
```

Isso resolve o risco do MatchStats.

Depois: Persistir BacktestRun
Criar entidades:``` text
BacktestRun
BacktestRunBet
```

Salvar:``` text
request
resultado agregado
bets simuladas
data da execução
versão da strategy
```

Isso facilita comparar versões.

Depois: rotina local automatizada
Como você não pretende publicar, dá para fazer simples:``` text
Spring Scheduler
```

Em vez de n8n inicialmente.
Exemplo:``` text
08:00 sync fixtures
09:00 capture odds
18:00 capture odds novamente
23:00 sync results
domingo 23:30 gerar relatório semanal
```

Para uso local, isso talvez seja mais simples que n8n.

13. Minha leitura geral
    Você construiu uma base muito boa para um projeto pessoal/local.
    O projeto hoje não é “produto final”, mas já é um laboratório quantitativo de apostas.
    O mais importante é que ele tem três pilares certos:``` text
    dados locais
    estratégia testável
    backtesting
```

E está seguindo uma filosofia boa:``` text
menos ligas
menos mercados
mais qualidade
mais histórico
```

Esse é exatamente o caminho certo para não se perder.

14. Conclusão
    Estado atual:``` text
    ✅ arquitetura boa para uso local
    ✅ APIs integradas dentro das limitações free
    ✅ banco local vira ativo principal
    ✅ backtesting funcional
    ✅ odds pré-jogo protegidas
    ✅ CLV opcional bem tratado
    ✅ strategy engine operacional
    ✅ próximo passo deve ser Value Bet Detection
```
O principal cuidado daqui para frente:``` text
não expandir para muitas ligas/mercados cedo demais
não confiar em stats placeholder
não chamar API free sem necessidade
não interpretar backtest com amostra pequena como verdade absoluta
```

Minha recomendação prática:``` text
2. Rodar backtests com diferentes confidence thresholds
3. Começar Phase 1.5c Value Bet Detection
4. Criar rotina local de coleta diária
5. Acumular histórico por algumas semanas
6. Só depois pensar em refactor maior
```
