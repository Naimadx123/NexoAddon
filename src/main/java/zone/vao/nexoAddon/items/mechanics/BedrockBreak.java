package zone.vao.nexoAddon.items.mechanics;

import org.bukkit.Sound;

public record BedrockBreak(int hardness, double probability, int durabilityCost, boolean disableOnFirstLayer, Sound sound) {
}
