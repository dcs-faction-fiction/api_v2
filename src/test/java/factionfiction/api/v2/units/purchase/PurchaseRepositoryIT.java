package factionfiction.api.v2.units.purchase;

import base.game.ImmutableFactionUnit;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.test.InMemoryDB;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import factionfiction.api.v2.units.UnitRepository;
import static java.math.BigDecimal.ONE;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class PurchaseRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignFaction sample;
  PurchaseRepository repository;
  UnitRepository unitRepository;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    sample = makeSampleCampaignFaction();
    unitRepository = new UnitRepository(jdbi);
    repository = new PurchaseRepository(unitRepository, jdbi);
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


}
