package zone.vao.nexoAddon.classes.mechanic;

import lombok.Getter;

@Getter
public class SpawnerBreak {
    private final double probability;
    private final boolean dropExperience;

    public SpawnerBreak(final double probability, final boolean dropExperience) {
        this.probability = probability;
        this.dropExperience = dropExperience;
    }
}
