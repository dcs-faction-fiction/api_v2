package factionfiction.api.v2.units.purchase;

import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import org.jdbi.v3.core.Jdbi;

public class PurchaseRepository {

  final Jdbi jdbi;

  public PurchaseRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    return jdbi.withHandle(h -> {
      h.execute("update campaign_faction"
        + " set credits = greatest(0, credits + ?)"
        + " where campaign_name = ? and faction_name = ?",
        credits,
        campaignName,
        factionName);
      return h.select("select credits from campaign_faction"
        + " where campaign_name = ? and faction_name = ?",
        campaignName,
        factionName)
        .mapTo(BigDecimal.class)
        .findFirst()
        .orElse(ZERO);
    });
  }

}
