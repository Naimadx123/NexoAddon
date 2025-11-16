package zone.vao.nexoAddon.utils.handlers;

import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public class ApiCompatibilityHandler {

  public static boolean hasEvolution(FurnitureMechanic mechanic) {
    try {
      Method oldMethod = mechanic.getClass().getMethod("hasEvolution");
      return (boolean) oldMethod.invoke(mechanic);
    } catch (NoSuchMethodException e) {
      try {
        Method newMethod = mechanic.getClass().getMethod("getHasEvolution");
        return (boolean) newMethod.invoke(mechanic);
      } catch (Exception ex) {
        throw new RuntimeException("Neither 'hasEvolution()' nor 'getHasEvolution()' are available!", ex);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error invoking method on mechanics", e);
    }
  }

  public static void openCrafting(Player bukkitPlayer, Component title) {
    try {
      Object craftPlayer = bukkitPlayer;
      Class<?> craftPlayerClass = findCraftPlayerClass(craftPlayer.getClass());
      Method getHandle = craftPlayerClass.getMethod("getHandle");
      Object serverPlayer = getHandle.invoke(craftPlayer);

      Object nmsTitle = toNmsComponent(title);

      Class<?> serverPlayerClass = serverPlayer.getClass();
      double x = (double) serverPlayerClass.getMethod("getX").invoke(serverPlayer);
      double y = (double) serverPlayerClass.getMethod("getY").invoke(serverPlayer);
      double z = (double) serverPlayerClass.getMethod("getZ").invoke(serverPlayer);

      Class<?> blockPosClass = Class.forName("net.minecraft.core.BlockPos");
      Method containing = blockPosClass.getMethod("containing", double.class, double.class, double.class);
      Object blockPos = containing.invoke(null, x, y, z);

      Object level = serverPlayerClass.getMethod("level").invoke(serverPlayer);

      Class<?> accessClass = Class.forName("net.minecraft.world.inventory.ContainerLevelAccess");
      Method createAccess = accessClass.getMethod("create",
          Class.forName("net.minecraft.world.level.Level"), blockPosClass);
      Object access = createAccess.invoke(null, level, blockPos);

      Class<?> craftingMenuClass = null;
      Class<?> invClass = Class.forName("net.minecraft.world.entity.player.Inventory");
      Class<?> playerClass = Class.forName("net.minecraft.world.entity.player.Player");

      Class<?> menuCtorClass = Class.forName("net.minecraft.world.inventory.MenuConstructor");
      Object menuCtorProxy = Proxy.newProxyInstance(
          menuCtorClass.getClassLoader(),
          new Class<?>[]{menuCtorClass},
          (proxy, method, args) -> {
            if ("createMenu".equals(method.getName())) {
              int id = (int) args[0];
              Object playerInv = args[1];
              Constructor<?> ctor = craftingMenuClass.getConstructor(int.class, invClass, accessClass);
              return ctor.newInstance(id, playerInv, access);
            }
            throw new UnsupportedOperationException(method.getName());
          });

      Class<?> simpleProviderClass = Class.forName("net.minecraft.world.inventory.SimpleMenuProvider");
      Constructor<?> providerCtor = simpleProviderClass.getConstructor(menuCtorClass,
          Class.forName("net.minecraft.network.chat.Component"));
      Object provider = providerCtor.newInstance(menuCtorProxy, nmsTitle);

      Class<?> menuProviderClass = Class.forName("net.minecraft.world.MenuProvider");
      Method openMenu = serverPlayerClass.getMethod("openMenu", menuProviderClass);
      openMenu.invoke(serverPlayer, provider);

    } catch (Throwable t) {
      try {
        bukkitPlayer.openWorkbench(bukkitPlayer.getLocation(), true);
      } catch (Throwable ignored) {}
      NexoAddon.getInstance().getLogger().warning("Failed to open custom-titled crafting UI: " + t);
    }
  }

  private static Class<?> findCraftPlayerClass(Class<?> instanceClass) throws ClassNotFoundException {
    if (instanceClass.getName().equals("org.bukkit.craftbukkit.entity.CraftPlayer")) {
      return instanceClass;
    }
    try {
      return Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
    } catch (ClassNotFoundException ignored) {}

    String serverPkg = Bukkit.getServer().getClass().getPackage().getName();
    String versionSegment = serverPkg.substring("org.bukkit.craftbukkit.".length());
    String fqcn = "org.bukkit.craftbukkit." + versionSegment + ".entity.CraftPlayer";
    return Class.forName(fqcn);
  }

  private static Object toNmsComponent(Component title) throws Throwable {
    try {
      Class<?> bridge = Class.forName("io.papermc.paper.adventure.PaperAdventure");
      Method asVanilla = bridge.getMethod("asVanilla", Component.class);
      return asVanilla.invoke(null, title);
    } catch (ClassNotFoundException ignored) {
      String plain = PlainTextComponentSerializer.plainText().serialize(
          Objects.requireNonNullElse(title, Component.text("")));
      Class<?> nmsComp = Class.forName("net.minecraft.network.chat.Component");
      Method literal = nmsComp.getMethod("literal", String.class);
      return literal.invoke(null, plain);
    }
  }
}
