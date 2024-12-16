package zone.vao.nexoAddon.classes.bigmining;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class BigMiningMechanic extends Mechanic {

    private final int radius;
    private final int depth;

    public BigMiningMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        radius = section.getInt("radius");
        depth = section.getInt("depth");
    }

    public int getRadius() {
        return this.radius;
    }

    public int getDepth() {
        return this.depth;
    }

}