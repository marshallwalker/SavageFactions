CREATE TABLE IF NOT EXISTS faction_relation (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  target_id VARCHAR(255) NOT NULL,
  relation VARCHAR(255) NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (faction_id, target_id)
)