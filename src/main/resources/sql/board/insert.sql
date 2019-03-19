INSERT INTO
  faction_board (
    faction_id,
    world_name,
    chunk_x,
    chunk_z
  )
VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE faction_id = VALUES(faction_id);