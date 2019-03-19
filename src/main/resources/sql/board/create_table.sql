CREATE TABLE IF NOT EXISTS faction_board (
  uniqueId INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  world_name VARCHAR(255) NOT NULL,
  chunk_x INT NOT NULL,
  chunk_z INT NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (world_name, chunk_x, chunk_z)
)