package de.thegame4craft.Core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

public class Scheduler {
    private final net.minestom.server.timer.Scheduler scheduler;

    public Scheduler() {
        this.scheduler = MinecraftServer.getSchedulerManager();
    }

    public void schedule(Runnable r, TimeInterval interval){
        scheduler.submitTask(() -> {
            r.run();
            return TaskSchedule.millis(interval.toMillisecond());
        });
    }
}
