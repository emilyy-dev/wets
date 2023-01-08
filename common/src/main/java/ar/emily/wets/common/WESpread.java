package ar.emily.wets.common;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

public final class WESpread {

  private static final Component PLAYERS_ONLY = TextComponent.of("This command can only be ran by players", TextColor.RED);
  private static final Component INVALID_ARGUMENT = TextComponent.of("Blocks per tick must be a valid positive number", TextColor.RED);
  private static final Component COMMAND_USAGE = TextComponent.of("Usage: /wets (<blocks per tick> | sorted | not-sorted)");
  private static final Component COMMAND_SORTED = TextComponent.of("Block placement of new operations will now be sorted");
  private static final Component COMMAND_NOT_SORTED = TextComponent.of("Block placement of new operations will now be unsorted");
  private static final Component COMMAND_BPT_UPDATED = TextComponent.of("Blocks per tick placement updated");

  private final Scheduler scheduler;
  private final Object2LongMap<UUID> blocksPerTickMap = new Object2LongOpenHashMap<>();
  private final Set<UUID> actorsWhosePlacementIsNotSorted = new HashSet<>();
  private long defaultBlocksPerTick = 1;

  public WESpread(final Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void playerLogout(final UUID id) {
    this.blocksPerTickMap.removeLong(id);
    this.actorsWhosePlacementIsNotSorted.remove(id);
  }

  public void flush() {
    this.defaultBlocksPerTick = Long.MAX_VALUE;
    this.blocksPerTickMap.clear();
    this.actorsWhosePlacementIsNotSorted.clear();
    this.scheduler.flush();
  }

  public void load() {
    WorldEdit.getInstance().getEventBus().register(new Object() {
      @Subscribe
      public void on(final EditSessionEvent event) {
        final @Nullable Actor actor = event.getActor();
        if (event.getStage() == EditSession.Stage.BEFORE_CHANGE && actor != null) {
          event.setExtent(new SchedulingExtent(event.getExtent(), actor.getUniqueId()));
        }
      }
    });
  }

  public void command(final Actor source, final List<String> args) {
    if (args.size() != 1) {
      source.print(COMMAND_USAGE);
      return;
    }
    if (!(source instanceof Player player)) {
      source.print(PLAYERS_ONLY);
      return;
    }

    final String arg = args.iterator().next();
    if ("sorted".equals(arg)) {
      this.actorsWhosePlacementIsNotSorted.remove(player.getUniqueId());
      source.print(COMMAND_SORTED);
      return;
    } else if ("not-sorted".equals(arg)) {
      this.actorsWhosePlacementIsNotSorted.add(player.getUniqueId());
      source.print(COMMAND_NOT_SORTED);
      return;
    }

    try {
      long blocksPerTick = Long.parseLong(arg);
      if (blocksPerTick < 0) { blocksPerTick = Long.MAX_VALUE; }
      this.blocksPerTickMap.put(player.getUniqueId(), blocksPerTick);
      source.print(COMMAND_BPT_UPDATED);
    } catch (final NumberFormatException exception) {
      source.print(INVALID_ARGUMENT);
      source.print(COMMAND_USAGE);
    }
  }

  private final class SchedulingExtent extends AbstractBufferingExtent {

    private final BlockMap<BaseBlock> buffer = BlockMap.createForBaseBlock();
    private final LongSupplier blocksPerTick;
    private final boolean sorted;

    private SchedulingExtent(final Extent delegate, final UUID playerId) {
      super(delegate);
      this.sorted = !WESpread.this.actorsWhosePlacementIsNotSorted.contains(playerId);
      this.blocksPerTick = () -> WESpread.this.blocksPerTickMap.getOrDefault(playerId, WESpread.this.defaultBlocksPerTick);
    }

    @Override
    protected @Nullable BaseBlock getBufferedFullBlock(final BlockVector3 position) {
      return this.buffer.computeIfAbsent(position, getExtent()::getFullBlock);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(final BlockVector3 position, final T block) {
      this.buffer.remove(position); // workaround silly WorldEdit bug, see https://github.com/emilyy-dev/wets/issues/1
      this.buffer.put(position, block.toBaseBlock());
      return true;
    }

    @Override
    protected Operation commitBefore() {
      final Iterator<Map.Entry<BlockVector3, BaseBlock>> it;
      {
        Stream<Map.Entry<BlockVector3, BaseBlock>> stream = this.buffer.entrySet().stream();
        if (this.sorted) { stream = stream.sorted(Map.Entry.comparingByKey(BlockVector3.sortByCoordsYzx())); }
        it = stream.iterator();
      }

      return new Operation() {
        boolean started = false;

        @Override
        public Operation resume(final RunContext run) {
          //noinspection PointlessBooleanExpression
          if (this.started == false && it.hasNext()) {
            this.started = true;
            WESpread.this.scheduler.runPeriodically(this::putBlock, 1L, 1L);
          }

          return null;
        }

        private void putBlock(final Scheduler.Task task) {
          try {
            for (long i = 0; it.hasNext() && i < SchedulingExtent.this.blocksPerTick.getAsLong(); ++i) {
              final var entry = it.next();
              setDelegateBlock(entry.getKey(), entry.getValue());
            }
          } catch (final WorldEditException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
          } finally {
            if (!it.hasNext()) { task.cancel(); }
          }
        }

        @Override
        public void cancel() {
        }
      };
    }
  }
}
