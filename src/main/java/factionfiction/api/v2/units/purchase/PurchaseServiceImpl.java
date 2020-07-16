package factionfiction.api.v2.units.purchase;

import java.math.BigDecimal;

public class PurchaseServiceImpl {

  final PurchaseRepository repository;

  public PurchaseServiceImpl(PurchaseRepository repository) {
    this.repository = repository;
  }

  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    return repository.giveCredits(campaignName, factionName, credits);
  }

}
