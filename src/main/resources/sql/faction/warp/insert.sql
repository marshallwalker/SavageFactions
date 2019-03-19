INSERT INTO
  faction_warp (
    faction_id,
    warp_name,
    warp_password,
    world_name,
    x,
    y,
    z,
    yaw,
    pitch
  )
VALUES
  (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY
UPDATE
  warp_password =
VALUES(warp_password),
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
VALUES(pitch);
