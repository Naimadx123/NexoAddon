package zone.vao.nexoAddon.utils.exceptions;

public class FailedRecipeLoadException extends IllegalArgumentException
{

    public FailedRecipeLoadException(String recipeId, String fileName, String reason)
    {
        super("Failed to load recipe " + recipeId + " from " + fileName+": " + reason);
    }

}
