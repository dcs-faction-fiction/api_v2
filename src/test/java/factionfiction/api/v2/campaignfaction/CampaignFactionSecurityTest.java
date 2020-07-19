package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.ImmutableFactionSituation;
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

public class CampaignFactionSecurityTest {

  CampaignFaction sample;
  UUID owner;
  AuthInfo authInfo;
  CampaignFactionServiceImpl impl;
  CampaignFactionSecurity security;
  CampaignRepository campaignRepository;
  FactionRepository factionRepository;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    sample = makeSampleCampaignFaction();
    authInfo = mock(AuthInfo.class);
    impl = mock(CampaignFactionServiceImpl.class);
    campaignRepository = mock(CampaignRepository.class);
    factionRepository = mock(FactionRepository.class);
    security = new CampaignFactionSecurity(impl, campaignRepository, factionRepository, authInfo);
  }

  @Test
  public void testCreateCampaignFaction() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(true);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    var result = security.newCampaignFaction(sample);

    assertThat(result, is(sample));
  }

  @Test
  public void testCreateCampaignFactionNotOwned() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  public void testCannotCreateCampaignFactionWithoutPermission() {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  public void testCannotCreateCampaignFactionWithoutOwner() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  public void testCanGetSituationCampaignCasesNone() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(sample.campaignName(), sample.factionName());
    });
  }

  @Test
  public void testCanGetSituationCampaignCasesCampaign() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockCampaignManagerAndCampaignOwner();
    mockOwnerAndResponse(situation);

    var result = security.getSituation(sample.campaignName(), sample.factionName());

    assertThat(result, is(situation));
  }

  @Test
  public void testCanGetSituationCampaignCasesFaction() {
    var situation = makeSituation();
    mockFactionManagerAndFactionOwner();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);

    var result = security.getSituation(sample.campaignName(), sample.factionName());

    assertThat(result, is(situation));
  }

  @Test
  public void testCanGetSituationCampaignCasesCampaignNoOwner() {
    var situation = makeSituation();
    mockNoFactionManager();
    mockCampaignManagerAndNoCampaignOwner();
    mockOwnerAndResponse(situation);

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(sample.campaignName(), sample.factionName());
    });
  }

  @Test
  public void testCanGetSituationCampaignCasesFactionNoOwner() {
    var situation = makeSituation();
    mockFactionManagerAndNoFactionOwner();
    mockNoCamapignManager();
    mockOwnerAndResponse(situation);

    assertThrows(NotAuthorizedException.class, () -> {
      security.getSituation(sample.campaignName(), sample.factionName());
    });
  }

  @Test
  public void testCanGetGameOptions() throws IOException {
    var options = makeSampleOptions();
    mockGameOptions(options);
    mockFactionManagerAndFactionOwner();
    mockNoCamapignManager();

    var result = security.getGameOptions(sample.campaignName(), sample.factionName());

    assertThat(result, is(options));
  }

  @Test
  public void testCannotGetGameOptions() throws IOException {
    var options = makeSampleOptions();
    mockGameOptions(options);
    mockNoFactionManager();
    mockNoCamapignManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.getGameOptions(sample.campaignName(), sample.factionName());
    });
  }

  @Test
  public void testGetAvailableCampaigns() {
    given(campaignRepository.getAvailableCampaignsForFaction("faction"))
      .willReturn(List.of("campaign"));

    var result = security.getAvailableCampaigns("faction");

    assertThat(result, is(List.of("campaign")));
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
