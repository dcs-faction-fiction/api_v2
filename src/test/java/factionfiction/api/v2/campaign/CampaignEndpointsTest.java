package factionfiction.api.v2.campaign;

import static base.game.Airbases.ANAPA;
import static base.game.CampaignCoalition.RED;
import base.game.units.MissionConfiguration;
import com.auth0.jwt.interfaces.DecodedJWT;
import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_JWT;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaignfaction.CampaignFactionService;
import factionfiction.api.v2.daemon.ServerInfo;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CampaignEndpointsTest {

  Context ctx;
  Javalin javalin;
  CampaignService campaignService;
  CampaignFactionService campaignFactionService;
  CampaignEndpoints endpoint;

  @BeforeEach
  void setup() throws IOException {
    ctx = mock(Context.class);
    javalin = mock(Javalin.class);
    campaignService = mock(CampaignService.class);
    campaignFactionService = mock(CampaignFactionService.class);
    endpoint = new CampaignEndpoints(
      v -> campaignService,
      v -> campaignFactionService);
  }

  @Test
  void testRegister() {
    endpoint.register(javalin);

    verify(javalin).get(eq("/v2/campaign-api"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).post(eq("/v2/campaign-api/campaigns"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).get(eq("/v2/campaign-api/campaigns"), any(), eq(roles(CAMPAIGN_MANAGER)));
  }

  @Test
  void testVersion() throws Exception {
    endpoint.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  void testNewCampaign() throws IOException {
    var campaign = makeSampleCampaign();
    var factionPayload = ImmutableCampaignCreatePayloadFactions.builder()
        .faction("Faction")
        .airbase(ANAPA)
        .coalition(RED)
        .build();
    var payload = ImmutableCampaignCreatePayload.builder()
      .name(campaign.name())
      .gameOptions(campaign.gameOptions())
      .factions(List.of(factionPayload))
      .build();
    given(ctx.bodyAsClass(CampaignCreatePayload.class)).willReturn(payload);
    given(campaignService.newCampaign(campaign.name(), campaign.gameOptions())).willReturn(campaign);

    endpoint.newCampaign(ctx);

    verify(ctx).json(campaign);
  }

  @Test
  void testGetCampaigns() throws IOException {
    var campaign = makeSampleCampaign();
    var campaigns = List.of(campaign);
    given(campaignService.listCampaigns()).willReturn(campaigns);

    endpoint.getCampaigns(ctx);

    verify(ctx).json(campaigns);
  }

  @Test
  void testGetServerInfo() {
    var response = mock(ServerInfo.class);
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "camp"));
    given(campaignService.getServerInfo("camp"))
      .willReturn(Optional.of(response));

    endpoint.getServerInfo(ctx);

    verify(ctx).json(response);
  }

  @Test
  void testStartServer() {
    var conf = mock(MissionConfiguration.class);
    var token = mock(DecodedJWT.class);
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "camp"));
    given(ctx.pathParam("server", String.class))
      .willReturn(Validator.create(String.class, "serv"));
    given(ctx.bodyAsClass(MissionConfiguration.class))
      .willReturn(conf);
    given(ctx.attribute(REQ_ATTR_JWT))
      .willReturn(token);

    endpoint.startServer(ctx);

    verify(campaignService).startMission("camp", "serv", conf, token);
  }

}
