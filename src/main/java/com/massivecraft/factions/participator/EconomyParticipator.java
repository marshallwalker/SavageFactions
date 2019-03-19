package com.massivecraft.factions.participator;

import com.massivecraft.factions.zcore.util.TL;

import java.util.UUID;

public interface EconomyParticipator extends RelationParticipator {

	UUID getAccountId();

	void transfer(double amount, EconomyParticipator participator);

	boolean withdraw(double amount);

	boolean deposit(double amount);

	double getBalance();

	String getBalanceFormatted();

	boolean canAfford(double amount);
}