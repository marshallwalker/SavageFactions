SELECT
  player_id,
  world_name,
  chunk_x,
  chunk_z FROM
  faction_claim_ownership WHERE faction_id=?
