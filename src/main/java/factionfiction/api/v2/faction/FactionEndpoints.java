package factionfiction.api.v2.faction;

import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.util.Map;
import java.util.function.Function;

public class FactionEndpoints implements Endpoint {

  final Function<Context, FactionService> serviceProvider;

  public FactionEndpoints(Function<Context, FactionService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/faction-api", this, roles(FACTION_MANAGER));
    javalin.post("/v2/faction-api/factions", this::newFaction, roles(FACTION_MANAGER));
    javalin.get("/v2/faction-api/factions", this::getFactions, roles(FACTION_MANAGER));
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  public void newFaction(Context ctx) {
    var service = serviceProvider.apply(ctx);
    var name = ctx.bodyAsClass(String.class);

    var faction = service.newFaction(name);
    ctx.json(faction);
  }

  public void getFactions(Context ctx) {
    var service = serviceProvider.apply(ctx);

    var factions = service.listFactions();
    ctx.json(factions);
  }
}
