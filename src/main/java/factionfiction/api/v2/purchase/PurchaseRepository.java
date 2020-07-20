package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.warehouse.WarehouseItemCode;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.units.UnitRepository;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class PurchaseRepository {

  final Jdbi jdbi;
  final UnitRepository unitRepository;
  final CampaignFactionRepository campaignFactionRepository;

  public PurchaseRepository(UnitRepository unitRepository, CampaignFactionRepository campaignFactionRepository, Jdbi jdbi) {
    this.unitRepository = unitRepository;
    this.campaignFactionRepository = campaignFactionRepository;
    this.jdbi = jdbi;
  }

  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    return jdbi.withHandle(h -> {
      addCreditsWithHandle(campaignName, factionName, credits, h);
      return getCreditsWithHandle(campaignName, factionName, h);
    });
  }

  public BigDecimal getCredits(String campaignName, String factionName) {
    return jdbi.withHandle(h -> h.select("select credits from campaign_faction where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(BigDecimal.class)
      .findFirst()
      .orElse(ZERO)
    );
  }

  public static BigDecimal getCreditsWithHandle(String campaignName, String factionName, Handle h) {
    return h.select("select credits from campaign_faction where campaign_name = ? and faction_name = ?",
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

  public void zoneIncrease(String campaignName, String factionName, GameOptions options) {
    jdbi.useHandle(h -> {
      UUID cfid = h.select("select id from campaign_faction "
        + "where campaign_name = ? and faction_name = ? "
        + "for update",
        campaignName, factionName)
        .mapTo(UUID.class)
        .first();

      var creditsAvailable = h.select(
        "select credits from campaign_faction where id = ?", cfid)
        .mapTo(BigDecimal.class).findFirst().orElse(ZERO);
      if (options.zones().increase().cost().compareTo(creditsAvailable) > 0) {
        throw new NotEnoughCreditsException();
      }

      h.execute("update campaign_faction set "
        + "zone_size_ft = zone_size_ft + ?, "
        + "credits = greatest(0, credits - ?) "
        + "where id = ?",
        options.zones().increase().amount(), options.zones().increase().cost(), cfid);
    });
  }

  public void zoneDecrease(String campaignName, String factionName, GameOptions options) {
    jdbi.useHandle(h -> {
      UUID cfid = h.select("select id from campaign_faction "
        + "where campaign_name = ? and faction_name = ? "
        + "for update",
        campaignName, factionName)
        .mapTo(UUID.class)
        .first();

      var sizeAvailable = h.select(
        "select zone_size_ft from campaign_faction where id = ?", cfid)
        .mapTo(Integer.class).findFirst().orElse(0);
      if (sizeAvailable <= options.zones().sizes().min()) {
        throw new ZoneAtMinumum();
      }

      h.execute("update campaign_faction set "
        + "zone_size_ft = zone_size_ft - ?, "
        + "credits = greatest(0, credits + ?) "
        + "where id = ?",
        options.zones().decrease().amount(), options.zones().decrease().cost(), cfid);
    });
  }

  void buyItem(String campaignName, String factionName, BigDecimal cost, WarehouseItemCode code) {
    jdbi.useHandle(h -> {
      addCreditsWithHandle(
        campaignName, factionName,
        cost.negate(),
        h);
      addNewItem(campaignName, factionName, code, h);
    });
  }

  private static int addCreditsWithHandle(String campaignName, String factionName, BigDecimal credits, Handle h) {
    return h.execute("update campaign_faction set credits = greatest(0, credits + ?) where campaign_name = ? and faction_name = ?",
      credits,
      campaignName,
      factionName);
  }

  private void addNewUnit(UUID id, String campaignName, String factionName, FactionUnit unit, Handle h) {
    UUID cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
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

  private void addNewItem(String campaignName, String factionName, WarehouseItemCode code, Handle h) {
    UUID cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
    var cf = campaignFactionRepository.getCampaignFaction(cfId);
    UUID cawid = getWarehouseId(h, campaignName, cf);
    UUID itemid = getWarehouseItemId(h, cawid, code);
    h.execute(
      "update campaign_airfield_warehouse_item set item_quantity = item_quantity + 1 where id = ?",
      itemid
    );
  }

  UUID getWarehouseItemId(Handle h, UUID cawid, WarehouseItemCode code) {
    return h.select(
      "select id from campaign_airfield_warehouse_item where warehouse_id = ? and item_code = ?",
      cawid,
      code)
      .mapTo(UUID.class)
      .findFirst()
      .orElseGet(() -> {
        UUID newid = UUID.randomUUID();
        h.execute("insert into campaign_airfield_warehouse_item (id, warehouse_id, item_code, item_quantity)"
          + " values(?, ?, ?, 0)",
          newid,
          cawid,
          code);
        return newid;
      });
  }

  UUID getWarehouseId(Handle h, String campaignName, CampaignFaction cf) {
    return h.select(
      "select id from campaign_airfield_warehouse where"
        + " campaign_name = ? and airbase = ?",
      campaignName,
      cf.airbase())
      .mapTo(UUID.class)
      .findFirst()
      .orElseGet(() -> {
        UUID newid = UUID.randomUUID();
        h.execute("insert into campaign_airfield_warehouse (id, campaign_name, airbase)"
          + " values(?, ?, ?)",
          newid,
          campaignName,
          cf.airbase()
        );
        return newid;
      });
  }
}
