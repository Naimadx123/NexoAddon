package zone.vao.nexoAddon.nms.v1_21_4;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import zone.vao.nexoAddon.nms.INmsHandler;

import java.util.ArrayList;
import java.util.List;

public class NmsHandler implements INmsHandler {

  public List<NamespacedKey> getDatapackCustomBiomes() {
    net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(Registries.BIOME);

    List<NamespacedKey> customBiomes = new ArrayList<>();
    for (ResourceLocation biomeId : biomeRegistry.keySet()) {
      if (!biomeId.getNamespace().equals("minecraft")) {
        NamespacedKey key = new NamespacedKey(biomeId.getNamespace(), biomeId.getPath());
        customBiomes.add(key);
      }
    }
    return customBiomes;
  }

  public NamespacedKey getBiomeKeyAtLocation(Location loc) {
    ServerLevel nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();
    int biomeX = loc.getBlockX() >> 2;
    int biomeY = loc.getBlockY() >> 2;
    int biomeZ = loc.getBlockZ() >> 2;
    net.minecraft.world.level.biome.Biome biomeBase = nmsWorld.getNoiseBiome(biomeX, biomeY, biomeZ).value();
    ResourceLocation biomeId = CraftRegistry.getMinecraftRegistry(Registries.BIOME).getKey(biomeBase);
    return new NamespacedKey(biomeId.getNamespace(), biomeId.getPath());
  }
}
