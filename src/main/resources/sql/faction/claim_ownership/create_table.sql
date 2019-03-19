CREATE TABLE IF NOT EXISTS faction_claim_ownership (
  uniqueId INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR (36) NOT NULL,
  player_id VARCHAR(36) NOT NULL,
  world_name VARCHAR (255) NOT NULL,
  chunk_x DOUBLE NOT NULL,
  chunk_z DOUBLE NOT NULL
)