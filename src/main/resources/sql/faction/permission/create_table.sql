CREATE TABLE IF NOT EXISTS faction_permission_%s (
  faction_id VARCHAR(36) NOT NULL PRIMARY KEY,
  ban VARCHAR (31) NOT NULL,
  build VARCHAR (31) NOT NULL,
  destroy VARCHAR (31) NOT NULL,
  frost_walk VARCHAR (31) NOT NULL,
  pain_build VARCHAR (31) NOT NULL,
  door VARCHAR (31) NOT NULL,
  button VARCHAR (31) NOT NULL,
  lever VARCHAR (31) NOT NULL,
  container VARCHAR (31) NOT NULL,
  invite VARCHAR (31) NOT NULL,
  kick VARCHAR (31) NOT NULL,
  item VARCHAR (31) NOT NULL,
  sethome VARCHAR (31) NOT NULL,
  territory VARCHAR (31) NOT NULL,
  access VARCHAR (31) NOT NULL,
  home VARCHAR (31) NOT NULL,
  disband VARCHAR (31) NOT NULL,
  promote VARCHAR (31) NOT NULL,
  setwarp VARCHAR (31) NOT NULL,
  warp VARCHAR (31) NOT NULL,
  fly VARCHAR (31) NOT NULL,
  vault VARCHAR (31) NOT NULL,
  tntbank VARCHAR (31) NOT NULL,
  tntfill VARCHAR (31) NOT NULL,
  withdraw VARCHAR (31) NOT NULL,
  chest VARCHAR (31) NOT NULL,
  spawner VARCHAR (31) NOT NULL
)