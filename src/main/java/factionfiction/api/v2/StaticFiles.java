package factionfiction.api.v2;

import com.github.apilab.core.ApplicationLifecycleItem;
import io.javalin.Javalin;
import static io.javalin.http.staticfiles.Location.EXTERNAL;
import java.util.Optional;

public class StaticFiles implements ApplicationLifecycleItem {

  final Javalin javalin;

  public StaticFiles(Javalin javalin) {
    this.javalin = javalin;
  }

  @Override
  public void start() {
    String uiFolder = Optional.ofNullable(System.getProperty("API_STATIC_FOLDER")).orElse("").trim();
    if (!uiFolder.isBlank()) {
      javalin.config.addStaticFiles(uiFolder, EXTERNAL);
    }
  }

  @Override
  public void stop() {
    ///
  }

}
