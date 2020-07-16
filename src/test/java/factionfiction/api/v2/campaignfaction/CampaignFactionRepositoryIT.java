package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static factionfiction.api.v2.test.InMemoryDB.jdbi;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CampaignFactionRepositoryIT {

  CampaignFaction sample;
  BigDecimal initialCredits;
  Jdbi jdbi;
  CampaignFactionRepository repository;
  UUID owner;

  @BeforeEach
  public void setup() throws IOException {
    sample = makeSampleCampaignFaction();
    owner = UUID.randomUUID();
    jdbi = jdbi();
    repository = new CampaignFactionRepository(jdbi);
  }

  @Test
  public void testNewCampaignFaction() {
    cleanCampaignFactionTable(jdbi);

    var campaignFaction = repository.newCampaignFaction(sample);

    assertThat(campaignFaction, is(sample));
  }

  @Test
  public void testNewCampaignFactionRed() {
    var sampleRed = ImmutableCampaignFaction.builder()
      .from(sample)
      .airbase(ANAPA)
      .build();
    cleanCampaignFactionTable(jdbi);

    var campaignFaction = repository.newCampaignFaction(sampleRed);

    assertThat(campaignFaction, is(sampleRed));
  }

  @Test
  public void testGetCFID() {
    var id = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, id, owner);

    var result = repository.getCampaignFactionId(sample.campaignName(), sample.factionName());

    assertThat(result, is(id));
  }

  @Test
  public void testGtById() {
    var id = UUID.randomUUID();
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, id, owner);

    var result = repository.getCampaignFaction(id);

    assertThat(result, is(makeSampleCampaignFaction()));
  }

}
