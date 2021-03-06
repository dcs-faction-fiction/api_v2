package factionfiction.api.v2.daemon;

import base.game.Airbases;
import static base.game.Airbases.KRYMSK;
import base.game.ImmutableLocation;
import base.game.Location;
import static base.game.units.Unit.ABRAMS;
import static base.game.units.Unit.T_80;
import base.game.warehouse.WarehouseItemCode;
import static base.game.warehouse.WarehouseItemCode.JET_FUEL_TONS;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import com.github.apilab.rest.exceptions.NotFoundException;
import static factionfiction.api.v2.daemon.ServerAction.MISSION_STARTED;
import static factionfiction.api.v2.daemon.ServerAction.START_NEW_MISSION;
import static factionfiction.api.v2.daemon.ServerAction.STOP_MISSION;
import static factionfiction.api.v2.test.InMemoryDB.jdbi;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.h2.util.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DaemonRepositoryIT {

  static final byte[] MISSION_BYTES = new byte[]{1,2,3};

  UUID unit1;
  UUID unit2;
  Jdbi jdbi;
  DaemonRepository repository;

  @BeforeEach
  void setup() {
    unit1 = UUID.randomUUID();
    unit2 = UUID.randomUUID();
    jdbi = jdbi();
    repository = new DaemonRepository(jdbi);
  }

  @Test
  void testReportWarehouses() {
    cleanTables();
    addData();
    var request = makeWarehouseRequest();

    repository.reportWarehouses("server1", request);

    assertThat(getAmount("campaign1", KRYMSK, JET_FUEL_TONS), is(1));
  }

  @Test
  void testReportWarehousesNotInitializedServer() {
    cleanTables();
    addData();
    var request = makeWarehouseRequest();

    repository.reportWarehouses("server2", request);

    assertThat(getAmount("campaign2", KRYMSK, JET_FUEL_TONS), is(0));
  }

  @Test
  void testReportWarehousesRandomServer() {
    cleanTables();
    addData();
    var request = makeWarehouseRequest();

    assertThrows(NotAuthorizedException.class, () -> {
      repository.reportWarehouses("whatever", request);
    });
  }

  @Test
  void testReportDeadUnit() {
    cleanTables();
    addData();

    repository.reportDeadUnits("server1", List.of(unit1));

    var unitIds = getUnitIds();
    assertThat(unitIds, is(List.of(unit2)));
  }

  @Test
  void testReportMovedUnit() {
    cleanTables();
    addData();
    var location = ImmutableLocation.builder()
      .longitude(ONE)
      .latitude(ONE)
      .altitude(ONE)
      .angle(ONE)
      .build();

    repository.reportMovedUnits("server1", List.of(ImmutableFactionUnitPosition.builder()
      .id(unit2)
      .location(location)
      .build()));

    var changedLocation = getUnitLocation(unit2);
    assertThat(changedLocation, is(location));
  }

  @Test
  void testDownloadMission() {
    cleanTables();
    addData();
    var result = new byte[3];
    repository.downloadMission("server1", b -> {
      try {
        IOUtils.readFully(b, result, 3);
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage(), ex);
      }
    });

    assertThat(result, is(MISSION_BYTES));
  }

  @Test
  void testActions() {
    var info = Optional.of((ServerInfo) ImmutableServerInfo.builder()
      .address("localhost")
      .port(1)
      .password("pw")
      .build());

    repository.setNextAction("server1", STOP_MISSION, info);
    repository.setNextAction("server1", MISSION_STARTED, info);
    repository.setNextAction("server1", START_NEW_MISSION, info);

    var action = repository.pullNextAction("server1");

    assertThat(action, is(Optional.of(START_NEW_MISSION)));
  }

  ImmutableWarehousesSpent makeWarehouseRequest() {
    var request = ImmutableWarehousesSpent.builder()
      .data(List.of(ImmutableWarehousesSpentItem.builder()
        .airbase("Krymsk")
        .type("jet_fuel")
        .amount(new BigDecimal(-2))
        .build()))
      .build();
    return request;
  }

  void cleanTables() {
    jdbi.useHandle(h -> {
      h.execute("truncate table campaign_airfield_warehouse_item");
      h.execute("truncate table campaign_airfield_warehouse");
      h.execute("truncate table server");
      h.execute("truncate table campaign_faction_units");
    });
  }

  void addData() {
    UUID cfId = UUID.randomUUID();
    UUID whid = UUID.randomUUID();
    jdbi.useHandle(h -> {
      h.execute("insert into server(name, campaign_name, mission_zip) values('server1', 'campaign1', ?)", MISSION_BYTES);
      h.execute("insert into server(name, campaign_name) values('server2', 'campaign2')");
      h.execute("insert into campaign_airfield_warehouse(id, campaign_name, airbase) values(?, ?, ?)",
        whid, "campaign1", "KRYMSK");
      h.execute("insert into campaign_airfield_warehouse_item(id, warehouse_id, item_code, item_quantity) values (?, ?, ?, ?)",
        UUID.randomUUID(),
        whid,
        JET_FUEL_TONS,
        3);
      h.execute("insert into campaign_faction_units(id, campaign_faction_id, type, x, y, z, angle) values(?, ?, ?, ?, ?, ?, ?)",
        unit1,
        cfId,
        ABRAMS,
        0, 0, 0, 0);
      h.execute("insert into campaign_faction_units(id, campaign_faction_id, type, x, y, z, angle) values(?, ?, ?, ?, ?, ?, ?)",
        unit2,
        cfId,
        T_80,
        0, 0, 0, 0);
    });
  }

  int getAmount(String campaignName, Airbases airbase, WarehouseItemCode code) {
    return jdbi.withHandle(h -> h.select(
      "select item_quantity"
        + " from campaign_airfield_warehouse_item wi"
        + " left join campaign_airfield_warehouse w on wi.warehouse_id = w.id"
        + " where wi.item_code = ? and w.campaign_name = ? and w.airbase = ?",
      code,
      campaignName,
      airbase)
    .mapTo(Integer.class)
    .findFirst()
    .orElseThrow(() -> new NotFoundException("not found")));
  }

  List<UUID> getUnitIds() {
    return jdbi.withHandle(h -> h.select("select id from campaign_faction_units").mapTo(UUID.class).list());
  }

  Location getUnitLocation(UUID unitId) {
    return jdbi.withHandle(h -> h.select("select x, y, z, angle from campaign_faction_units where id = ?", unitId)
      .map((rs, st) -> ImmutableLocation.builder()
        .latitude(rs.getBigDecimal("y"))
        .longitude(rs.getBigDecimal("x"))
        .altitude(rs.getBigDecimal("z"))
        .angle(rs.getBigDecimal("angle"))
        .build())
      .findFirst()
      .orElseThrow());
  }
}
