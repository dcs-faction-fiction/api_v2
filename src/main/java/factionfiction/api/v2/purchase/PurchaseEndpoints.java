package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.Location;
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
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-warehouse-items", this::buyWarehouseItems, roles(FACTION_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/zone-increase", this::zoneIncrease, roles(FACTION_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/zone-decrease", this::zoneDecrease, roles(FACTION_MANAGER));
    javalin.post("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-recoshot", this::buyRecoShot, roles(FACTION_MANAGER));
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

  public void buyWarehouseItems(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var code = ctx.bodyAsClass(PurchaseBucket.class);
    var service = serviceProvider.apply(ctx);

    code.basket().entrySet().stream().forEach(e -> {
      for (int i = 0; i < e.getValue(); i++)
        service.buyWarehouseItem(campaign, faction, e.getKey());
    });

    ctx.json("{}");
  }

  public void zoneIncrease(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    serviceProvider.apply(ctx).zoneIncrease(campaign, faction);
    ctx.json("{}");
  }

  public void zoneDecrease(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    serviceProvider.apply(ctx).zoneDecrease(campaign, faction);
    ctx.json("{}");
  }

  void buyRecoShot(Context ctx) {
    var campaign = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var faction = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var location = ctx.bodyAsClass(Location.class);

    serviceProvider.apply(ctx).buyRecoShot(campaign, faction, location);
    ctx.json("{}");
  }
}
