package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import factionfiction.api.v2.campaign.CampaignCreatePayloadFactions;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaign.CampaignService;
import factionfiction.api.v2.campaign.ImmutableCampaignCreatePayloadFactions;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.game.GameOptionsLoader;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignFactionEndpointsTest {

  Context ctx;
  Javalin javalin;
  CampaignFactionEndpoints endpoints;
  CampaignService campService;
  CampaignFactionService campFactionService;

  @BeforeEach
  public void setup() {
    javalin = mock(Javalin.class);
    ctx = mock(Context.class);
    campService = mock(CampaignService.class);
    campFactionService = mock(CampaignFactionService.class);
    endpoints = new CampaignFactionEndpoints(
      v -> campService,
      v -> campFactionService);
  }

  @Test
  public void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/campaignfaction-api"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).post(eq("/v2/campaignfaction-api/campaigns/:campaign/factions"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/game-options"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/factions/:faction/campaigns"), any(), eq(roles(FACTION_MANAGER)));
  }

  @Test
  public void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  public void testAddCampaignFaction() throws IOException {
    var cf = makeSampleCampaignFaction();
    var campaign = makeSampleCampaign();
    var payload = ImmutableCampaignCreatePayloadFactions.builder()
      .faction(cf.factionName())
      .airbase(cf.airbase())
      .build();
    given(ctx.bodyAsClass(CampaignCreatePayloadFactions.class))
      .willReturn(payload);
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, cf.campaignName()));
    given(campService.find(cf.campaignName()))
      .willReturn(campaign);
    given(campFactionService.newCampaignFaction(cf))
      .willReturn(cf);

    endpoints.addNew(ctx);

    verify(ctx).json(cf);
  }

  @Test
  public void testGetSituation() throws IOException {
    var cf = makeSampleCampaignFaction();
    var situation = mock(FactionSituation.class);
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, cf.factionName()));
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, cf.campaignName()));
    given(campFactionService.getSituation(cf.campaignName(), cf.factionName()))
      .willReturn(situation);

    endpoints.getSituation(ctx);

    verify(ctx).json(situation);
  }

  @Test
  public void testGetOptions() throws IOException {
    var cf = makeSampleCampaignFaction();
    var options = new GameOptionsLoader().loadDefaults();
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, cf.factionName()));
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, cf.campaignName()));
    given(campFactionService.getGameOptions(cf.campaignName(), cf.factionName()))
      .willReturn(options);

    endpoints.getGameOptions(ctx);

    verify(ctx).json(options);
  }

  @Test
  public void testGetAvailableCampaigns() {
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction"));
    given(campFactionService.getAvailableCampaigns("faction"))
      .willReturn(List.of("campaign"));
    endpoints.getAvailableCampaigns(ctx);

    verify(ctx).json(List.of("campaign"));
  }
}
