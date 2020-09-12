package factionfiction.api.v2.purchase;

import base.game.ImmutableFactionUnit;
import base.game.Location;
import static base.game.warehouse.WarehouseItemCode.JF_17;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction2;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.test.InMemoryDB;
import static factionfiction.api.v2.units.UnitHelper.cleanRecoShots;
import static factionfiction.api.v2.units.UnitHelper.cleanUnitTable;
import static factionfiction.api.v2.units.UnitHelper.insertSampleFactionUnit;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import factionfiction.api.v2.units.UnitRepository;
import java.io.IOException;
import java.math.BigDecimal;
import static java.math.BigDecimal.ONE;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignFaction sample;
  PurchaseRepository repository;
  UnitRepository unitRepository;
  CampaignFactionRepository campaignFactionRepository;

  @BeforeEach
  void setup() {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    sample = makeSampleCampaignFaction();
    unitRepository = new UnitRepository(jdbi);
    campaignFactionRepository = new CampaignFactionRepository(jdbi);
    repository = new PurchaseRepository(unitRepository, campaignFactionRepository, jdbi);
  }

  @Test
  void testGiveCredits() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.giveCredits(
      sample.campaignName(),
      sample.factionName(),
      ONE);

    assertThat(result, is(sample.credits().add(ONE)));
  }

  @Test
  void testGetCredits() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(result, is(sample.credits()));
  }

  @Test
  void testBuyUnit() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);
    var unit = makeSampleFactionUnit();

    var result = repository.buyUnit(
      sample.campaignName(),
      sample.factionName(),
      ONE,
      unit);
    // Equalize the bean to the fact that has been
    // saved with a new UUID in db.
    unit = ImmutableFactionUnit.builder()
      .from(unit)
      .id(result.id())
      .build();
    var credits = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(result, is(unit));
    assertThat(credits.compareTo(sample.credits()), is(-1));
  }

  @Test
  void testBuyItem() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    repository.buyItem(
      sample.campaignName(),
      sample.factionName(),
      ONE,
      JF_17);

    var credits = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(credits.compareTo(sample.credits()), is(-1));
  }

  @Test
  void testIncreaseDecreaseZone() throws IOException {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);
    var campaign = sample.campaignName();
    var faction = sample.factionName();
    var options = new GameOptionsLoader().loadDefaults();
    assertThrows(ZoneAtMinumum.class, () -> {
      repository.zoneDecrease(
        campaign,
        faction,
        options);
    });

    repository.zoneIncrease(
      sample.campaignName(),
      sample.factionName(),
      new GameOptionsLoader().loadDefaults());

    var credits = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(credits.compareTo(sample.credits()), is(-1));

    repository.zoneDecrease(
      sample.campaignName(),
      sample.factionName(),
      new GameOptionsLoader().loadDefaults());

    credits = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(credits.compareTo(sample.credits()), is(-1));

    repository.giveCredits(sample.campaignName(), sample.factionName(), new BigDecimal(-1000));

    assertThrows(NotEnoughCreditsException.class, () -> {
      repository.zoneIncrease(
        campaign,
        faction,
        options);
    });
  }

  @Test
  void testBuyRecoShot() throws IOException {
    cleanCampaignFactionTable(jdbi);
    cleanUnitTable(jdbi);
    cleanRecoShots(jdbi);

    var cfId = UUID.randomUUID();
    var cfId2 = UUID.randomUUID();
    insertSampleCampaignFaction(jdbi, cfId, owner);
    insertSampleCampaignFaction2(jdbi, cfId2, owner);
    insertSampleFactionUnit(jdbi, UUID.randomUUID(), cfId2);

    var location = Location.of("1", "2");
    var options = new GameOptionsLoader().loadDefaults();

    repository.buyRecoShot(sample.campaignName(), sample.factionName(), location, options);

    var credits = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(credits.compareTo(sample.credits()), is(-1));

    jdbi.useHandle(h -> {
      var id = h.select("select id from recoshot where campaign_faction_id = ?", cfId)
        .mapTo(UUID.class)
        .findOne()
        .get();
      var type = h.select("select type from recoshot_item where recoshot_id = ?", id)
        .mapTo(String.class)
        .findOne()
        .get();
      assertThat(type, is("T_80"));
    });

  }

}
