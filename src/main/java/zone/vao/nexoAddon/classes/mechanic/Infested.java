package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;
import org.bukkit.entity.EntityType;

import java.util.List;

@Getter
public class Infested {
    private final List<EntityType> entities;
    private final double probability;
    private final String selector;
    private final boolean particles;

    public Infested(final List<EntityType> entities, final double probability, final String selector, final boolean particles) {
        this.entities = entities;
        this.probability = probability;
        this.selector = selector;
        this.particles = particles;
    }
}
