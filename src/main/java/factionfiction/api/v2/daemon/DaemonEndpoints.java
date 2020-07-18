package factionfiction.api.v2.daemon;

import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.DAEMON;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import java.util.Map;

public class DaemonEndpoints implements Endpoint {

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/daemon-api", this, roles(DAEMON));
  }

  @OpenApi(ignore = true)
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }
}
