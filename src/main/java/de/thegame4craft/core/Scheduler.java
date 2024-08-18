package de.thegame4craft.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public class Scheduler {
    private final net.minestom.server.timer.Scheduler scheduler;

    public Scheduler() {
        this.scheduler = MinecraftServer.getSchedulerManager();
    }

    public @NotNull Task schedule(Runnable r, TimeInterval interval){
        return scheduler.submitTask(() -> {
            r.run();
            return TaskSchedule.millis(interval.toMillisecond());
        });
    }
}
