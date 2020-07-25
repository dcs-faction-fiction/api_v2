package factionfiction.api.v2.campaign;

import base.game.units.MissionConfiguration;
import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.campaignfaction.CampaignFaction.fromCampaignAndFactionAndOptions;
import factionfiction.api.v2.campaignfaction.CampaignFactionService;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.util.Map;
import java.util.function.Function;

public class CampaignEndpoints implements Endpoint {

  final Function<Context, CampaignService> campServiceProvider;
  final Function<Context, CampaignFactionService> cfServiceProvider;

  public CampaignEndpoints(
    Function<Context, CampaignService> campServiceProvider,
    Function<Context, CampaignFactionService> cfServiceProvider) {

    this.campServiceProvider = campServiceProvider;
    this.cfServiceProvider = cfServiceProvider;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/campaign-api", this, roles(CAMPAIGN_MANAGER));
    javalin.post("/v2/campaign-api/campaigns", this::newCampaign, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaign-api/campaigns", this::getCampaigns, roles(CAMPAIGN_MANAGER));
    javalin.post("/v2/campaign-api/campaigns/:campaign/servers/:server/start-server", this::startServer, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaign-api/campaigns/:campaign/server-info", this::getServerInfo, roles(CAMPAIGN_MANAGER));
  }

  @OpenApi(ignore = true)
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  @OpenApi(requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = CampaignCreatePayload.class)))
  public void newCampaign(Context ctx) {
    var cfService = cfServiceProvider.apply(ctx);
    var campService = campServiceProvider.apply(ctx);
    var request = ctx.bodyAsClass(CampaignCreatePayload.class);

    var campaign = campService.newCampaign(request.name(), request.gameOptions());
    for (var cf: request.factions())
      addFactionToCampaign(cfService, campaign, cf, request);

    ctx.json(campaign);
  }

  @OpenApi(responses = {@OpenApiResponse(status = "200", content = @OpenApiContent(from = Campaign.class, isArray = true))})
  public void getCampaigns(Context ctx) {
    var campService = campServiceProvider.apply(ctx);

    var factions = campService.listCampaigns();
    ctx.json(factions);
  }

  public void startServer(Context ctx) {
    var campService = campServiceProvider.apply(ctx);
    var campaign = ctx.pathParam("campaign", String.class).get();
    var server = ctx.pathParam("server", String.class).get();
    var configuration = ctx.bodyAsClass(MissionConfiguration.class);

    campService.startMission(campaign, server, configuration);

    ctx.json("{}");
  }

  public void getServerInfo(Context ctx) {
    var campService = campServiceProvider.apply(ctx);
    var campaign = ctx.pathParam("campaign", String.class).get();

    var result = campService.getServerInfo(campaign);
    result.ifPresentOrElse(ctx::json, () -> ctx.json("{}"));
  }

  void addFactionToCampaign(CampaignFactionService cfService, Campaign campaign, CampaignCreatePayloadFactions cf, CampaignCreatePayload request) {
    cfService.newCampaignFaction(
      fromCampaignAndFactionAndOptions(
        campaign.name(),
        cf.faction(),
        cf.airbase(),
        cf.coalition(),
        request.gameOptions()));
  }
}
