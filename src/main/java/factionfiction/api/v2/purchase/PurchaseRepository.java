package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.Location;
import base.game.warehouse.WarehouseItemCode;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptions;
import static factionfiction.api.v2.mappers.RowMappers.factionUnitMapper;
import static factionfiction.api.v2.math.MathService.metersToLat;
import static factionfiction.api.v2.math.MathService.metersToLon;
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
      UUID cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
      addCredits(h, cfId, credits);
      return getCreditsWithHandle(campaignName, factionName, h);
    });
  }

  public BigDecimal getCredits(String campaignName, String factionName) {
    return jdbi.withHandle(h -> getCreditsWithHandle(campaignName, factionName, h));
  }

  public FactionUnit buyUnit(String campaignName, String factionName, BigDecimal cost, FactionUnit unit) {
    UUID id = UUID.randomUUID();
    jdbi.useHandle(h -> {
      UUID cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
      spendCreditsOrFail(h, cfId, cost);
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

      checkIfEnoughCretidsToSpend(h, cfid, options.zones().increase().cost());

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

  public void buyRecoShot(String campaign, String faction, Location location, GameOptions gameOptions) {
    UUID cfId = campaignFactionRepository.getCampaignFactionId(campaign, faction);
    var size = (double) gameOptions.zones().recoShot().edgeSize();
    var cost = gameOptions.zones().recoShot().cost();
    var deltaLat = metersToLat(size/2);
    var deltaLon = metersToLon(size/2, location.latitude().doubleValue());
    var latmin = location.latitude().doubleValue() - deltaLat;
    var latmax = location.latitude().doubleValue() + deltaLat;
    var lonmin = location.longitude().doubleValue() - deltaLon;
    var lonmax = location.longitude().doubleValue() + deltaLon;
    jdbi.useHandle(h -> {
      var foundUnits = h.select("select cfu.id as id, cfu.type as type, cfu.x as x, cfu.y as y, cfu.z as z, cfu.angle as angle from campaign_faction_units cfu "
        + "left join campaign_faction cf on cf.id = campaign_faction_id "
        + "where cf.campaign_name = ? and cf.faction_name != ? "
        + "and y between ? and ? "
        + "and x between ? and ?",
        campaign, faction,
        latmin, latmax,
        lonmin, lonmax)
      .map(factionUnitMapper())
      .list();

      UUID recoId = UUID.randomUUID();
      h.execute("insert into recoshot (id, campaign_faction_id, latmin, latmax, lonmin, lonmax) values(?, ?, ?, ?, ?, ?)", recoId, cfId, latmin, latmax, lonmin, lonmax);
      foundUnits.forEach(unit ->
        h.execute("insert into recoshot_item (id, recoshot_id, type, x, y, z, angle) values(?, ?, ?, ?, ?, ?, ?)",
          UUID.randomUUID(), recoId,
          unit.type(), unit.location().longitude(), unit.location().latitude(), unit.location().altitude(), unit.location().angle()
        )
      );

      spendCreditsOrFail(h, cfId, cost);
    });
  }

  void buyItem(String campaignName, String factionName, BigDecimal cost, WarehouseItemCode code) {
    jdbi.useHandle(h -> {
      // Lock the campaignfaction so that purchases are serialized and solve the problem of the insert/upsert
      UUID cfId = campaignFactionRepository.getCampaignFactionId(campaignName, factionName);
      h.execute("select id from campaign_faction where id = ? for update", cfId);
      // buy item and then take credits
      addNewItem(cfId, campaignName, code, h);
      spendCreditsOrFail(h, cfId, cost);
    });
  }

  void addNewUnit(UUID id, String campaignName, String factionName, FactionUnit unit, Handle h) {
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

  void addNewItem(UUID cfId, String campaignName, WarehouseItemCode code, Handle h) {
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

  static BigDecimal getCreditsWithHandle(String campaignName, String factionName, Handle h) {
    return h.select("select credits from campaign_faction where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(BigDecimal.class)
      .findFirst()
      .orElse(ZERO);
  }

  static void spendCreditsOrFail(Handle h, UUID cfId, BigDecimal credits) {
    checkIfEnoughCretidsToSpend(h, cfId, credits);
    addCredits(h, cfId, credits.negate());
  }

  static void addCredits(Handle h, UUID cfId, BigDecimal credits) {
    h.execute("update campaign_faction set credits = greatest(0, credits + ?) where id = ?",
      credits,
      cfId);
  }

  static void checkIfEnoughCretidsToSpend(Handle h, UUID cfid, BigDecimal cost) throws NotEnoughCreditsException {
    var creditsAvailable = h.select(
      "select credits from campaign_faction where id = ?", cfid)
      .mapTo(BigDecimal.class).findFirst().orElse(ZERO);
    if (cost.compareTo(creditsAvailable) > 0) {
      throw new NotEnoughCreditsException();
    }
  }
}
