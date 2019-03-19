CREATE TABLE IF NOT EXISTS faction_warp (
  uniqueId INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  warp_name VARCHAR(255) NOT NULL,
  warp_password VARCHAR(255) NOT NULL DEFAULT "",
  world_name VARCHAR(255) NOT NULL,
  x DOUBLE NOT NULL,
  y DOUBLE NOT NULL,
  z DOUBLE NOT NULL,
  yaw FLOAT NOT NULL,
  pitch FLOAT NOT NULL,
  CONSTRAINT unique_object_param UNIQUE (faction_id, warp_name)
)