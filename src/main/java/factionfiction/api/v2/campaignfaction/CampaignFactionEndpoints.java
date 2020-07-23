package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import factionfiction.api.v2.campaign.CampaignCreatePayloadFactions;
import factionfiction.api.v2.campaign.CampaignService;
import static factionfiction.api.v2.campaignfaction.CampaignFaction.fromCampaignAndFactionAndOptions;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.util.Map;
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
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions", this::getAllFactions, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/allied-factions", this::getAlliedFactions, roles(FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction", this::getSituation, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/game-options", this::getGameOptions, roles(CAMPAIGN_MANAGER, FACTION_MANAGER));
    javalin.get("/v2/campaignfaction-api/factions/:faction/campaigns", this::getAvailableCampaigns, roles(FACTION_MANAGER));
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
      campaign.gameOptions());

    var result = service.newCampaignFaction(cfFull);

    ctx.json(result);
  }

  @OpenApi(responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = FactionSituation.class, isArray = false))})
  public void getSituation(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campaignName = ctx.pathParam(CAMPAIGN_PATHPARAM, String.class).get();
    var factionName = ctx.pathParam(FACTION_PATHPARAM, String.class).get();

    var result = service.getSituation(campaignName, factionName);

    ctx.json(result);
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
}
