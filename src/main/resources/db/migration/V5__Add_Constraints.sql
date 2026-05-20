-- Constraints já foram adicionadas nas definições de tabela (UNIQUE, NOT NULL, FK)
-- Poderíamos adicionar mais constraints aqui se necessário, como checks de gols não negativos.
ALTER TABLE matches ADD CONSTRAINT chk_home_goals_non_negative CHECK (home_goals >= 0);
ALTER TABLE matches ADD CONSTRAINT chk_away_goals_non_negative CHECK (away_goals >= 0);