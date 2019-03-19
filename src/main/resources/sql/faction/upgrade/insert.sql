INSERT INTO
  faction_upgrade (
    faction_id,
    `key`,
    `value`
  )
VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);