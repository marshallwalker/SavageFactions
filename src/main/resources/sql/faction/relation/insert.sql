INSERT INTO
  faction_relation (
    faction_id,
    target_id,
    relation
  )
VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE relation = VALUES(relation);