package factionfiction.api.v2.campaignfaction;

import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaign.CampaignRepository;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignFactionServiceTest {

  CampaignFaction sample;
  CampaignFactionServiceImpl impl;
  CampaignRepository campRepo;
  FactionRepository facRepo;
  CampaignFactionRepository repository;

  @BeforeEach
  public void setup() throws IOException {
    sample = makeSampleCampaignFaction();
    campRepo = mock(CampaignRepository.class);
    facRepo = mock(FactionRepository.class);
    repository = mock(CampaignFactionRepository.class);
    impl = new CampaignFactionServiceImpl(campRepo, facRepo, repository);
  }

  @Test
  public void testCreateCampaignFaction() throws IOException {
    given(campRepo.find(sample.campaignName()))
      .willReturn(makeSampleCampaign());
    given(facRepo.find(sample.factionName()))
      .willReturn(makeSampleFaction());

    impl.newCampaignFaction(sample);

    verify(repository).newCampaignFaction(sample);
  }

  @Test
  public void testConverter() throws IOException {
    var options = new GameOptionsLoader().loadDefaults();
    var result = impl.fromCampaignAndFactionAndOptions(
      sample.campaignName(),
      sample.factionName(),
      sample.airbase(),
      options);

    assertThat(result, is(sample));
  }
}
