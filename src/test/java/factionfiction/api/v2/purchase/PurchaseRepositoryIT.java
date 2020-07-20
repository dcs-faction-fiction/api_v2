package factionfiction.api.v2.purchase;

import base.game.ImmutableFactionUnit;
import static base.game.warehouse.WarehouseItemCode.JF_17;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.test.InMemoryDB;
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

public class PurchaseRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignFaction sample;
  PurchaseRepository repository;
  UnitRepository unitRepository;
  CampaignFactionRepository campaignFactionRepository;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    sample = makeSampleCampaignFaction();
    unitRepository = new UnitRepository(jdbi);
    campaignFactionRepository = new CampaignFactionRepository(jdbi);
    repository = new PurchaseRepository(unitRepository, campaignFactionRepository, jdbi);
  }

  @Test
  public void testGiveCredits() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.giveCredits(
      sample.campaignName(),
      sample.factionName(),
      ONE);

    assertThat(result, is(sample.credits().add(ONE)));
  }

  @Test
  public void testGetCredits() {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.getCredits(
      sample.campaignName(),
      sample.factionName());

    assertThat(result, is(sample.credits()));
  }

  @Test
  public void testBuyUnit() {
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
  public void testBuyItem() {
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
  public void testIncreaseDecreaseZone() throws IOException {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    assertThrows(ZoneAtMinumum.class, () -> {
      repository.zoneDecrease(
        sample.campaignName(),
        sample.factionName(),
        new GameOptionsLoader().loadDefaults());
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
      sample.campaignName(),
      sample.factionName(),
         new GameOptionsLoader().loadDefaults());
    });
  }

}
