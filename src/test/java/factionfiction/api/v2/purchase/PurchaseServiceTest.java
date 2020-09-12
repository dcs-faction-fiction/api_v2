package factionfiction.api.v2.purchase;

import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.Location;
import static base.game.units.Unit.ABRAMS;
import static base.game.units.Unit.T_80;
import static base.game.warehouse.WarehouseItemCode.AV_8B_NA;
import static base.game.warehouse.WarehouseItemCode.JF_17;
import static base.game.warehouse.WarehouseItemCode.UH_1H;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaign.ImmutableCampaign;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.ImmutableGameOptions;
import factionfiction.api.v2.game.ImmutableGameOptionsUnit;
import factionfiction.api.v2.game.ImmutableGameOptionsWarehouseItem;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
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

class PurchaseServiceTest {

  UUID unitUUID;
  String campaignName;
  String factionName;
  PurchaseServiceImpl service;
  PurchaseRepository purchaseRepository;
  CampaignRepository campaignRepository;
  CampaignFactionRepository campaignFactionRepository;

  @BeforeEach
  void setup() {
    unitUUID = UUID.randomUUID();
    campaignName = "Campaign";
    factionName = "Faction";
    purchaseRepository = mock(PurchaseRepository.class);
    campaignRepository = mock(CampaignRepository.class);
    campaignFactionRepository = mock(CampaignFactionRepository.class);
    service = new PurchaseServiceImpl(
      purchaseRepository,
      campaignRepository,
      campaignFactionRepository);
  }

  @Test
  void testGiveCredits() {
    given(purchaseRepository.giveCredits(campaignName, factionName, ONE))
      .willReturn(ONE);

    var result = service.giveCredits(
      campaignName,
      factionName,
      ONE);

    assertThat(result, is(ONE));
  }

  @Test
  void testBuyUnit() throws IOException {
    var unit = mockBuyUnitSituation(new BigDecimal(30));

    var result = service.buyUnit(campaignName, factionName, unit);

    assertThat(result, is(unit));
  }

  @Test
  void testBuyUnitOutsideAirbase() throws IOException {
    var units = mockBuyUnitSituationOutsideAirbase(new BigDecimal(30));
    var unit = units.get(0);
    var changedUnit = units.get(1);

    var result = service.buyUnit(campaignName, factionName, unit);

    assertThat(result, is(changedUnit));
  }

