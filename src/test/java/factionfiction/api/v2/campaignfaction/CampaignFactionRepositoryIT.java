package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.ANAPA;
import static base.game.Airbases.KUTAISI;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import static factionfiction.api.v2.campaign.CampaignHelper.cleanCampaignTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
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
import org.jdbi.v3.core.Jdbi;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  public void testGetAllFactionNamesOfCampaign() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner);

    var result = repository.getAllFactionNamesOfCampaign("campaign", campaignOwner);

    assertThat(Set.copyOf(result), is(Set.of("faction", "faction2")));
  }

  @Test
  public void testAlliedFactions() {
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner);

    var result = repository.getAlliedFactionNamesOfCampaign("campaign", factionOwner);

    assertThat(result, is(List.of("faction")));
  }

  @Test
  public void testAlliedFactionsWrongRole() {
    var someOtherOwner = UUID.randomUUID();
    var campaignOwner = UUID.randomUUID();
    var factionOwner = UUID.randomUUID();
    cleanFactionTable(jdbi);
    cleanCampaignTable(jdbi);
    cleanCampaignFactionTable(jdbi);
    insertDataForFactionNames(jdbi, campaignOwner, factionOwner);

    assertThrows(NotAuthorizedException.class, () -> {
      repository.getAlliedFactionNamesOfCampaign("campaign", someOtherOwner);
    });
  }

  private void insertDataForFactionNames(Jdbi jdbi, UUID campaignOwner, UUID factionOwner) {
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
        UUID.randomUUID()));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
        + " values(?, ?, ?, ?, ?)",
      UUID.randomUUID(), "campaign", "faction", ANAPA, false));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
        + " values(?, ?, ?, ?, ?)",
      UUID.randomUUID(), "campaign", "faction2", KUTAISI, true));
  }

}
