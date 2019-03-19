package com.massivecraft.factions.util;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.SavageFactionsPlugin;

public class AutoLeaveTask implements Runnable {

	private static AutoLeaveProcessTask task;
	double rate;

	public AutoLeaveTask() {
		this.rate = Conf.autoLeaveRoutineRunsEveryXMinutes;
	}

	public synchronized void run() {
		if (task != null && !task.isFinished()) {
			return;
		}

		task = new AutoLeaveProcessTask();
		task.runTaskTimer(SavageFactionsPlugin.plugin, 1, 1);

		// maybe setting has been changed? if so, restart this task at new rate
		if (this.rate != Conf.autoLeaveRoutineRunsEveryXMinutes) {
			SavageFactionsPlugin.plugin.startAutoLeaveTask(true);
		}
	}
}
