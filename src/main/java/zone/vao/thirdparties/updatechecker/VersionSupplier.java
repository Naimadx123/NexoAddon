package zone.vao.thirdparties.updatechecker;

import java.io.IOException;

@FunctionalInterface
public interface VersionSupplier {

    String getLatestVersionString() throws IOException;

}