  @Test
  void testBuyUnitWithNotEnoughCredits() throws IOException {
    var unit = mockBuyUnitSituation(new BigDecimal(0));

    assertThrows(NotEnoughCreditsException.class, () -> {
      service.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  void testBuyUnitWithNotAllowedUnit() throws IOException {
    var unit = mockBuyUnitSituationUnitNotAllowed(new BigDecimal(0));

    assertThrows(UnitNotAllowedInCampaignException.class, () -> {
      service.buyUnit(campaignName, factionName, unit);
    });
  }

  @Test
  void testBuyWarehouseItem() throws IOException {
    mockBuyItemSituation(new BigDecimal(30));

    service.buyWarehouseItem(campaignName, factionName, JF_17);

    verify(purchaseRepository)
      .buyItem(campaignName, factionName, new BigDecimal(2), JF_17);
  }

  @Test
  void testBuyWarehouseItemNoCredits() throws IOException {
    mockBuyItemSituation(new BigDecimal(0));

    assertThrows(NotEnoughCreditsException.class, () -> {
      service.buyWarehouseItem(campaignName, factionName, JF_17);
    });
  }

  @Test
  void testBuyWarehouseItemNotAllowedItem() throws IOException {
    mockBuyItemSituation(new BigDecimal(30));

    assertThrows(WarehouseItemNotAllowedInCampaignException.class, () -> {
      service.buyWarehouseItem(campaignName, factionName, UH_1H);
    });
  }

  @Test
  void testPassIncrease() throws IOException {
    var c = makeSampleCampaign();
    given(campaignRepository.find("c"))
      .willReturn(c);

    service.zoneIncrease("c", "f");

    verify(purchaseRepository).zoneIncrease("c", "f", c.gameOptions());
  }

  @Test
  void testPassDecrease() throws IOException {
    var c = makeSampleCampaign();
    given(campaignRepository.find("c"))
      .willReturn(c);

    service.zoneDecrease("c", "f");

    verify(purchaseRepository).zoneDecrease("c", "f", c.gameOptions());
  }

  @Test
  void testPassBuyRecoShot() throws IOException {
    var c = makeSampleCampaign();
    given(campaignRepository.find("c"))
      .willReturn(c);

    service.buyRecoShot("c", "f", Location.of("1", "2"));

    verify(purchaseRepository).buyRecoShot("c", "f", Location.of("1", "2"), c.gameOptions());
  }

  ImmutableFactionUnit mockBuyUnitSituation(BigDecimal creditsAvailable) throws IOException {
    var cfId = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    campaign = ImmutableCampaign.builder()
      .from(campaign)
      .gameOptions(ImmutableGameOptions.builder()
        .from(campaign.gameOptions())
        .units(List.of(ImmutableGameOptionsUnit.builder()
          .code(T_80)
          .cost(ZERO)
        .build(), ImmutableGameOptionsUnit.builder()
          .code(ABRAMS)
          .cost(ONE)
          .build()
        ))
        .build())
      .build();
    var cf = makeSampleCampaignFaction();
    campaignName = cf.campaignName();
    factionName = cf.factionName();
    var unit = ImmutableFactionUnit.builder()
      .type(ABRAMS)
      .location(cf.airbase().location())
      .build();
    var cost = campaign.gameOptions().units().stream()
      .filter(u -> u.code() == unit.type())
      .filter(u -> u.cost().compareTo(ZERO) > 0)
      .findFirst()
      .orElseThrow(() -> new UnitNotAllowedInCampaignException())
      .cost();
    given(campaignRepository.find(cf.campaignName())).willReturn(campaign);
    given(campaignFactionRepository.getCampaignFactionId(cf.campaignName(), cf.factionName())).willReturn(cfId);
    given(campaignFactionRepository.getCampaignFaction(cfId)).willReturn(cf);
    given(purchaseRepository.buyUnit(cf.campaignName(), cf.factionName(), cost, unit)).willReturn(unit);
    given(purchaseRepository.getCredits(campaignName, factionName)).willReturn(creditsAvailable);
    given(purchaseRepository.buyUnit(campaignName, factionName, cost, unit)).willReturn(unit);
    return unit;
  }

  List<ImmutableFactionUnit> mockBuyUnitSituationOutsideAirbase(BigDecimal creditsAvailable) throws IOException {
    var cfId = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    var cf = makeSampleCampaignFaction();
    campaignName = cf.campaignName();
    factionName = cf.factionName();
    var unit = ImmutableFactionUnit.builder()
      .type(ABRAMS)
      .location(ImmutableLocation.builder()
        .latitude(ZERO)
        .longitude(ZERO)
        .altitude(ZERO)
        .angle(ZERO)
        .build())
      .build();
    var cost = campaign.gameOptions().units().stream()
      .filter(u -> u.code() == unit.type())
      .filter(u -> u.cost().compareTo(ZERO) > 0)
      .findFirst()
      .orElseThrow(() -> new UnitNotAllowedInCampaignException())
      .cost();
    given(campaignRepository.find(cf.campaignName())).willReturn(campaign);
    given(campaignFactionRepository.getCampaignFactionId(cf.campaignName(), cf.factionName())).willReturn(cfId);
    given(campaignFactionRepository.getCampaignFaction(cfId)).willReturn(cf);
    given(purchaseRepository.buyUnit(cf.campaignName(), cf.factionName(), cost, unit)).willReturn(unit);
    given(purchaseRepository.getCredits(campaignName, factionName)).willReturn(creditsAvailable);

    var changedUnit = ImmutableFactionUnit.builder()
      .from(unit)
      .location(makeSampleCampaignFaction().airbase().location())
      .build();
    given(purchaseRepository.buyUnit(campaignName, factionName, cost, changedUnit)).willReturn(changedUnit);
    return List.of(unit, changedUnit);
  }

  ImmutableFactionUnit mockBuyUnitSituationUnitNotAllowed(BigDecimal creditsAvailable) throws IOException {
    var cfId = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    campaign = ImmutableCampaign.builder()
      .from(campaign)
      .gameOptions(ImmutableGameOptions.builder()
        .from(campaign.gameOptions())
        .units(List.of())
        .build())
      .build();
    var cf = makeSampleCampaignFaction();
    campaignName = cf.campaignName();
    factionName = cf.factionName();
    var unit = ImmutableFactionUnit.builder()
      .type(ABRAMS)
      .location(cf.airbase().location())
      .build();
    given(campaignRepository.find(cf.campaignName())).willReturn(campaign);
    given(campaignFactionRepository.getCampaignFactionId(cf.campaignName(), cf.factionName())).willReturn(cfId);
    given(campaignFactionRepository.getCampaignFaction(cfId)).willReturn(cf);
    given(purchaseRepository.getCredits(campaignName, factionName)).willReturn(creditsAvailable);
    return unit;
  }

  void mockBuyItemSituation(BigDecimal creditsAvailable) throws IOException {
    var cfId = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    campaign = ImmutableCampaign.builder()
      .from(campaign)
      .gameOptions(ImmutableGameOptions.builder()
        .from(campaign.gameOptions())
        .warehouseItems(List.of(ImmutableGameOptionsWarehouseItem.builder()
          .code(AV_8B_NA)
          .cost(ZERO)
        .build(), ImmutableGameOptionsWarehouseItem.builder()
          .code(JF_17)
          .cost(new BigDecimal(2))
          .build()))
        .build())
      .build();
    var cf = makeSampleCampaignFaction();
    campaignName = cf.campaignName();
    factionName = cf.factionName();
    given(campaignRepository.find(cf.campaignName())).willReturn(campaign);
    given(campaignFactionRepository.getCampaignFactionId(cf.campaignName(), cf.factionName())).willReturn(cfId);
    given(campaignFactionRepository.getCampaignFaction(cfId)).willReturn(cf);
    given(purchaseRepository.getCredits(campaignName, factionName)).willReturn(creditsAvailable);
  }
}
