package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.Location;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptionsUnit;
import factionfiction.api.v2.game.GameOptionsWarehouseItem;
import factionfiction.api.v2.math.MathService;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.util.function.Predicate;

public class PurchaseServiceImpl {

  final PurchaseRepository purchaseRepository;
  final CampaignRepository campaignRepository;
  final CampaignFactionRepository campaignFactionRepository;

  public PurchaseServiceImpl(
    PurchaseRepository purchaseRepository,
    CampaignRepository campaignRepository,
    CampaignFactionRepository campaignFactionRepository) {

    this.purchaseRepository = purchaseRepository;
    this.campaignRepository = campaignRepository;
    this.campaignFactionRepository = campaignFactionRepository;
  }

  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    return purchaseRepository.giveCredits(campaignName, factionName, credits);
  }

  public FactionUnit buyUnit(String campaignName, String factionName, FactionUnit unit) {
    var campaignUnit = findUnitOptionsFromCampaign(campaignName, unit);
    var cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);

    if (cf.airbase().carrier())
      throw new UnitNotAllowedInCampaignException();

    return buyUnitTransactionally(campaignUnit, campaignName, factionName,
      confineUnitWithinFactionZone(campaignName, factionName, unit));
  }

  public void buyWarehouseItem(String campaignName, String factionName, WarehouseItemCode item) {
    buyItemTransactionally(
      findWarehouseOptionsFromCampaign(campaignName, item),
      campaignName, factionName);
  }

  public void zoneIncrease(String campaign, String faction) {
    var c = campaignRepository.find(campaign);
    var cfId = campaignFactionRepository.getCampaignFactionId(campaign, faction);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);

    if (cf.airbase().farp())
      return;
    if (cf.airbase().carrier())
      return;

    purchaseRepository.zoneIncrease(campaign, faction, c.gameOptions());
  }

  public void zoneDecrease(String campaign, String faction) {
    var c = campaignRepository.find(campaign);
    var cfId = campaignFactionRepository.getCampaignFactionId(campaign, faction);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);

    if (cf.airbase().farp())
      return;
    if (cf.airbase().carrier())
      return;

    purchaseRepository.zoneDecrease(campaign, faction, c.gameOptions());
  }

  public void buyRecoShot(String campaign, String faction, Location location) {
    var c = campaignRepository.find(campaign);
    purchaseRepository.buyRecoShot(campaign, faction, location, c.gameOptions());
  }

  FactionUnit confineUnitWithinFactionZone(String campaignName, String factionName, FactionUnit unit) {
    var cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);

    if (!MathService.isInCircle(cf.airbase().location(), cf.zoneSizeFt(), unit.location()))
      throw new NotAuthorizedException("Cannot do it outside zone");

    unit = ImmutableFactionUnit.builder()
      .from(unit)
      .location(unit.location())
      .build();
    return unit;
  }

  GameOptionsUnit findUnitOptionsFromCampaign(String campaignName, FactionUnit unit) {
    var campaign = campaignRepository.find(campaignName);
    return campaign.gameOptions().units().stream()
      .filter(unitWithCostGreaterThanZero())
      .filter(unitMatchingType(unit))
      .findFirst()
      .orElseThrow(UnitNotAllowedInCampaignException::new);
  }

  Predicate<GameOptionsUnit> unitWithCostGreaterThanZero() {
    return u -> u.cost().compareTo(ZERO) > 0;
  }

  Predicate<GameOptionsUnit> unitMatchingType(FactionUnit unit) {
    return u -> u.code() == unit.type();
  }

  FactionUnit buyUnitTransactionally(GameOptionsUnit campaUnit, String campaignName, String factionName, FactionUnit unit) {
    var credits = purchaseRepository.getCredits(campaignName, factionName);
    if (!enoughCreditsToBuyUnit(campaUnit, credits))
      throw new NotEnoughCreditsException();

    return purchaseRepository.buyUnit(
      campaignName, factionName,
      campaUnit.cost(),
      unit);
  }

  static boolean enoughCreditsToBuyUnit(GameOptionsUnit unit, BigDecimal credits) {
    return credits.compareTo(unit.cost()) >= 0;
  }

  GameOptionsWarehouseItem findWarehouseOptionsFromCampaign(String campaignName, WarehouseItemCode item) {
    var campaign = campaignRepository.find(campaignName);
    return campaign.gameOptions().warehouseItems().stream()
      .filter(filterWarehouseItemGreaterThanZero())
      .filter(filterWarehouseItemMatchByCode(item))
      .findFirst()
      .orElseThrow(WarehouseItemNotAllowedInCampaignException::new);
  }

  Predicate<GameOptionsWarehouseItem> filterWarehouseItemMatchByCode( WarehouseItemCode item) {
    return i -> i.code() == item;
  }

  Predicate<GameOptionsWarehouseItem> filterWarehouseItemGreaterThanZero() {
    return i -> i.cost().compareTo(ZERO) > 0;
  }

  void buyItemTransactionally(GameOptionsWarehouseItem campaignWarehouseItem, String campaignName, String factionName) {
    var credits = purchaseRepository.getCredits(campaignName, factionName);
    if (!enoughCreditsToBuyItem(campaignWarehouseItem, credits))
      throw new NotEnoughCreditsException();

    purchaseRepository.buyItem(
      campaignName, factionName,
      campaignWarehouseItem.cost(),
      campaignWarehouseItem.code());
  }

  boolean enoughCreditsToBuyItem(GameOptionsWarehouseItem item, BigDecimal credits) {
    return credits.compareTo(item.cost()) >= 0;
  }
}
