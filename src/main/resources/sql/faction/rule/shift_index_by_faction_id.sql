UPDATE
  faction_rule
SET
  `index` = `index` - 1
WHERE
  faction_id = ?
  AND `index` > ?
