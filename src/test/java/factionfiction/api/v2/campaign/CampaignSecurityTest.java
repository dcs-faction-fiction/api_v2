package factionfiction.api.v2.campaign;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.daemon.ServerInfo;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignSecurityTest {

  GameOptions gameOptions;
  AuthInfo authInfo;
  CampaignServiceImpl impl;
  CampaignSecurity security;

  @BeforeEach
  public void setup() throws IOException {
    gameOptions = new GameOptionsLoader().loadDefaults();
    authInfo = mock(AuthInfo.class);
    impl = mock(CampaignServiceImpl.class);
    security = new CampaignSecurity(impl, authInfo);
  }

  @Test
  public void testNeedsCampaignManagerToListOwnCampaigns() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(uuid);

    var list = List.of(makeSampleCampaign());
    given(impl.listCampaigns(any())).willReturn(list);

    var result = security.listCampaigns();

    assertThat(result, is(list));
  }

  @Test
  public void testCannotListIfNotCampaignManager() {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.listCampaigns();
    });
  }

  @Test
  public void testCanCreateCampaignWhenCampaignManager() {
    String name = "valid name";
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);

    security.newCampaign(name, gameOptions);

    verify(impl).newCampaign(name, uuid, gameOptions);
  }

  @Test
  public void testCannotCreateCampaignWhenNotCampaignManager() {
    String name = "valid name";
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaign(name, gameOptions);
    });
  }

  @Test
  public void testFindCampaignNotPermission() throws IOException {
    given(authInfo.isCampaignManager()).willReturn(false);
    assertThrows(NotAuthorizedException.class, () -> {
      security.find("");
    });
  }

  @Test
  public void testFindCampaignNotOwned() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("", uuid)).willReturn(false);
    assertThrows(NotAuthorizedException.class, () -> {
      security.find("");
    });
  }

  @Test
  public void testFindCampaignOwned() throws IOException {
    var uuid = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("", uuid)).willReturn(true);
    given(impl.find("")).willReturn(campaign);

    var result = security.find("");

    assertThat(result, is(campaign));
  }

  @Test
  public void testGetServerInfo() throws IOException {
    var uuid = UUID.randomUUID();
    var campaign = makeSampleCampaign();
    var info = mock(ServerInfo.class);
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner(campaign.name(), uuid)).willReturn(true);
    given(impl.getServerInfo(campaign.name())).willReturn(Optional.of(info));

    var result = security.getServerInfo(campaign.name());

    assertThat(result, is(Optional.of(info)));
  }

  @Test
  public void testGetServerInfoNoManager() throws IOException {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.getServerInfo("");
    });
  }

  @Test
  public void testGetServerInfoNoOwner() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("", uuid)).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.getServerInfo("");
    });
  }

  @Test
  public void teststartMissionAsAdmin() throws IOException {
    given(authInfo.isAdmin()).willReturn(true);

    security.startMission("camp", "serv");

    verify(impl).startMission("camp", "serv");
  }

  @Test
  public void teststartMission() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("camp", uuid)).willReturn(true);
    given(impl.userCanManageServer(uuid, "serv")).willReturn(true);

    security.startMission("camp", "serv");

    verify(impl).startMission("camp", "serv");
  }

  @Test
  public void teststartMissionNoManager() throws IOException {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.startMission("camp", "serv");
    });
  }

  @Test
  public void teststartMissionNoOwner() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("camp", uuid)).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.startMission("camp", "serv");
    });
  }

  @Test
  public void teststartMissionNoServerOwner() throws IOException {
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.isOwner("camp", uuid)).willReturn(true);
    given(impl.userCanManageServer(uuid, "serv")).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.startMission("camp", "serv");
    });
  }
}
