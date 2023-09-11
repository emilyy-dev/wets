package ar.emily.wets.fabric;

import ar.emily.wets.common.WESpread;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractNonPlayerActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.UUID;

final class FabricNonPlayerActor extends AbstractNonPlayerActor {

  private static final String[] EMPTY_STRINGS = new String[0];

  private final CommandSourceStack source;

  FabricNonPlayerActor(final CommandSourceStack source) {
    this.source = source;
  }

  @Override
  public UUID getUniqueId() {
    return WESpread.NON_PLAYER_ACTOR_ID;
  }

  @Override
  public String getName() {
    return this.source.getTextName();
  }

  @Override
  @Deprecated
  public void printRaw(final String msg) {
    for (final String part : msg.split("\n")) {
      this.source.sendSystemMessage(net.minecraft.network.chat.Component.literal(part));
    }
  }

  @Override
  @Deprecated
  public void printDebug(final String msg) {
    for (final String part : msg.split("\n")) {
      this.source.sendSystemMessage(net.minecraft.network.chat.Component.literal(part).withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  @Deprecated
  public void print(final String msg) {
    for (final String part : msg.split("\n")) {
      this.source.sendSystemMessage(
          net.minecraft.network.chat.Component.literal(part).withStyle(ChatFormatting.LIGHT_PURPLE)
      );
    }
  }

  @Override
  @Deprecated
  public void printError(final String msg) {
    for (final String part : msg.split("\n")) {
      this.source.sendSystemMessage(net.minecraft.network.chat.Component.literal(part).withStyle(ChatFormatting.RED));
    }
  }

  @Override
  @SuppressWarnings("DataFlowIssue")
  public void print(final Component component) {
    this.source.sendSystemMessage(
        net.minecraft.network.chat.Component.Serializer.fromJson(
            GsonComponentSerializer.INSTANCE.serialize(WorldEditText.format(component, getLocale()))
        )
    );
  }

  @Override
  public Locale getLocale() {
    return WorldEdit.getInstance().getConfiguration().defaultLocale;
  }

  @Override
  public SessionKey getSessionKey() {
    return new SessionKey() {
      @Override
      public @NotNull String getName() {
        return FabricNonPlayerActor.this.getName();
      }

      @Override
      public boolean isActive() {
        return true;
      }

      @Override
      public boolean isPersistent() {
        return true;
      }

      @Override
      public UUID getUniqueId() {
        return FabricNonPlayerActor.this.getUniqueId();
      }
    };
  }

  @Override
  public String[] getGroups() {
    return EMPTY_STRINGS;
  }

  @Override
  public void checkPermission(final String permission) {
  }

  @Override
  public boolean hasPermission(final String permission) {
    return Permissions.check(this.source, permission);
  }
}
