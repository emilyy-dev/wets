package ar.emily.wets.bukkit;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

final class FoliaSchedulerAdapter implements SchedulerAdapter.Instance {

  @Override
  public Consumer<Runnable> scheduleGlobal(final Plugin plugin, final long initialDelay, final long period) {
    final Server server = plugin.getServer();
    return task -> server.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), initialDelay, period);
  }

  @Override
  public void runAt(final Plugin plugin, final Location pos, final Runnable task) {
    final Server server = plugin.getServer();
    if (server.isOwnedByCurrentRegion(pos)) {
      task.run();
    } else {
      server.getRegionScheduler().execute(plugin, pos, task);
    }
  }
}
