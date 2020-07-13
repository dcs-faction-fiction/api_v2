package factionfiction.api.v2.campaign;

import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.game.ImmutableGameOptions;
import java.io.IOException;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignServiceTest {

  UUID owner;
  CampaignRepository repository;
  CampaignServiceImpl service;
  GameOptions defaultOptions;

  @BeforeEach
  public void setup() throws IOException {
    defaultOptions = new GameOptionsLoader().loadDefaults();
    owner = UUID.randomUUID();
    repository = mock(CampaignRepository.class);
    service = new CampaignServiceImpl(repository);
  }

  @Test
  public void testListCampaignsEmpty() {
    var list = service.listCampaigns(owner);

    assertThat(list, is(emptyList()));
  }

  @Test
  public void testListCampaignsNull() {
    assertThrows(NullPointerException.class, () -> {
      service.listCampaigns(null);
    });
  }

  @Test
  public void testListCampaignsResults() throws IOException {
    var list = List.of(makeSampleCampaign());
    given(repository.listCampaigns(owner)).willReturn(list);
    var result = service.listCampaigns(owner);

    assertThat(result, is(list));
  }

  @Test
  public void testCreateCampaign() {
    var name = "Campaign";
    GameOptions options = ImmutableGameOptions.copyOf(defaultOptions);
    given(repository.newCampaign(name, owner, options))
      .willReturn(ImmutableCampaign.builder()
        .name("Campaign")
        .gameOptions(options)
        .build());

    var campaign = service.newCampaign(name, owner, options);

    verify(repository).newCampaign(name, owner, options);
    assertThat(campaign.name(), is("Campaign"));
    assertThat(campaign.gameOptions(), is(options));
  }
}
