INSERT INTO faction_chest (faction_id, `index`, item) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE item=VALUES(item)