package factionfiction.api.v2.campaign;

import static base.game.Airbases.ANAPA;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaignfaction.CampaignFactionServiceImpl;
import static factionfiction.api.v2.test.AuthProvider.mockUser;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignEndpointsTest {

  UUID user;
  Context ctx;
  Javalin javalin;
  CampaignServiceImpl impl;
  CampaignFactionServiceImpl cfImpl;
  CampaignEndpoints endpoints;

  @BeforeEach
  public void setup() throws IOException {
    user = UUID.randomUUID();
    ctx = mock(Context.class);
    javalin = mock(Javalin.class);
    impl = mock(CampaignServiceImpl.class);
    cfImpl = mock(CampaignFactionServiceImpl.class);
    endpoints = new CampaignEndpoints(impl, cfImpl);
  }

  @Test
  public void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/campaign-api"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).post(eq("/v2/campaign-api/campaigns"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).get(eq("/v2/campaign-api/campaigns"), any(), eq(roles(CAMPAIGN_MANAGER)));
  }

  @Test
  public void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  public void testNewCampaign() throws IOException {
    var campaign = makeSampleCampaign();
    var factionPayload = ImmutableCampaignCreatePayloadFactions.builder()
        .faction("Faction")
        .airbase(ANAPA)
        .build();
    var payload = ImmutableCampaignCreatePayload.builder()
      .name(campaign.name())
      .gameOptions(campaign.gameOptions())
      .factions(List.of(factionPayload))
      .build();
    mockUserWIthCampaignRole();
    given(ctx.bodyAsClass(CampaignCreatePayload.class)).willReturn(payload);
    given(impl.newCampaign(campaign.name(), user, campaign.gameOptions())).willReturn(campaign);

    endpoints.newCampaign(ctx);

    verify(ctx).json(campaign);
    verify(cfImpl).fromCampaignAndFactionAndOptions(
      payload.name(),
      factionPayload.faction(),
      factionPayload.airbase(),
      campaign.gameOptions());
  }

  @Test
  public void testGetCampaigns() throws IOException {
    var campaign = makeSampleCampaign();
    var campaigns = List.of(campaign);
    mockUserWIthCampaignRole();
    given(impl.listCampaigns(user)).willReturn(campaigns);

    endpoints.getCampaigns(ctx);

    verify(ctx).json(campaigns);
  }

  void mockUserWIthCampaignRole() {
    mockUser(ctx, user, Set.of(CAMPAIGN_MANAGER));
  }
}
