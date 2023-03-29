package ar.emily.wets.bukkit;

import ar.emily.wets.common.AbstractScheduler;
import ar.emily.wets.common.WESpread;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WESpreadPlugin extends JavaPlugin implements Listener {

  private static final List<String> COMPLETIONS = List.of("sorted", "not-sorted");

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  private final AbstractScheduler scheduler =
      new AbstractScheduler(
          getServer()::getCurrentTick,
          task -> getServer().getScheduler().runTaskTimer(this, task, 1L, 1L)
      );
  private final WESpread plugin = new WESpread(this.scheduler);

  @EventHandler
  public void on(final PlayerQuitEvent event) {
    this.plugin.playerLogout(event.getPlayer().getUniqueId());
  }

  @Override
  public void onLoad() {
    this.plugin.load();
  }

  @Override
  public void onEnable() {
    this.scheduler.setup();
    getServer().getPluginManager().registerEvents(this, this);
  }

  @Override
  public boolean onCommand(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String label,
      final String @NotNull [] args
  ) {
    this.plugin.command(BukkitAdapter.adapt(sender), Arrays.asList(args));
    return true;
  }

  @Override
  public @NotNull List<String> onTabComplete(
      final @NotNull CommandSender sender,
      final @NotNull Command command,
      final @NotNull String alias,
      final String @NotNull [] args
  ) {
    return args.length == 1 ? StringUtil.copyPartialMatches(args[0], COMPLETIONS, new ArrayList<>(1)) : List.of();
  }
}
