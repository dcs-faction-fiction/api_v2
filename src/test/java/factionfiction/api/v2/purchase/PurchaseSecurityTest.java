package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.Location;
import static base.game.warehouse.WarehouseItemCode.JF_17;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.faction.FactionRepository;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PurchaseSecurityTest {

  UUID owner;
  CampaignFaction cf;
  FactionUnit unit;
  String campaignName;
  String factionName;
  AuthInfo authInfo;
  PurchaseServiceImpl impl;
  CampaignRepository campaignRepository;
  FactionRepository factionRepository;
  PurchaseSecurity security;

  @BeforeEach
  void setup() {
    owner = UUID.randomUUID();
    cf = makeSampleCampaignFaction();
    unit = makeSampleFactionUnit();
    campaignName = cf.campaignName();
    factionName = cf.factionName();
    authInfo = mock(AuthInfo.class);
    impl = mock(PurchaseServiceImpl.class);
    campaignRepository = mock(CampaignRepository.class);
    factionRepository = mock(FactionRepository.class);
    security = new PurchaseSecurity(impl, campaignRepository, factionRepository, authInfo);
  }

  @Test
  void testGiveCredits() {
    mockCorrectCampaignSecurity();
    given(impl.giveCredits(campaignName, factionName, ONE))
      .willReturn(ZERO);

    var result = security.giveCredits(campaignName, factionName, ONE);

    assertThat(result, is(ZERO));
    verify(impl).giveCredits(campaignName, factionName, ONE);
  }

  @Test
  void testBuyUnit() {
    mockCorrectFactionSecurity();
    given(impl.buyUnit(campaignName, factionName, unit))
      .willReturn(unit);

    var result = security.buyUnit(campaignName, factionName, unit);

    assertThat(result, is(unit));
    verify(impl).buyUnit(campaignName, factionName, unit);
  }

  @Test
  void testBuyWarehouseItem() {
    mockCorrectFactionSecurity();

    security.buyWarehouseItem(campaignName, factionName, JF_17);

    verify(impl).buyWarehouseItem(campaignName, factionName, JF_17);
  }

  @Test
  void testIncreaseZone() {
    mockCorrectFactionSecurity();

    security.zoneIncrease(campaignName, factionName);

    verify(impl).zoneIncrease(campaignName, factionName);
  }

  @Test
  void testDecreaseZone() {
    mockCorrectFactionSecurity();

    security.zoneDecrease(campaignName, factionName);

    verify(impl).zoneDecrease(campaignName, factionName);
  }

  @Test
  void testBuyRecoShot() {
    mockCorrectFactionSecurity();

    security.buyRecoShot(campaignName, factionName, Location.of("1", "2"));

    verify(impl).buyRecoShot(campaignName, factionName, Location.of("1", "2"));
  }

  @Test
  void testGiveCreditsNoManager() {
    mockNoCampaignManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.giveCredits(campaignName, factionName, ONE);
    });
  }

  @Test
  void testBuyUnitNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  void testBuyWarehouseItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyWarehouseItem(campaignName, factionName, JF_17);
    });
  }

  @Test
  void testIncreaseZoneItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneIncrease(campaignName, factionName);
    });
  }

  @Test
  void testDecreaseZoneItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneDecrease(campaignName, factionName);
    });
  }

  @Test
  void testBuyRecoShotNoManager() {
    mockNoFactionManager();
    var loc = Location.of("1", "2");

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyRecoShot(campaignName, factionName, loc);
    });
  }

  @Test
  void testGiveCreditsNoOwner() {
    mockNoCampaignOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.giveCredits(campaignName, factionName, ONE);
    });
  }

  @Test
  void testBuyUnitNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  void testBuyWarehouseItemNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyWarehouseItem(campaignName, factionName, JF_17);
    });
  }

  @Test
  void testIncreaseZoneNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneIncrease(campaignName, factionName);
    });
  }

  @Test
  void testDecreaseZoneItemNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneDecrease(campaignName, factionName);
    });
  }
  @Test
  void testBuyRecoShotNoOwner() {
    mockNoFactionOwner();
    var loc = Location.of("1", "2");

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyRecoShot(campaignName, factionName, loc);
    });
  }

  void mockCorrectCampaignSecurity() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(campaignName, owner))
      .willReturn(true);
  }

  void mockNoCampaignManager() {
    given(authInfo.isCampaignManager()).willReturn(false);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(campaignName, owner))
      .willReturn(false);
  }

  void mockNoCampaignOwner() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(campaignName, owner))
      .willReturn(false);
  }

  void mockCorrectFactionSecurity() {
    given(authInfo.isFactionManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(factionRepository.isOwner(factionName, owner))
      .willReturn(true);
  }

  void mockNoFactionManager() {
    given(authInfo.isFactionManager()).willReturn(false);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(factionRepository.isOwner(factionName, owner))
      .willReturn(false);
  }

  void mockNoFactionOwner() {
    given(authInfo.isFactionManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(factionRepository.isOwner(factionName, owner))
      .willReturn(false);
  }

}
