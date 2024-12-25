package zone.vao.nexoAddon.classes.bedrockbreak;

import zone.vao.nexoAddon.mechanics.breakable.BreakableMechanic;
import com.nexomc.nexo.mechanics.MechanicFactory;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.ThreadLocalRandom;

public class BedrockBreakMechanic extends BreakableMechanic {

    final long delay;
    final long period;
    final int probability;

    public BedrockBreakMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        super(mechanicFactory, section);
        this.delay = section.getLong("delay");
        this.period = section.getLong("hardness");
        this.probability = (int) (1D / section.getDouble("probability"));
    }

    public long getPeriod() {
        return period;
    }

    public boolean bernouilliTest() {
        return ThreadLocalRandom.current().nextInt(probability) == 0;
    }
}