package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import static base.game.Airbases.KUTAISI;
import static base.game.CampaignCoalition.RED;
import base.game.Location;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import com.github.apilab.rest.exceptions.NotFoundException;
import static factionfiction.api.v2.campaign.CampaignHelper.cleanCampaignTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionUnitsTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static factionfiction.api.v2.faction.FactionHelper.cleanFactionTable;
import static factionfiction.api.v2.test.InMemoryDB.jdbi;
import factionfiction.api.v2.units.UnitHelper;
import static factionfiction.api.v2.units.UnitHelper.cleanRecoShots;
import static factionfiction.api.v2.units.UnitHelper.insertRecoShot;
import static factionfiction.api.v2.units.UnitHelper.insertSampleFactionUnit;
import static factionfiction.api.v2.units.UnitHelper.makeSampleRecoShot;
import factionfiction.api.v2.units.UnitRepository;
import factionfiction.api.v2.warehouse.WarehouseHelper;
import static factionfiction.api.v2.warehouse.WarehouseHelper.insertSampleWarehouseItems;
import factionfiction.api.v2.warehouse.WarehouseRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.immutables.gson.Gson.Ignore;
import org.jdbi.v3.core.Jdbi;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CampaignFactionRepositoryIT {

  CampaignFaction sample;
  BigDecimal initialCredits;
  Jdbi jdbi;
  CampaignFactionRepository repository;
  UUID owner;

  @BeforeEach
  void setup() throws IOException {
    sample = makeSampleCampaignFaction();
    owner = UUID.randomUUID();
    jdbi = jdbi();
    repository = new CampaignFactionRepository(jdbi);
  }

  @Test
  void testNewCampaignFaction() {
    cleanCampaignFactionTable(jdbi);

    var campaignFaction = repository.newCampaignFaction(sample);

    assertThat(campaignFaction, is(sample));
  }

  @Test
  void testNewCampaignFactionRed() {
    var sampleRed = ImmutableCampaignFaction.builder()
      .from(sample)
      .airbase(ANAPA)
      .coalition(RED)
      .build();
    cleanCampaignFactionTable(jdbi);

    var campaignFaction = repository.newCampaignFaction(sampleRed);

    assertThat(campaignFaction, is(sampleRed));
  }

  @Test
  void testGetCFID() {
    var id = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, id, owner);

    var result = repository.getCampaignFactionId(sample.campaignName(), sample.factionName());

    assertThat(result, is(id));
  }

  @Test
  void testGtById() {
    var id = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, id, owner);

    var result = repository.getCampaignFaction(id);

    assertThat(result, is(makeSampleCampaignFaction()));
  }

  @Test
  void testGetAllFactionNamesOfCampaign() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    var result = repository.getAllFactionNamesOfCampaign("campaign", campaignOwner);

    assertThat(Set.copyOf(result), is(Set.of("faction", "faction2")));
  }

  @Test
  void testAlliedFactions() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    var result = repository.getAlliedFactionNamesOfCampaign("campaign", factionOwner);

    assertThat(result, is(List.of("faction")));
  }

  @Test
  void testAlliedFactionsWrongRole() {
    var someOtherOwner = UUID.randomUUID();
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    assertThrows(NotAuthorizedException.class, () -> {
      repository.getAlliedFactionNamesOfCampaign("campaign", someOtherOwner);
    });
  }

  @Test
  void testEnemyFactions() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    var result = repository.getEnemyFactionNamesOfCampaign("campaign", factionOwner);

    assertThat(result, is(List.of("faction2")));
  }

  @Test
  void testEnemyFactionsOtherFaction() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    var result = repository.getEnemyFactionNamesOfCampaign("campaign", faction2Owner);

    assertThat(result, is(List.of("faction")));
  }

  @Test
  void testEnemyFactionsWrongRole() {
    var someOtherOwner = UUID.randomUUID();
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    var faction2Owner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner, faction2Owner);

    assertThrows(NotAuthorizedException.class, () -> {
      repository.getEnemyFactionNamesOfCampaign("campaign", someOtherOwner);
    });
  }

  private void insertDataForFactionNames(Jdbi jdbi, UUID campaignOwner, UUID factionOwner, UUID faction2Owner) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign(name, manager_user)"
        + " values(?, ?)", "campaign",
        campaignOwner));
    jdbi.useHandle(h -> h.execute(
      "insert into faction(name, commander_user)"
        + " values(?, ?)", "faction",
        factionOwner));
    jdbi.useHandle(h -> h.execute(
      "insert into faction(name, commander_user)"
        + " values(?, ?)", "faction2",
        faction2Owner));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
        + " values(?, ?, ?, ?, ?)",
      UUID.randomUUID(), "campaign", "faction", ANAPA, false));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
        + " values(?, ?, ?, ?, ?)",
      UUID.randomUUID(), "campaign", "faction2", KUTAISI, true));
  }

  @Test
  void testMoveUnit() {
    var newLocation = KUTAISI.location();
    UUID unitId = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    cleanCampaignFactionUnitsTable(jdbi);
    insertDataForMoveUnit(jdbi, unitId);

    repository.moveUnit("campaign name", "faction name", unitId, newLocation);

    Location location = getLocationOfUnitById(unitId);
    assertThat(location, is(newLocation));
  }

  @Test
  void testMoveUnitOutsideBase() {
    var newLocation = ANAPA.location();
    UUID unitId = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    cleanCampaignFactionUnitsTable(jdbi);
    insertDataForMoveUnit(jdbi, unitId);

    assertThrows(NotAuthorizedException.class, () -> {
      repository.moveUnit("campaign name", "faction name", unitId, newLocation);
    });
  }

  @Test
  void testDeleteRecoShot() {
    var id = UUID.randomUUID();
    var cfId = UUID.randomUUID();
    cleanRecoShots(jdbi);
    insertRecoShot(jdbi, id, cfId);

    repository.deleteRecoShot(id);

    var count = jdbi.withHandle(h ->
      h.select("select count(id) from recoshot where id = ?", id)
        .mapTo(Integer.class)
        .findOne()
        .get());

    assertThat(count, is(0));

    count = jdbi.withHandle(h ->
      h.select("select count(recoshot_id) from recoshot_item where recoshot_id = ?", id)
        .mapTo(Integer.class)
        .findOne()
        .get());

    assertThat(count, is(0));
  }

  @Test
  void testGetRecoShots() {
    var id = UUID.randomUUID();
    var cfId = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    cleanRecoShots(jdbi);
    insertSampleCampaignFaction(jdbi, cfId, owner);
    insertRecoShot(jdbi, id, cfId);

    var result = repository.getRecoShots("campaign name", "faction name");
    var output = makeSampleRecoShot(id);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is(output));
  }

  @Test
  void testRemoveFaction() {
    var cfId = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, cfId, owner);
    insertSampleWarehouseItems(jdbi);
    insertSampleFactionUnit(jdbi, cfId, cfId);

    var warehouseRepository = new WarehouseRepository(jdbi);
    var warehouse = warehouseRepository.getWarehouseFromCampaignFaction("campaign name", KUTAISI);
    assertThat(warehouse.isEmpty(), is(false));

    var unitRepository = new UnitRepository(jdbi);
    var units = unitRepository.getUnitsFromCampaignFaction(cfId);
    assertThat(units.isEmpty(), is(false));

    repository.removeCampaignFaction("campaign name", "faction name");

    assertThrows(NotFoundException.class, () -> {
      repository.getCampaignFaction(cfId);
    });

    warehouse = warehouseRepository.getWarehouseFromCampaignFaction("campaign name", KUTAISI);
    assertThat(warehouse.isEmpty(), is(true));

    units = unitRepository.getUnitsFromCampaignFaction(cfId);
    assertThat(units.isEmpty(), is(true));
  }

  Location getLocationOfUnitById(UUID unitId) {
    var location = jdbi.withHandle(h -> h.select("select"
      + " x as longitude,"
      + " y as latitude,"
      + " z as altitude,"
      + " angle"
      + " from campaign_faction_units"
      + " where id = ?",
      unitId)
      .mapTo(Location.class)
      .findFirst().get());
    return location;
  }

  private void insertDataForMoveUnit(Jdbi jdbi, UUID unitId) {
    UUID cfid = UUID.randomUUID();
    insertSampleCampaignFaction(jdbi, cfid, UUID.randomUUID());
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction_units(id, campaign_faction_id, x, y, z, angle, type)"
        + " values(?, ?, ?, ?, ?, ?, 'AMBRAMS')",
      unitId, cfid, 0, 0, 0, 0));
  }

}
