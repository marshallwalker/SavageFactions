package com.massivecraft.factions.struct;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BanInfo {

	// FPlayer IDs
	private final UUID banner;
	private final UUID banned;
	private final long time;

	public BanInfo(UUID banner, UUID banned, long time) {
		this.banner = banner;
		this.banned = banned;
		this.time = time;
	}

	public BanInfo(ResultSet result) throws SQLException {
		this.banner = UUID.fromString(result.getString(1));
		this.banned = UUID.fromString(result.getString(2));
		this.time = result.getLong(3);
	}

	/**
	 * Get the FPlayer ID of the player who issued the ban.
	 *
	 * @return FPlayer ID.
	 */
	public UUID getBanner() {
		return this.banner;
	}

	/**
	 * Get the FPlayer ID of the player who got banned.
	 *
	 * @return FPlayer ID.
	 */
	public UUID getBanned() {
		return banned;
	}

	/**
	 * Get the server time when the ban was issued.
	 *
	 * @return system timestamp.
	 */
	public long getTime() {
		return time;
	}
}
