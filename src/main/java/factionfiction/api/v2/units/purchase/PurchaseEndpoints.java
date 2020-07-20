package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

public class PurchaseEndpoints implements Endpoint {

  static final String FACTION_PATHPARAM = "faction";
  static final String CAMPAIGN_PATHPARAM = "campaign";

  final Function<Context, PurchaseService> serviceProvider;

  public PurchaseEndpoints(Function<Context, PurchaseService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/purchase-api", this, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/give-credits", this::giveCredits, roles(CAMPAIGN_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-unit", this::buyUnit, roles(FACTION_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-warehouse-item", this::buyWarehouseItem, roles(FACTION_MANAGER));
  }

  @OpenApi(ignore = true)
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  public void giveCredits(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var credits = ctx.bodyAsClass(BigDecimal.class);

    serviceProvider.apply(ctx).giveCredits(campaign, faction, credits);
    ctx.json("{}");
  }

  public void buyUnit(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var unit = ctx.bodyAsClass(FactionUnit.class);

    var result = serviceProvider.apply(ctx).buyUnit(campaign, faction, unit);
    ctx.json(result);
  }

  public void buyWarehouseItem(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var code = ctx.bodyAsClass(WarehouseItemCode.class);

    serviceProvider.apply(ctx).buyWarehouseItem(campaign, faction, code);
    ctx.json("{}");
  }
}
