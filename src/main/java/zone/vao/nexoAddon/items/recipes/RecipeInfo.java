package zone.vao.nexoAddon.items.recipes;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class RecipeInfo
{

    private boolean copyTrim;
    private boolean copyPdc;
    private boolean copyEnchants;
    private boolean keepDurability;
    private boolean copyMeta;

    public RecipeInfo(FileConfiguration config, String recipeId)
    {
        this.copyTrim = config.getBoolean(recipeId + ".copy_trim", false);
        this.copyPdc = config.getBoolean(recipeId + ".copy_pdc", false);
        this.copyEnchants = config.getBoolean(recipeId + ".copy_enchantments", true);
        this.keepDurability = config.getBoolean(recipeId + ".keep_durability", true);
        this.copyMeta = config.getBoolean(recipeId + ".copy_meta", false);
    }

}
