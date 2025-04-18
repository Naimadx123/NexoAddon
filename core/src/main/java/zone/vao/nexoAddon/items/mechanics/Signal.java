package zone.vao.nexoAddon.items.mechanics;

import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import com.nexomc.nexo.mechanics.furniture.FurnitureFactory;
import com.nexomc.nexo.mechanics.furniture.FurnitureHelpers;
import com.nexomc.nexo.mechanics.furniture.FurnitureMechanic;
import com.nexomc.nexo.mechanics.furniture.IFurniturePacketManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import zone.vao.nexoAddon.NexoAddon;
import zone.vao.nexoAddon.items.Mechanics;

import java.util.Objects;

public record Signal(int radius, double channel, String role) {

    public static class SignalListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL)
        public static void on(NexoFurnitureInteractEvent event) {
            if (NexoAddon.getInstance().getMechanics().isEmpty()) return;
            if (event.getHand() != EquipmentSlot.HAND) return;

            Mechanics mechanics = NexoAddon.getInstance().getMechanics().get(event.getMechanic().getItemID());
            if (mechanics == null) return;

            Signal signal = mechanics.getSignal();
            if (signal == null || !Objects.equals(signal.role(), "TRANSMITTER")) return;

            event.getPlayer().swingMainHand();
            scanForReceivers(event.getBaseEntity().getWorld(), event.getBaseEntity().getLocation(), signal);
        }

        private static void scanForReceivers(World world, Location center, Signal signal) {
            int radius = signal.radius();
            double channel = signal.channel();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = center.clone().add(x, y, z);
                        Block block = world.getBlockAt(loc);
                        if (block.getType() != Material.AIR) continue;
                        processReceiver(block, channel);
                    }
                }
            }
        }

        private static void processReceiver(Block block, double channel) {
            if (!NexoFurniture.isFurniture(block.getLocation())) return;

            ItemDisplay baseEntity = NexoFurniture.baseEntity(block);
            FurnitureMechanic mechanic = NexoFurniture.furnitureMechanic(block);
            if (mechanic == null) return;

            Mechanics targetMechanics = NexoAddon.getInstance().getMechanics().get(mechanic.getItemID());
            if (targetMechanics == null) return;

            Signal targetSignal = targetMechanics.getSignal();
            if (targetSignal == null || !Objects.equals(targetSignal.role(), "RECEIVER") || targetSignal.channel() != channel) return;

            toggleLightState(baseEntity, mechanic);
        }

        private static void toggleLightState(ItemDisplay baseEntity, FurnitureMechanic mechanic) {
            if (baseEntity == null) return;

            boolean isLightOn = FurnitureHelpers.lightState(baseEntity);
            FurnitureHelpers.toggleLight(baseEntity, !isLightOn);
            IFurniturePacketManager packetManager = FurnitureFactory.Companion.instance().packetManager();

            if (isLightOn) {
                packetManager.removeLightMechanicPacket(baseEntity, mechanic);
            } else {
                packetManager.sendLightMechanicPacket(baseEntity, mechanic);
            }
        }
    }
}
