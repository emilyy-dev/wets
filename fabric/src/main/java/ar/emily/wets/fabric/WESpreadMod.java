package ar.emily.wets.fabric;

import ar.emily.wets.common.AbstractScheduler;
import ar.emily.wets.common.WESpread;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.fabric.FabricAdapter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static net.minecraft.commands.Commands.LEVEL_GAMEMASTERS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class WESpreadMod implements ModInitializer {

  private static final List<String> SORTED = List.of("sorted");
  private static final List<String> NOT_SORTED = List.of("not-sorted");

  private WESpread plugin;

  @Override
  public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> command(dispatcher));
    ServerLifecycleEvents.SERVER_STARTING.register(server -> {
      this.plugin = new WESpread(new AbstractScheduler(server::getTickCount, task -> ServerTickEvents.END_SERVER_TICK.register(s -> task.run())));
      this.plugin.load();
    });
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.plugin.flush());
    ServerPlayConnectionEvents.DISCONNECT.register((packetListener, server) ->
        this.plugin.playerLogout(packetListener.getPlayer().getUUID())
    );
  }

  private int executeCommand(final CommandContext<CommandSourceStack> ctx, final List<String> args) throws CommandSyntaxException {
    this.plugin.command(FabricAdapter.adaptPlayer(ctx.getSource().getPlayerOrException()), args);
    return Command.SINGLE_SUCCESS;
  }

  private void command(final CommandDispatcher<CommandSourceStack> dispatcher) {
    final Predicate<CommandSourceStack> permissionRequirement = Permissions.require("wets", LEVEL_GAMEMASTERS);
    final LiteralCommandNode<CommandSourceStack> wetsCommand = dispatcher.register(
        literal("worldedit-tick-spreader")
            .requires(permissionRequirement)
            .then(literal("sorted").executes(ctx -> executeCommand(ctx, SORTED)))
            .then(literal("not-sorted").executes(ctx -> executeCommand(ctx, NOT_SORTED)))
            .then(argument("blocks-per-tick", longArg()).executes(ctx ->
                executeCommand(ctx, List.of(String.valueOf(getLong(ctx, "blocks-per-tick"))))
            ))
    );

    dispatcher.register(literal("wets").requires(permissionRequirement).redirect(wetsCommand));
  }
}
