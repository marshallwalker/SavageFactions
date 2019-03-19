INSERT INTO
  faction_home (faction_id, world_name, x, y, z, yaw, pitch)
VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY
UPDATE
  world_name =
VALUES(world_name),
  x =
VALUES(x),
  y =
VALUES(y),
  z =
VALUES(z),
  yaw =
VALUES(yaw),
  pitch =
VALUES(pitch)
