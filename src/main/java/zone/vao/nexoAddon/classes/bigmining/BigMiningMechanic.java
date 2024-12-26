package zone.vao.nexoAddon.classes.bigmining;

import com.nexomc.nexo.mechanics.Mechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

public class BigMiningMechanic extends Mechanic {
    private final int radius;
    private final int depth;
    private final BigMiningMechanicFactory factory;

    public BigMiningMechanic(MechanicFactory factory, ConfigurationSection section) {
        super(factory, section);
        this.factory = (BigMiningMechanicFactory) factory;
        
        // Load configuration values
        this.radius = section.getInt("radius", 1);
        this.depth = section.getInt("depth", 1);
    }

    public int getRadius() {
        return radius;
    }

    public int getDepth() {
        return depth;
    }

    public boolean shouldCallEvents() {
        return factory.callEvents();
    }
}