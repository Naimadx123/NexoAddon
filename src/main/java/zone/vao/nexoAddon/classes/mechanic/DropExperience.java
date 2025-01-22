package zone.vao.nexoAddon.classes.mechanic;


import lombok.Getter;

@Getter
public class DropExperience {
    private final double experience;


    public DropExperience(final double experience) {
        this.experience = experience;
    }
}
