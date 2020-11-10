package factionfiction.api.v2.campaignfaction;

import base.game.Airbases;
import static base.game.CampaignCoalition.BLUE;
import static base.game.CampaignCoalition.RED;
import base.game.FactionUnit;
import base.game.Location;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import com.github.apilab.rest.exceptions.NotFoundException;
import factionfiction.api.v2.game.ImmutableRecoShot;
import factionfiction.api.v2.game.RecoShot;
import static factionfiction.api.v2.mappers.RowMappers.factionUnitMapper;
import static factionfiction.api.v2.mappers.RowMappers.recoShotMapper;
import factionfiction.api.v2.math.MathService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static java.util.stream.Collectors.toList;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class CampaignFactionRepository {

  public static final String TABLE_NAME = "campaign_faction";

  final Jdbi jdbi;

  public CampaignFactionRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {
    UUID id = UUID.randomUUID();
    insert(id, campaignFaction);
    return find(id);
  }

  public UUID getCampaignFactionId(String campaignName, String factionName) {
    return jdbi.withHandle(h -> h.select(
      "select id from campaign_faction where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(UUID.class)
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  public CampaignFaction getCampaignFaction(UUID id) {
    return find(id);
  }

  public List<String> getAllFactionNamesOfCampaign(String campaignName, UUID userId) {
    return jdbi.withHandle(h -> h.select(
      "select cf.faction_name from campaign_faction cf"
        + " left join campaign c on cf.campaign_name = c.name"
        + " where cf.campaign_name = ? and c.manager_user = ?",
      campaignName,
      userId)
      .mapTo(String.class)
      .list());
  }

  public List<String> getAlliedFactionNamesOfCampaign(String campaignName, UUID userId) {
    boolean isBlue = ensurePartOfCampaignAndReturnIfBlue(campaignName, userId);
    return returnAlliedFactions(campaignName, isBlue);
  }

  public List<String> getEnemyFactionNamesOfCampaign(String campaignName, UUID userId) {
    boolean isBlue = ensurePartOfCampaignAndReturnIfBlue(campaignName, userId);
    return returnAlliedFactions(campaignName, !isBlue);
  }

  List<String> returnAlliedFactions(String campaignName, Boolean isBlue) {
    return jdbi.withHandle(h -> h.select(
      "select cf.faction_name from campaign_faction cf"
        + " left join campaign c on cf.campaign_name = c.name"
        + " where cf.campaign_name = ? and is_blue = ?",
      campaignName,
      isBlue)
      .mapTo(String.class)
      .list());
  }

  boolean ensurePartOfCampaignAndReturnIfBlue(String campaignName, UUID userId) {
    // Important on security, in this case permission filtering is done
    // through joining in selects for owner for allied factions.
    return jdbi.withHandle(h -> h.select(
      "select is_blue from campaign_faction cf"
        + " left join faction f on cf.faction_name = f.name"
        + " where cf.campaign_name = ? and f.commander_user = ?",
      campaignName,
      userId)
      .mapTo(Boolean.class)
      .findFirst()
      .orElseThrow(() -> new NotAuthorizedException("Not part of this campaign"))
    );
  }

  public void moveUnit(
    String campaignName, String factionName,
    UUID uid, Location location) {

    jdbi.useHandle(h -> {
      UUID cfid = getCampaignFactionId(campaignName, factionName);
      var cf = getCampaignFaction(cfid);

      var abloc = cf.airbase().location();
      var radiusft = h.select("select "
        + "cf.zone_size_ft "
        + "from campaign_faction cf "
        + "where cf.campaign_name = ? and cf.faction_name = ? "
        + "limit 1", campaignName, factionName)
        .mapTo(Integer.class)
        .first();

      if (!MathService.isInCircle(abloc, radiusft, location))
        throw new NotAuthorizedException("Cannot move outsize zone");

      h.execute("update campaign_faction_units "
        + "set x = ?, y = ?, z = ?, angle = ? "
        + "where id = ? and campaign_faction_id = ?",
        location.longitude(), location.latitude(), location.altitude(), location.angle(),
        uid, cfid);
    });
  }

  public void deleteRecoShot(UUID id) {
    jdbi.useHandle(h -> {
      h.execute("delete from recoshot where id = ?", id);
      h.execute("delete from recoshot_item where recoshot_id = ?", id);
    });
  }

  public List<RecoShot> getRecoShots(String campaignName, String factionName) {
    return jdbi.withHandle(h -> {
      UUID cfid = getCampaignFactionId(campaignName, factionName);
      return h.select("select id, latmin, latmax, lonmin, lonmax from recoshot where campaign_faction_id = ?", cfid)
        .map(recoShotMapper())
        .list()
        .stream()
        .map(shot ->
          ImmutableRecoShot.builder().from(shot)
            .units(getUnitsFromRecoShot(h, shot.id()))
            .build()
        )
        .collect(toList());
    });
  }

  List<FactionUnit> getUnitsFromRecoShot(Handle h, UUID id) {
    return h.select("select null as id, type, x, y, z, angle from recoshot_item where recoshot_id = ?", id)
      .map(factionUnitMapper())
      .list();
  }

  CampaignFaction find(UUID id) {
    return jdbi.withHandle(h -> h.select(
      "select campaign_name, faction_name, airbase, zone_size_ft, credits, is_blue"
        + " from campaign_faction where id = ?", id)
      .map((rs, st) -> ImmutableCampaignFaction.builder()
        .campaignName(rs.getString("campaign_name"))
        .factionName(rs.getString("faction_name"))
        .airbase(Airbases.valueOf(rs.getString("airbase")))
        .zoneSizeFt(rs.getInt("zone_size_ft"))
        .credits(new BigDecimal(rs.getString("credits")))
        .coalition(rs.getBoolean("is_blue") ? BLUE : RED)
        .build()
      )
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  void insert(
    UUID id,
    CampaignFaction campaignFaction) {

    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction ("
        + "id, "
        + "campaign_name, "
        + "faction_name, "
        + "airbase, "
        + "is_blue, "
        + "zone_size_ft, "
        + "credits)"
        + " values(?, ?, ?, ?, ?, ?, ?)",
      id,
      campaignFaction.campaignName(),
      campaignFaction.factionName(),
      campaignFaction.airbase(),
      campaignFaction.coalition() == BLUE,
      campaignFaction.zoneSizeFt(),
      campaignFaction.credits()
    ));
  }

  void removeCampaignFaction(String campaignName, String factionName) {
    var cfId = getCampaignFactionId(campaignName, factionName);
    jdbi.useHandle(h ->
      h.select("select campaign_name, airbase from campaign_faction where id = ?", cfId).mapToMap().findFirst().ifPresent(map -> {
        var airbase = map.get("airbase");
        h.execute("delete from campaign_airfield_warehouse where campaign_name = ? and airbase = ?", campaignName, airbase);
        h.execute("delete from campaign_faction where id = ?", cfId);
        h.execute("delete from campaign_faction_units where campaign_faction_id not in (select distinct id from campaign_faction)");
        h.execute("delete from campaign_faction_flight_log where campaign_faction_id not in (select distinct id from campaign_faction)");
        h.execute("delete from campaign_airfield_warehouse_item where warehouse_id not in (select distinct id from campaign_airfield_warehouse)");
      })
    );
  }
}
