CREATE TABLE IF NOT EXISTS faction_announcement (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  faction_id VARCHAR(36) NOT NULL,
  player_id VARCHAR(36) NOT NULL,
  message VARCHAR(255) NOT NULL
)