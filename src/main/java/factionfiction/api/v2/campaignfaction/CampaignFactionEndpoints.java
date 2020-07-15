package factionfiction.api.v2.campaignfaction;

import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import factionfiction.api.v2.campaign.CampaignCreatePayloadFactions;
import factionfiction.api.v2.campaign.CampaignService;
import static factionfiction.api.v2.campaignfaction.CampaignFaction.fromCampaignAndFactionAndOptions;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.util.Map;
import java.util.function.Function;

public class CampaignFactionEndpoints implements Endpoint {

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
    javalin.post("/v2/campaignfaction-api/campaigns/:campaign/factions", this, roles(CAMPAIGN_MANAGER));
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  public void addNew(Context ctx) {
    var service = cfServiceProvider.apply(ctx);
    var campService = campServiceProvider.apply(ctx);
    var cf = ctx.bodyAsClass(CampaignCreatePayloadFactions.class);

    var campaignName = ctx.pathParam("campaign", String.class).get();
    var campaign = campService.find(campaignName);
    var cfFull = fromCampaignAndFactionAndOptions(
      campaignName,
      cf.faction(),
      cf.airbase(),
      campaign.gameOptions());

    var result = service.newCampaignFaction(cfFull);

    ctx.json(result);
  }
}
