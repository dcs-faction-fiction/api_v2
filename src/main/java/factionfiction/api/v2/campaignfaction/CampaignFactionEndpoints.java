package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.Location;
import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import factionfiction.api.v2.campaign.CampaignCreatePayloadFactions;
import factionfiction.api.v2.campaign.CampaignService;
import static factionfiction.api.v2.campaignfaction.CampaignFaction.fromCampaignAndFactionAndOptions;
import factionfiction.api.v2.game.GameOptions;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class CampaignFactionEndpoints implements Endpoint {

  static final String CAMPAIGN_PATHPARAM = "campaign";
  static final String FACTION_PATHPARAM = "faction";

  final Function<Context, CampaignService> campServiceProvider;
  final Function<Context, CampaignFactionService> cfServiceProvider;

  public CampaignFactionEndpoints(
    Function<Context, CampaignService> campServiceProvider,
    Function<Context, CampaignFactionService> cfServiceProvider) {

    this.campServiceProvider = campServiceProvider;
    this.cfServiceProvider = cfServiceProvider;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/campaignfaction-api", this, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.post("/v2/campaignfaction-api/campaigns/:campaign/factions", this::addNew, roles(CAMPAIGN_MANAGER));
    javalin.delete("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction", this::removeFaction, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions", this::getAllFactions, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/allied-factions", this::getAlliedFactions, roles(FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/enemy-faction-locations", this::getEnemyFactionLocations, roles(FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction", this::getSituation, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/reco-shots", this::getRecoShots, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/game-options", this::getGameOptions, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.post("/v2/campaignfaction-api/campaigns/:campaign/game-options", this::setGameOptions, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaignfaction-api/factions/:faction/campaigns", this::getAvailableCampaigns, roles(FACTION_MANAGER));
    javalin.post("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/units/:unitid/new-location", this::moveUnit, roles(FACTION_MANAGER));
    javalin.delete("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/reco-shots/:id", this::deleteRecoShot, roles(FACTION_MANAGER));
  }

  @OpenApi(ignore = true)
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  @OpenApi(requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CampaignCreatePayloadFactions.class)))
  public void addNew(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campService = campServiceProvider.apply(ctx);
    var cf = ctx.bodyAsClass(CampaignCreatePayloadFactions.class);

    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var campaign = campService.find(campaignName);
    var cfFull = fromCampaignAndFactionAndOptions(
      campaignName,
      cf.faction(),
      cf.airbase(),
      cf.coalition(),
      campaign.gameOptions());

    var result = service.newCampaignFaction(cfFull);

    ctx.json(result);
  }

  public void removeFaction(Context ctx) {
    var service = cfServiceProvider.apply(ctx);

    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    service.removeCampaignFaction(campaignName, factionName);

    ctx.json("{}");
  }

  @OpenApi(responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = FactionSituation.class, isArray = false))})
  public void getSituation(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    var result = service.getSituation(campaignName, factionName);

    ctx.json(result);
  }

  public void setGameOptions(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var options = ctx.bodyAsClass(GameOptions.class);

    service.setGameOptions(campaignName, options);

    ctx.json("{}");
  }

  public void getGameOptions(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    var result = service.getGameOptions(campaignName, factionName);

    ctx.json(result);
  }

  public void getAvailableCampaigns(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    var result = service.getAvailableCampaigns(factionName);

    ctx.json(result);
  }

  public void getAllFactions(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();

    var result = service.getAllFactions(campaignName);

    ctx.json(result);
  }

  public void getAlliedFactions(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();

    var result = service.getAlliedFactions(campaignName);

    ctx.json(result);
  }

  public void moveUnit(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var unitId = UUID.fromString(ctx.pathParam("unitid", String.class).get());
    var location = ctx.bodyAsClass(Location.class);

    service.moveUnit(campaignName, factionName, unitId, location);

    ctx.json("{}");
  }

  public void getEnemyFactionLocations(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();

    var result = service.getEnemyFactionLocations(campaignName);

    ctx.json(result);
  }

  public void getRecoShots(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    var result = service.getRecoShots(campaignName, factionName);

    ctx.json(result);
  }

  public void deleteRecoShot(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();
    var id = UUID.fromString(ctx.pathParam("id", String.class).get());

    service.deleteRecoShot(campaignName, factionName, id);

    ctx.json("{}");
  }
}
