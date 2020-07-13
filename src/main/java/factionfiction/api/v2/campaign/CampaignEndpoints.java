package factionfiction.api.v2.campaign;

import com.github.apilab.rest.Endpoint;
import factionfiction.api.v2.auth.AuthInfo;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import factionfiction.api.v2.campaignfaction.CampaignFactionSecurity;
import factionfiction.api.v2.campaignfaction.CampaignFactionService;
import factionfiction.api.v2.campaignfaction.CampaignFactionServiceImpl;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.util.Map;

public class CampaignEndpoints implements Endpoint {

  final CampaignServiceImpl impl;
  final CampaignFactionServiceImpl cfImpl;

  public CampaignEndpoints(
    CampaignServiceImpl impl,
    CampaignFactionServiceImpl cfImpl) {

    this.impl = impl;
    this.cfImpl = cfImpl;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/campaign-api", this, roles(CAMPAIGN_MANAGER));
    javalin.post("/v2/campaign-api/campaigns", this::newCampaign, roles(CAMPAIGN_MANAGER));
    javalin.get("/v2/campaign-api/campaigns", this::getCampaigns, roles(CAMPAIGN_MANAGER));
  }

  public void newCampaign(Context ctx) {
    var service = service(ctx);
    var cfService = cfService(ctx);
    var request = ctx.bodyAsClass(CampaignCreatePayload.class);

    var campaign = service.newCampaign(request.name(), request.gameOptions());
    for (var cf: request.factions())
      addFactionToCampaign(cfService, campaign, cf, request);

    ctx.json(campaign);
  }

  void addFactionToCampaign(CampaignFactionService cfService, Campaign campaign, CampaignCreatePayloadFactions cf, CampaignCreatePayload request) {
    cfService.newCampaignFaction(
      cfImpl.fromCampaignAndFactionAndOptions(
        campaign.name(),
        cf.faction(),
        cf.airbase(),
        request.gameOptions()));
  }

  public void getCampaigns(Context ctx) {
    var service = service(ctx);

    var factions = service.listCampaigns();
    ctx.json(factions);
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  private CampaignService service(Context ctx) {
    var authInfo = AuthInfo.fromContext(ctx);
    return new CampaignSecurity(impl, authInfo);
  }
  private CampaignFactionService cfService(Context ctx) {
    var authInfo = AuthInfo.fromContext(ctx);
    return new CampaignFactionSecurity(cfImpl, authInfo);
  }
}
