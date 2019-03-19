INSERT INTO
  faction_rule (
    faction_id,
    `index`,
    rule
  )
VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `rule` = VALUES(`rule`);