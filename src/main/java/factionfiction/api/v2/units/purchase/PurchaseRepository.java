package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import com.github.apilab.rest.exceptions.NotFoundException;
import factionfiction.api.v2.units.UnitRepository;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class PurchaseRepository {

  final Jdbi jdbi;
  final UnitRepository unitRepository;

  public PurchaseRepository(UnitRepository unitRepository, Jdbi jdbi) {
    this.unitRepository = unitRepository;
    this.jdbi = jdbi;
  }

  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    return jdbi.withHandle(h -> {
      addCreditsWithHandle(campaignName, factionName, credits, h);
      return getCreditsWithHandle(campaignName, factionName, h);
    });
  }

  public BigDecimal getCredits(String campaignName, String factionName) {
    return jdbi.withHandle(h -> h.select("select credits from campaign_faction"
      + " where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(BigDecimal.class)
      .findFirst()
      .orElse(ZERO)
    );
  }

  public static BigDecimal getCreditsWithHandle(String campaignName, String factionName, Handle h) {
    return h.select("select credits from campaign_faction"
      + " where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(BigDecimal.class)
      .findFirst()
      .orElse(ZERO);
  }

  public FactionUnit buyUnit(String campaignName, String factionName, BigDecimal cost, FactionUnit unit) {
    UUID id = UUID.randomUUID();
    jdbi.useHandle(h -> {
      addCreditsWithHandle(
        campaignName, factionName,
        cost.negate(),
        h);
      addNewUnit(id, campaignName, factionName, unit, h);
    });

    return unitRepository.getUnit(id);
  }

  private static int addCreditsWithHandle(String campaignName, String factionName, BigDecimal credits, Handle h) {
    return h.execute("update campaign_faction"
      + " set credits = greatest(0, credits + ?)"
      + " where campaign_name = ? and faction_name = ?",
      credits,
      campaignName,
      factionName);
  }

  private static void addNewUnit(UUID id, String campaignName, String factionName, FactionUnit unit, Handle h) {
    var cfId = h.select(
      "select id from campaign_faction where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(UUID.class)
      .findFirst()
      .orElseThrow(() -> new NotFoundException("Campaing-Faction not found"));
    h.execute(
      "insert into campaign_faction_units"
        + " (id, campaign_faction_id, type, x, y, z, angle)"
        + " values(?, ?, ?, ?, ?, ?, ?)",
      id,
      cfId,
      unit.type(),
      unit.location().longitude(),
      unit.location().latitude(),
      unit.location().altitude(),
      unit.location().angle()
    );
  }

}
