package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.protectionlib.ProtectionLib;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;
import zone.vao.nexoAddon.utils.CooldownUtil;
import zone.vao.nexoAddon.utils.LiquidUtil;

import java.util.List;

public record Liquid(
    String itemId,
    Biome biome,
    String biomeKey,
    boolean followFlow,
    boolean placeEnabled,
    boolean overwriteOtherLiquids,
    int placeRadius,
    String emptyItem,
    String bucketItem,
    String bottleItem,
    Biome revertTo,
    List<PotionEffect> effects,
    double effectsCooldown,
    String effectsPool,
    List<String> commands,
    double commandsCooldown,
    String commandsPool,
    boolean commandsAsConsole
) {

  public static class LiquidListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;
      if (event.getBucket() != Material.WATER_BUCKET) return;

      Player player = event.getPlayer();
      ItemStack held = player.getInventory().getItem(event.getHand());
      String itemId = NexoItems.idFromItem(held);
      if (itemId == null || itemId.isEmpty()) return;

      Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
      if (mechanics == null) return;

      Liquid liquid = mechanics.getLiquid();
      if (liquid == null || !liquid.placeEnabled()) return;

      Location location = event.getBlock().getLocation();
      if (!ProtectionLib.canBuild(player, location)) {
        event.setCancelled(true);
        return;
      }

      if (liquid.emptyItem() != null) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(liquid.emptyItem());
        if (itemBuilder != null) event.setItemStack(itemBuilder.build());
      }

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(location, task -> {
        if (!LiquidUtil.isWater(location.getBlock())) return;
        LiquidUtil.paintBiome(location, liquid.placeRadius(), liquid.biome(), liquid.overwriteOtherLiquids());
      }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;

      Block block = event.getBlock();
      if (!LiquidUtil.isWater(block)) return;

      Liquid liquid = LiquidUtil.byBiome(block.getBiome());
      if (liquid == null) return;

      Player player = event.getPlayer();
      if (!ProtectionLib.canBuild(player, block.getLocation())) {
        event.setCancelled(true);
        return;
      }

      if (liquid.bucketItem() != null) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(liquid.bucketItem());
        if (itemBuilder != null) event.setItemStack(itemBuilder.build());
      }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCauldron(CauldronLevelChangeEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;

      Location location = event.getBlock().getLocation();
      CauldronLevelChangeEvent.ChangeReason reason = event.getReason();

      if (reason == CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL
          && event.getEntity() instanceof Player filler) {
        Liquid source = LiquidUtil.byBiome(event.getBlock().getBiome());
        if (source != null && source.bottleItem() != null) swapFilledBottle(filler, source.bottleItem());
      }

      if (event.getNewLevel() <= 0) return;
      if (reason != CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY) return;
      if (!(event.getEntity() instanceof Player player)) return;

      Liquid liquid = heldLiquid(player);
      if (liquid == null) return;

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(location, task ->
          LiquidUtil.paintBiome(location, liquid.placeRadius(), liquid.biome(),
              liquid.overwriteOtherLiquids()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBottleFill(PlayerInteractEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;
      if (event.getHand() != EquipmentSlot.HAND) return;

      Action action = event.getAction();
      if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

      Player player = event.getPlayer();
      if (player.getInventory().getItemInMainHand().getType() != Material.GLASS_BOTTLE) return;

      RayTraceResult hit = player.rayTraceBlocks(5.0, FluidCollisionMode.SOURCE_ONLY);
      if (hit == null || hit.getHitBlock() == null) return;

      Block block = hit.getHitBlock();
      if (!LiquidUtil.holdsLiquid(block)) return;

      Liquid liquid = LiquidUtil.byBiome(block.getBiome());
      if (liquid == null || liquid.bottleItem() == null) return;

      swapFilledBottle(player, liquid.bottleItem());
    }

    private void swapFilledBottle(Player player, String bottleId) {
      int before = countWaterBottles(player);

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtEntityLater(player, task -> {
        if (!player.isOnline()) return;
        if (countWaterBottles(player) <= before) return;

        ItemBuilder itemBuilder = NexoItems.itemFromId(bottleId);
        if (itemBuilder == null) return;

        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
          if (!isWaterBottle(contents[slot])) continue;

          ItemStack replacement = itemBuilder.build();
          replacement.setAmount(1);

          if (contents[slot].getAmount() > 1) {
            contents[slot].setAmount(contents[slot].getAmount() - 1);
            player.getInventory().addItem(replacement);
          } else {
            player.getInventory().setItem(slot, replacement);
          }
          return;
        }
      }, 1L);
    }

    private int countWaterBottles(Player player) {
      int count = 0;
      for (ItemStack item : player.getInventory().getContents()) {
        if (isWaterBottle(item)) count += item.getAmount();
      }
      return count;
    }

    private boolean isWaterBottle(ItemStack item) {
      if (item == null || item.getType() != Material.POTION) return false;
      if (NexoItems.idFromItem(item) != null) return false;

      return item.getItemMeta() instanceof PotionMeta meta
          && meta.hasBasePotionType()
          && meta.getBasePotionType() == PotionType.WATER;
    }

    private Liquid heldLiquid(Player player) {
      for (ItemStack item : new ItemStack[]{
          player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand()}) {
        String itemId = NexoItems.idFromItem(item);
        if (itemId == null || itemId.isEmpty()) continue;

        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(itemId);
        if (mechanics == null) continue;

        Liquid liquid = mechanics.getLiquid();
        if (liquid != null && liquid.placeEnabled()) return liquid;
      }
      return null;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;

      NexoAddon.getInstance().getFoliaLib().getScheduler().runAtLocationLater(
          event.getChunk().getBlock(0, 0, 0).getLocation(),
          task -> LiquidUtil.restart(event.getChunk()),
          3L
      );
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;
      LiquidUtil.forgetChunk(event.getChunk());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent event) {
      if (!NexoAddon.getInstance().getIsLiquid()) return;

      Block from = event.getBlock();
      if (!LiquidUtil.isWater(from)) return;

      Liquid liquid = LiquidUtil.byBiome(from.getBiome());
      if (liquid == null || !liquid.followFlow()) return;

      Block to = event.getToBlock();
      if (to.getBiome().getKey().equals(liquid.biome().getKey())) return;

      LiquidUtil.paintBiome(to.getLocation(), 0, liquid.biome());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
      CooldownUtil.forget(event.getPlayer().getUniqueId());
    }
  }
}
