package zone.vao.nexoAddon.events.blockBreaks;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.protectionlib.ProtectionLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.classes.Mechanics;


public class SpawnerBreakListener {

    private static final String SPAWNER_TYPE_KEY = "spawnerType";

    public static void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.SPAWNER) return;

        Block block = e.getBlock();
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String nexoItemId = NexoItems.idFromItem(tool);

        Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(nexoItemId);
        if (mechanics == null || mechanics.getSpawnerBreak() == null || !ProtectionLib.canBreak(player, block.getLocation())) return;

        double probability = mechanics.getSpawnerBreak().getProbability();
        boolean dropExperience = mechanics.getSpawnerBreak().isDropExperience();
        handleBreakingSpawner(e, block, probability, dropExperience);
    }

    private static void handleBreakingSpawner(BlockBreakEvent event, Block block, double probability, boolean dropExperience) {
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        EntityType entityType = getValidEntityType(spawner.getSpawnedType());

        ItemStack spawnerItem = createSpawnerItem(entityType);

        if (Math.random() <= probability) {
            block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
        }

        if (!dropExperience) {
            event.setExpToDrop(0);
        }
    }

    private static ItemStack createSpawnerItem(EntityType entityType) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
        ItemMeta spawnerMeta = spawnerItem.getItemMeta();

        if (spawnerMeta != null) {
            spawnerMeta.setDisplayName(entityType.name() + " Spawner");
            spawnerMeta.getPersistentDataContainer()
                    .set(new NamespacedKey(NexoAddon.getInstance(), SPAWNER_TYPE_KEY), PersistentDataType.STRING, entityType.name());
            spawnerItem.setItemMeta(spawnerMeta);
        }

        return spawnerItem;
    }

    private static EntityType getValidEntityType(EntityType type) {
        return (type == null || type == EntityType.UNKNOWN) ? EntityType.PIG : type;
    }
}
