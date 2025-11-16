package zone.vao.nexoAddon.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NALoader implements PluginLoader {
  private static List<Repo> buildRepos() {
    List<Repo> repos = new ArrayList<>();

    try {
      Field field = MavenLibraryResolver.class.getField("MAVEN_CENTRAL_DEFAULT_MIRROR");
      String mirrorUrl = (String) field.get(null);
      repos.add(new Repo("central", mirrorUrl));
    } catch (Exception e) {
      repos.add(new Repo("central", "https://maven-central.storage-download.googleapis.com/maven2"));
    }

    repos.add(new Repo("jitpack", "https://jitpack.io/"));
    repos.add(new Repo("nexo", "https://repo.nexomc.com/releases/"));

    return repos;
  }


  private static final List<String> DEPENDS =
      List.of(
          "com.nexomc:protectionlib:1.0.8",
          "net.objecthunter:exp4j:0.4.8"
      );

  @Override
  public void classloader(PluginClasspathBuilder classpathBuilder) {

    MavenLibraryResolver resolver = new MavenLibraryResolver();

    for (Repo repo : buildRepos()) {
      resolver.addRepository(new RemoteRepository.Builder(repo.id(), "default", repo.url()).build());
    }

    for (String dep : DEPENDS) {
      resolver.addDependency(new Dependency(new DefaultArtifact(dep), null));
    }

    classpathBuilder.addLibrary(resolver);
  }
}