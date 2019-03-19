CREATE TABLE IF NOT EXISTS faction_upgrade (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  `key` VARCHAR (31) NOT NULL,
  `value`  INT NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (faction_id, `key`)
)