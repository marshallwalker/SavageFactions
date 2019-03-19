CREATE TABLE IF NOT EXISTS faction_chest (
  faction_id VARCHAR(36) NOT NULL,
  `index` INT NOT NULL,
  item VARCHAR(511) NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (faction_id, `index`)
)