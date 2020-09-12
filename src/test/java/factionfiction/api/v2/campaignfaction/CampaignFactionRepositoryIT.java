package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import static base.game.Airbases.KUTAISI;
import static base.game.CampaignCoalition.RED;
import base.game.Location;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import static factionfiction.api.v2.campaign.CampaignHelper.cleanCampaignTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionUnitsTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static factionfiction.api.v2.faction.FactionHelper.cleanFactionTable;
import static factionfiction.api.v2.test.InMemoryDB.jdbi;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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

    repository.moveUnit("campaign name", "faction name", unitId, newLocation);

    Location location = getLocationOfUnitById(unitId);
    assertThat(location, is(not(newLocation)));
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
