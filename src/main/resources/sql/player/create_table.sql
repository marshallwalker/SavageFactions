CREATE TABLE IF NOT EXISTS faction_player (
  uniqueId INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  player_id VARCHAR(36) NOT NULL,
  faction_id VARCHAR(36) NOT NULL,
  role VARCHAR(255) NOT NULL,
  title VARCHAR(255),
  power DOUBLE DEFAULT 0.0,
  power_boost DOUBLE DEFAULT 0.0,
  last_power_update_time BIGINT,
  last_login_time BIGINT,
  chat_mode VARCHAR (255),
  ignore_alliance_chat BOOLEAN DEFAULT FALSE,
  `name` VARCHAR(16),
  monitor_joins BOOLEAN DEFAULT FALSE,
  spying_chat BOOLEAN DEFAULT FALSE,
  show_scoreboard BOOLEAN DEFAULT FALSE,
  warmup_task INT DEFAULT 0,
  is_admin_bypassing BOOLEAN DEFAULT FALSE,
  will_auto_leave BOOLEAN DEFAULT FALSE,
  map_height INT DEFAULT 17,
  is_flying BOOLEAN DEFAULT TRUE,
  is_stealth_enabled BOOLEAN DEFAULT FALSE,
  inspect_mode BOOLEAN DEFAULT FALSE
)