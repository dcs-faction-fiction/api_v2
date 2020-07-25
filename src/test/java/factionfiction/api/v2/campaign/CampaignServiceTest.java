package factionfiction.api.v2.campaign;

import base.game.units.MissionConfiguration;
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
  public void testFindCampaign() throws IOException {
    var campaign = makeSampleCampaign();
    given(repository.find(campaign.name())).willReturn(campaign);
    var result = service.find(campaign.name());

    assertThat(result, is(campaign));
  }

  @Test
  public void testIsOwner() throws IOException {
    var campaign = makeSampleCampaign();
    given(repository.isOwner(campaign.name(), owner)).willReturn(true);
    var isowner = service.isOwner(campaign.name(), owner);

    verify(repository).isOwner(campaign.name(), owner);
    assertThat(isowner, is(true));
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

  @Test
  public void testPassServerInfo() {
    service.getServerInfo("camp");
    verify(repository).getInfoFromCampaign("camp");
  }

  @Test
  public void testPassStartMission() {
    var conf = mock(MissionConfiguration.class);
    service.startMission("camp", "serv", conf);
    verify(repository).startMission("camp", "serv", conf);
  }

  @Test
  public void testPassUserCanManageServer() {
    var user = UUID.randomUUID();
    service.userCanManageServer(user, "serv");
    verify(repository).userCanManageServer(user, "serv");
  }
}
