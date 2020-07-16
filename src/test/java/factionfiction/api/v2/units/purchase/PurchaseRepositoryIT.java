package factionfiction.api.v2.units.purchase;

import factionfiction.api.v2.campaignfaction.CampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.test.InMemoryDB;
import static java.math.BigDecimal.ONE;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PurchaseRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignFaction sample;
  PurchaseRepository repository;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    sample = makeSampleCampaignFaction();
    repository = new PurchaseRepository(jdbi);
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
}
