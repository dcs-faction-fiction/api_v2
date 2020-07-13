package factionfiction.api.v2.common;

import com.github.apilab.rest.Endpoint;
import factionfiction.api.v2.game.GameOptions;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

public class CommonEndpoints implements Endpoint {
  final GameOptions defaultOptions;

  public CommonEndpoints(GameOptions defaultOptions) {
    this.defaultOptions = defaultOptions;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/common-api", this);
    javalin.get("/v2/common-api/default-game-options", this::getDefaultOptions);
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  public void getDefaultOptions(Context ctx) {
    ctx.json(defaultOptions);
  }
}
