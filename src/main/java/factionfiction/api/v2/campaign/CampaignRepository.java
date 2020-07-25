package factionfiction.api.v2.campaign;

import base.game.Airbases;
import base.game.CampaignCoalition;
import static base.game.CampaignCoalition.BLUE;
import static base.game.CampaignCoalition.RED;
import base.game.CampaignMap;
import static base.game.CampaignMap.CAUCASUS;
import base.game.FactionUnit;
import base.game.FullMissionBuilder;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.MissionConfiguration;
import base.game.units.Unit;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.exceptions.NotFoundException;
import com.google.gson.Gson;
import static factionfiction.api.v2.daemon.ServerAction.START_NEW_MISSION;
import factionfiction.api.v2.daemon.ServerInfo;
import factionfiction.api.v2.game.GameOptions;
import java.io.ByteArrayOutputStream;
import static java.lang.Boolean.TRUE;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

public class CampaignRepository {

  public static final String TABLE_NAME = "campaign";

  Jdbi jdbi;
  Gson gson;
  GameOptions defaultOptions;

  public CampaignRepository(Jdbi jdbi, Gson gson, GameOptions defaultOptions) {
    this.jdbi = jdbi;
    this.gson = gson;
    this.defaultOptions = defaultOptions;
  }

  public List<Campaign> listCampaigns(UUID owner) {
    return jdbi.withHandle(h -> h
      .select("select name, game_options from campaign where manager_user = ?", owner)
      .map(mapToCampaign())
      .list());
  }

  public Campaign newCampaign(String name, UUID owner, GameOptions options) {
    insert(name, owner, options);
    return find(name);
  }

  private void insert(String name, UUID owner, GameOptions options) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign(name, manager_user, game_options)"
        + " values(?, ?, ?)",
      name,
      owner,
      gson.toJson(options)));
  }

  public Campaign find(String name) {
    return jdbi.withHandle(h -> h.select("select name, game_options from campaign where name = ?", name)
      .map(mapToCampaign())
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  public boolean isOwner(String name, UUID owner) {
    return jdbi.withHandle(h -> h.select(
      "select name from campaign where name = ? and manager_user = ?",
      name,
      owner)
      .mapTo(String.class)
      .findFirst())
      .isPresent();
  }

  private RowMapper<Campaign> mapToCampaign() {
    return (rs, ctx) -> campaignFromResultSet(rs);
  }

  private Campaign campaignFromResultSet(ResultSet rs) throws SQLException {
    return ImmutableCampaign.builder()
      .name(rs.getString("name"))
      .gameOptions(parseOptionOrDefaults(rs))
      .build();
  }

  private GameOptions parseOptionOrDefaults(ResultSet rs) throws SQLException {
    return Optional.ofNullable(rs.getString("game_options"))
      .map(s -> gson.fromJson(s, GameOptions.class))
      .orElse(defaultOptions);
  }

  public List<String> getAvailableCampaignsForFaction(String factionName) {
    return jdbi.withHandle(h -> h.select(
      "select campaign_name from campaign_faction where faction_name = ?",
      factionName)
      .mapTo(String.class)
      .list());
  }

  public boolean userCanManageServer(UUID user, String server) {
    return jdbi.withHandle(h -> h.select(
      "select server_name from user_server "
      + "where user_id = ? and server_name = ?",
      user, server)
      .mapTo(String.class)
      .findFirst()
      .isPresent());
  }

  public Optional<ServerInfo> getInfoFromCampaign(String campaign) {
    return jdbi.withHandle(h -> h.select(
      "select address, port, password from server where campaign_name = ? and running = true",
      campaign)
      .mapTo(ServerInfo.class)
      .findOne()
    );
  }

  public void startMission(String campaignName, String serverName, MissionConfiguration configuration) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverName);

      var map = getCampaignMap(campaignName);
      var warehousesMap = getWarehouses(campaignName);
      var blueUnits = getCoalitionUnitsForCampaign(campaignName, BLUE);
      var redUnits = getCoalitionUnitsForCampaign(campaignName, RED);

      FullMissionBuilder mb = new FullMissionBuilder();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      mb.build(map, configuration, warehousesMap, blueUnits, redUnits, bos);

      h.execute("update server"
        + " set next_action = ?, mission_zip = ?, campaign_name = ?"
        + " where name = ?",
        START_NEW_MISSION,
        bos.toByteArray(),
        campaignName,
        serverName);
    });
  }

  private CampaignMap getCampaignMap(String campaignName) {
    return jdbi.withHandle(h ->
       h.select("select map from campaign where name = ?", campaignName)
        .mapTo(CampaignMap.class)
        .findFirst()
        .orElse(CAUCASUS)
    );
  }

  private Map<CampaignCoalition, Map<Airbases, Map<WarehouseItemCode, BigDecimal>>> getWarehouses(String campaignName) {
    Map<CampaignCoalition, Map<Airbases, Map<WarehouseItemCode, BigDecimal>>> result = new EnumMap<>(CampaignCoalition.class);
    jdbi.useHandle(h ->
      h.select("select"
        + " w.airbase airbase,"
        + " i.item_code code,"
        + " i.item_quantity qty,"
        + " cf.is_blue is_blue"
        + " from campaign_airfield_warehouse_item i"
        + " left join campaign_airfield_warehouse w on i.warehouse_id = w.id"
        + " left join campaign_faction cf on cf.campaign_name = w.campaign_name and cf.airbase = w.airbase"
        + " where w.campaign_name = ?",
        campaignName)
        .mapToMap()
        .forEach(m -> {
          var airbase = Airbases.valueOf((String) m.get("airbase"));
          var code = WarehouseItemCode.valueOf((String) m.get("code"));
          var qty = new BigDecimal(m.get("qty").toString());
          var coalition = TRUE.equals(Boolean.valueOf(m.get("is_blue").toString())) ? BLUE : RED;
          result
            .computeIfAbsent(coalition, coa -> new EnumMap<>(Airbases.class))
            .computeIfAbsent(airbase, a -> new EnumMap<>(WarehouseItemCode.class))
            .put(code, qty);
        })
    );
    return result;
  }

  private List<FactionUnit> getCoalitionUnitsForCampaign(String campaignName, CampaignCoalition coa) {
    return jdbi.withHandle(h ->
      h.select("select u.id, u.type, u.x, u.y, u.z, angle from campaign_faction_units u "
        + "left join campaign_faction cf on u.campaign_faction_id = cf.id "
        + "where cf.campaign_name = ? and cf.is_blue = ?"
        + "limit 10000", // 10k units per coalition max
        campaignName, coa == BLUE)
        .map((rs, st) ->
          (FactionUnit) ImmutableFactionUnit
            .builder()
            .id(UUID.fromString(rs.getString(1)))
            .type(Unit.valueOf(rs.getString(2)))
            .location(ImmutableLocation.builder()
              .longitude(new BigDecimal(rs.getString(3)))
              .latitude(new BigDecimal(rs.getString(4)))
              .altitude(new BigDecimal(rs.getString(5)))
              .angle(new BigDecimal(rs.getString(6)))
              .build())
            .build())
        .list()
    );
  }

  private void ensureServerExists(Handle h, String serverid) {
    boolean exists = h.select("select name from server where name = ?", serverid)
      .mapTo(String.class)
      .findFirst()
      .isPresent();
    if (!exists) {
      h.execute("insert into server (name) values(?)", serverid);
    }
  }

}
