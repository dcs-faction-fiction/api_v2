package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import base.game.FactionSituation;
import base.game.ImmutableFactionSituation;
import base.game.Location;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaign.ImmutableCampaign;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import java.math.BigDecimal;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CampaignFactionSecurityTest {

  CampaignFaction sample;
  UUID owner;
  AuthInfo authInfo;
  CampaignFactionServiceImpl impl;
  CampaignFactionSecurity security;
  CampaignRepository campaignRepository;
  FactionRepository factionRepository;

  @BeforeEach
  void setup() {
    owner = UUID.randomUUID();
    sample = makeSampleCampaignFaction();
    authInfo = mock(AuthInfo.class);
    impl = mock(CampaignFactionServiceImpl.class);
    campaignRepository = mock(CampaignRepository.class);
    factionRepository = mock(FactionRepository.class);
    security = new CampaignFactionSecurity(impl, campaignRepository, factionRepository, authInfo);
  }

  @Test
  void testCreateCampaignFaction() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(true);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    var result = security.newCampaignFaction(sample);

    assertThat(result, is(sample));
  }

  @Test
  void testCreateCampaignFactionNotOwned() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  void testCannotCreateCampaignFactionWithoutPermission() {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  void testCannotCreateCampaignFactionWithoutOwner() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  void testCanGetSituationCampaignCasesNone() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);
    var campaign = sample.campaignName();
    var faction = sample.factionName();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(campaign, faction);
    });
  }

  @Test
  void testCanGetSituationCampaignCasesCampaign() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockCampaignManagerAndCampaignOwner();
    mockOwnerAndResponse(situation);

    var result = security.getSituation(sample.campaignName(), sample.factionName());

    assertThat(result, is(situation));
  }

  @Test
  void testCanGetSituationCampaignCasesFaction() {
    var situation = makeSituation();
    mockFactionManagerAndFactionOwner();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);

    var result = security.getSituation(sample.campaignName(), sample.factionName());

    assertThat(result, is(situation));
  }

  @Test
  void testCanGetSituationCampaignCasesCampaignNoOwner() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockCampaignManagerAndNoCampaignOwner();
    mockOwnerAndResponse(situation);
    var campaign = sample.campaignName();
    var faction = sample.factionName();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(campaign, faction);
    });
  }

  @Test
  void testCanGetSituationCampaignCasesFactionNoOwner() {
    var situation = makeSituation();
    mockFactionManagerAndNoFactionOwner();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);
    var campaign = sample.campaignName();
    var faction = sample.factionName();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(campaign, faction);
    });
  }

  @Test
  void testCanGetGameOptions() throws IOException {
    var options = makeSampleOptions();
    mockGameOptions(options);
    mockFactionManagerAndFactionOwner();
    mockNoCamapignManager();

    var result = security.getGameOptions(sample.campaignName(), sample.factionName());

    assertThat(result, is(options));
  }

  @Test
  void testCannotGetGameOptions() throws IOException {
    var options = makeSampleOptions();
    mockGameOptions(options);
    mockNoFactionManager();
    mockNoCamapignManager();
    var campaign = sample.campaignName();
    var faction = sample.factionName();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getGameOptions(campaign, faction);
    });
  }

  @Test
  void testGetAvailableCampaigns() {
    given(campaignRepository.getAvailableCampaignsForFaction("faction"))
      .willReturn(List.of("campaign"));

    var result = security.getAvailableCampaigns("faction");

    assertThat(result, is(List.of("campaign")));
  }

  @Test
  void testGetAllFactions() {
    var expected = List.of(mock(FactionSituation.class));
    mockCampaignManagerAndCampaignOwner();
    given(impl.getAllFactions("campaign", owner))
      .willReturn(expected);

    var result = security.getAllFactions("campaign");

    assertThat(result, is(expected));
  }

  @Test
  void testGetAllFactionsNoCampaignManager() {
    mockNoCamapignManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getAllFactions("campaign");
    });
  }

  @Test
  void testGetAlliedFactions() {
    var expected = List.of(mock(FactionSituation.class));
    mockFactionManagerAndFactionOwner();
    given(impl.getAlliedFactions("campaign", owner))
      .willReturn(expected);

    var result = security.getAlliedFactions("campaign");

    assertThat(result, is(expected));
  }

  @Test
  void testGetAlliedFactionsNoFactionManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getAlliedFactions("campaign");
    });;
  }

  @Test
  void testGetEnemyFactions() {
    var expected = List.of(mock(Location.class));
    mockFactionManagerAndFactionOwner();
    given(impl.getEnemyFactionLocations("campaign", owner))
      .willReturn(expected);

    var result = security.getEnemyFactionLocations("campaign");

    assertThat(result, is(expected));
  }

  @Test
  void testGetEnemyFactionsNoFactionManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getEnemyFactionLocations("campaign");
    });;
  }

  @Test
  void testMoveUnit() {
    var unitId = UUID.randomUUID();
    var newLocation = mock(Location.class);
    mockFactionManagerAndFactionOwner();

    security.moveUnit(sample.campaignName(), sample.factionName(), unitId, newLocation);

    verify(impl).moveUnit(sample.campaignName(), sample.factionName(), unitId, newLocation);
  }

  @Test
  void testMoveUnitNoFactionManager() {
    mockNoFactionManager();
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var id = UUID.randomUUID();
    var loc = ANAPA.location();

    assertThrows(NotAuthorizedException.class, () -> {
      security.moveUnit(campaign, faction, id, loc);
    });
  }

  @Test
  void testMoveUnitNoFactionOwner() {
    mockFactionManagerAndNoFactionOwner();
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var id = UUID.randomUUID();
    var loc = ANAPA.location();

    assertThrows(NotAuthorizedException.class, () -> {
      security.moveUnit(campaign, faction, id, loc);
    });;
  }

  @Test
  void testDeleteRecoShot() {
    mockFactionManagerAndFactionOwner();
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var id = UUID.randomUUID();

    security.deleteRecoShot(campaign, faction, id);

    verify(impl).deleteRecoShot(id);
  }

  @Test
  void testDeleteRecoShotNoFactionManager() {
    mockNoFactionManager();
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var id = UUID.randomUUID();

    assertThrows(NotAuthorizedException.class, () -> {
      security.deleteRecoShot(campaign, faction, id);
    });;
  }

  @Test
  void testDeleteRecoShotNoFactionOwner() {
    mockFactionManagerAndNoFactionOwner();
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var id = UUID.randomUUID();

    assertThrows(NotAuthorizedException.class, () -> {
      security.deleteRecoShot(campaign, faction, id);
    });;
  }

  void mockGameOptions(GameOptions options) {
    given(campaignRepository.find(sample.campaignName()))
      .willReturn(ImmutableCampaign.builder()
        .name(sample.campaignName())
        .gameOptions(options)
        .build());
  }

  static GameOptions makeSampleOptions() throws IOException {
    return new GameOptionsLoader().loadDefaults();
  }

  void mockOwnerAndResponse(FactionSituation situation) {
    given(impl.getSituation(sample.campaignName(), sample.factionName()))
      .willReturn(situation);
  }

  void mockNoFactionManager() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isFactionManager()).willReturn(false);
    given(factionRepository.isOwner(sample.factionName(), owner))
      .willReturn(false);
  }

  void mockNoCamapignManager() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isCampaignManager()).willReturn(false);
    given(campaignRepository.isOwner(sample.factionName(), owner))
      .willReturn(false);
  }

  void mockFactionManagerAndFactionOwner() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isFactionManager()).willReturn(true);
    given(factionRepository.isOwner(sample.factionName(), owner))
      .willReturn(true);
  }

  void mockCampaignManagerAndCampaignOwner() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(campaignRepository.isOwner(sample.campaignName(), owner))
      .willReturn(true);
  }

  void mockFactionManagerAndNoFactionOwner() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isFactionManager()).willReturn(true);
    given(factionRepository.isOwner(sample.factionName(), owner))
      .willReturn(false);
  }

  void mockCampaignManagerAndNoCampaignOwner() {
    given(authInfo.getUserUUID()).willReturn(owner);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(campaignRepository.isOwner(sample.campaignName(), owner))
      .willReturn(false);
  }

  FactionSituation makeSituation() {
    return ImmutableFactionSituation.builder()
      .id(UUID.randomUUID())
      .campaign(sample.campaignName())
      .faction(sample.factionName())
      .credits(new BigDecimal(30))
      .zoneSizeFt(50_000)
      .airbases(emptyList())
      .units(emptyList())
      .build();
  }
}
