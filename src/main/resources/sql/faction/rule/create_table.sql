CREATE TABLE IF NOT EXISTS faction_rule (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  `index` INT NOT NULL,
  rule VARCHAR(255) NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (faction_id, `index`)
)