package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptionsUnit;
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

    return buyUnitTransactionally(campaignUnit, campaignName, factionName,
      confineUnitWithinFactionZone(campaignName, factionName, unit));
  }

  FactionUnit confineUnitWithinFactionZone(String campaignName, String factionName, FactionUnit unit) {
    var cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);
    var confinedLocation = MathService.shrinkToCircle(
      cf.airbase().location(),  cf.zoneSizeFt(), unit.location());

    unit = ImmutableFactionUnit.builder()
      .from(unit)
      .location(confinedLocation)
      .build();
    return unit;
  }

  GameOptionsUnit findUnitOptionsFromCampaign(String campaignName, FactionUnit unit) {
    var campaign = campaignRepository.find(campaignName);
    var campaignUnit = campaign.gameOptions().units().stream()
      .filter(unitWithCostGreaterThanZero())
      .filter(unitMatchingType(unit))
      .findFirst()
      .orElseThrow(() -> new UnitNotAllowedInCampaignException());
    return campaignUnit;
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

    // Reusing the handle in order to stay in the same
    // transaction with all the operations.
    return purchaseRepository.buyUnit(
      campaignName, factionName,
      campaUnit.cost(),
      unit);
  }

  static boolean enoughCreditsToBuyUnit(GameOptionsUnit unit, BigDecimal credits) {
    return credits.compareTo(unit.cost()) >= 0;
  }


}
