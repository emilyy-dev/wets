package ar.emily.wets.bukkit;

import ar.emily.wets.common.Scheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.function.Consumer;

public final class BukkitSchedulerAdapter implements Scheduler {

  private final Plugin plugin;
  private final BukkitScheduler scheduler;

  public BukkitSchedulerAdapter(final Plugin plugin, final BukkitScheduler scheduler) {
    this.plugin = plugin;
    this.scheduler = scheduler;
  }

  @Override
  public void runPeriodically(final Consumer<Task> action, final long initialDelay, final long period) {
    this.scheduler.runTaskTimer(this.plugin, task -> action.accept(task::cancel), initialDelay, period);
  }
}
