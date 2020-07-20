package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
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

public class PurchaseSecurityTest {

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
  public void setup() {
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
  public void testGiveCredits() {
    mockCorrectCampaignSecurity();
    given(impl.giveCredits(campaignName, factionName, ONE))
      .willReturn(ZERO);

    var result = security.giveCredits(campaignName, factionName, ONE);

    assertThat(result, is(ZERO));
    verify(impl).giveCredits(campaignName, factionName, ONE);
  }

  @Test
  public void testBuyUnit() {
    mockCorrectFactionSecurity();
    given(impl.buyUnit(campaignName, factionName, unit))
      .willReturn(unit);

    var result = security.buyUnit(campaignName, factionName, unit);

    assertThat(result, is(unit));
    verify(impl).buyUnit(campaignName, factionName, unit);
  }

  @Test
  public void testBuyWarehouseItem() {
    mockCorrectFactionSecurity();

    security.buyWarehouseItem(campaignName, factionName, JF_17);

    verify(impl).buyWarehouseItem(campaignName, factionName, JF_17);
  }

  @Test
  public void testIncreaseZone() {
    mockCorrectFactionSecurity();

    security.zoneIncrease(campaignName, factionName);

    verify(impl).zoneIncrease(campaignName, factionName);
  }

  @Test
  public void testDecreaseZone() {
    mockCorrectFactionSecurity();

    security.zoneDecrease(campaignName, factionName);

    verify(impl).zoneDecrease(campaignName, factionName);
  }

  @Test
  public void testGiveCreditsNoManager() {
    mockNoCampaignManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.giveCredits(campaignName, factionName, ONE);
    });
  }

  @Test
  public void testBuyUnitNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  public void testBuyWarehouseItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyWarehouseItem(campaignName, factionName, JF_17);
    });
  }

  @Test
  public void testIncreaseZoneItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneIncrease(campaignName, factionName);
    });
  }

  @Test
  public void testDecreaseZoneItemNoManager() {
    mockNoFactionManager();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneDecrease(campaignName, factionName);
    });
  }

  @Test
  public void testGiveCreditsNoOwner() {
    mockNoCampaignOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.giveCredits(campaignName, factionName, ONE);
    });
  }

  @Test
  public void testBuyUnitNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  public void testBuyWarehouseItemNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.buyWarehouseItem(campaignName, factionName, JF_17);
    });
  }

  @Test
  public void testIncreaseZoneNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneIncrease(campaignName, factionName);
    });
  }

  @Test
  public void testDecreaseZoneItemNoOwner() {
    mockNoFactionOwner();

    assertThrows(NotAuthorizedException.class, () -> {
      security.zoneDecrease(campaignName, factionName);
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
