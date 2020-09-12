package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import static base.game.CampaignCoalition.RED;
import base.game.FactionSituation;
import base.game.ImmutableFactionAirbase;
import base.game.ImmutableFactionSituation;
import base.game.Location;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import factionfiction.api.v2.campaign.CampaignCreatePayloadFactions;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaign.CampaignService;
import factionfiction.api.v2.campaign.ImmutableCampaignCreatePayloadFactions;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.game.ImmutableRecoShot;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import static factionfiction.api.v2.warehouse.WarehouseHelper.makeSampleWarehouseMap;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import java.io.IOException;
import static java.math.BigDecimal.ZERO;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CampaignFactionEndpointsTest {

  Context ctx;
  Javalin javalin;
  CampaignFactionEndpoints endpoints;
  CampaignService campService;
  CampaignFactionService campFactionService;

  @BeforeEach
  void setup() {
    javalin = mock(Javalin.class);
    ctx = mock(Context.class);
    campService = mock(CampaignService.class);
    campFactionService = mock(CampaignFactionService.class);
    endpoints = new CampaignFactionEndpoints(
      v -> campService,
      v -> campFactionService);
  }

  @Test
  void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/campaignfaction-api"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).post(eq("/v2/campaignfaction-api/campaigns/:campaign/factions"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions"), any(), eq(roles(CAMPAIGN_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/allied-factions"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/enemy-faction-locations"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/reco-shots"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/game-options"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/campaignfaction-api/factions/:faction/campaigns"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).post(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/units/:unitid/new-location"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).delete(eq("/v2/campaignfaction-api/campaigns/:campaign/factions/:faction/reco-shots/:id"), any(), eq(roles(FACTION_MANAGER)));
  }

  @Test
  void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  void testAddCampaignFaction() throws IOException {
    var cf = makeSampleCampaignFaction();
    var campaign = makeSampleCampaign();
    var payload = ImmutableCampaignCreatePayloadFactions.builder()
      .faction(cf.factionName())
      .airbase(cf.airbase())
      .coalition(cf.coalition())
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
  void testGetSituation() throws IOException {
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
  void testGetOptions() throws IOException {
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
  void testGetAvailableCampaigns() {
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction"));
    given(campFactionService.getAvailableCampaigns("faction"))
      .willReturn(List.of("campaign"));
    endpoints.getAvailableCampaigns(ctx);

    verify(ctx).json(List.of("campaign"));
  }

  @Test
  void testGetAllFactions() {
    var situation = sampleSituation();
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(campFactionService.getAllFactions("campaign1"))
      .willReturn(List.of(situation));

    endpoints.getAllFactions(ctx);

    verify(ctx).json(List.of(situation));
  }

  @Test
  void testGetAlliedFactions() {
    var situation = sampleSituation();
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction1"));
    given(campFactionService.getAlliedFactions("campaign1"))
      .willReturn(List.of(situation));

    endpoints.getAlliedFactions(ctx);

    verify(ctx).json(List.of(situation));
  }

  @Test
  void testEnemyLocations() {
    var location = mock(Location.class);
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(campFactionService.getEnemyFactionLocations("campaign1"))
      .willReturn(List.of(location));

    endpoints.getEnemyFactionLocations(ctx);

    verify(ctx).json(List.of(location));
  }

  @Test
  void testMoveUnit() {
    var uuid = UUID.randomUUID();
    var location = mock(Location.class);
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction1"));
    given(ctx.pathParam("unitid", String.class))
      .willReturn(Validator.create(String.class, uuid.toString()));
    given(ctx.bodyAsClass(Location.class))
      .willReturn(location);

    endpoints.moveUnit(ctx);

    verify(campFactionService).moveUnit("campaign1", "faction1", uuid, location);
  }

  @Test
  void testDeleteRecoShot() {
    var id = UUID.randomUUID();
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction1"));
    given(ctx.pathParam("id", String.class))
      .willReturn(Validator.create(String.class, id.toString()));

    endpoints.deleteRecoShot(ctx);

    verify(campFactionService).deleteRecoShot("campaign1", "faction1", id);
  }

  @Test
  void testGetRecoShots() {
    given(ctx.pathParam("campaign", String.class))
      .willReturn(Validator.create(String.class, "campaign1"));
    given(ctx.pathParam("faction", String.class))
      .willReturn(Validator.create(String.class, "faction1"));

    var recoshot = ImmutableRecoShot.builder()
      .id(UUID.randomUUID())
      .minLat(ZERO)
      .maxLat(ZERO)
      .minLon(ZERO)
      .maxLon(ZERO)
      .units(List.of())
      .build();
    given(campFactionService.getRecoShots("campaign1", "faction1"))
      .willReturn(List.of(recoshot));

    endpoints.getRecoShots(ctx);

    verify(ctx).json(List.of(recoshot));
  }

  FactionSituation sampleSituation() {
    return ImmutableFactionSituation.builder()
      .id(UUID.randomUUID())
      .campaign("campaign1")
      .faction("faction1")
      .airbases(List.of(ImmutableFactionAirbase.builder()
        .name(ANAPA.name())
        .code(ANAPA)
        .coalition(RED)
        .waypoints(List.of())
        .warehouse(makeSampleWarehouseMap())
        .build()))
      .units(List.of(makeSampleFactionUnit()))
      .credits(ZERO)
      .zoneSizeFt(50_000)
      .build();
  }
}
