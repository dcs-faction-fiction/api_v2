package factionfiction.api.v2.campaign;

import com.github.apilab.core.GSONModule;
import com.google.gson.Gson;
import static factionfiction.api.v2.campaign.CampaignHelper.cleanCampaignTable;
import static factionfiction.api.v2.campaign.CampaignHelper.insertSampleCampaign;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.test.InMemoryDB;
import java.io.IOException;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CampaignRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignRepository repository;
  Gson gson;
  GameOptions defaultOptions;

  @BeforeEach
  public void setup() throws IOException {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    gson = new GSONModule().gson();
    defaultOptions = new GameOptionsLoader().loadDefaults();
    repository = new CampaignRepository(jdbi, gson, defaultOptions);
  }

  @Test
  public void testCampaignListZero() {
    cleanCampaignTable(jdbi);

    var campaigns = repository.listCampaigns(owner);

    assertThat(campaigns, is(emptyList()));
  }

  @Test
  public void testCampaignListOne() throws IOException {
    cleanCampaignTable(jdbi);

    insertSampleCampaign(jdbi, owner);

    var campaigns = repository.listCampaigns(owner);

    assertThat(campaigns, is(List.of(makeSampleCampaign())));
  }

  @Test
  public void testNewCampaign() throws IOException {
    cleanCampaignTable(jdbi);
    var campaignToCreate = makeSampleCampaign();

    var campaign = repository.newCampaign(campaignToCreate.name(), owner, defaultOptions);
    var gampaigns = repository.listCampaigns(owner);

    assertThat(campaign, is(campaignToCreate));
    assertThat(gampaigns, is(List.of(campaignToCreate)));
  }

  @Test
  public void testIsOwner() throws IOException {
    cleanCampaignTable(jdbi);
    insertSampleCampaign(jdbi, owner);
    var sample = makeSampleCampaign();

    var isOwner = repository.isOwner(sample.name(), owner);

    assertThat(isOwner, is(true));
  }

  @Test
  public void testIsNotOwner() throws IOException {
    cleanCampaignTable(jdbi);
    insertSampleCampaign(jdbi, owner);
    var sample = makeSampleCampaign();

    var isOwner = repository.isOwner(sample.name(), UUID.randomUUID());

    assertThat(isOwner, is(false));
  }

  @Test
  public void testGetAvailableCampaignsForFaction() throws IOException {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.getAvailableCampaignsForFaction("faction name");

    assertThat(result, is(List.of("campaign name")));
  }
}
