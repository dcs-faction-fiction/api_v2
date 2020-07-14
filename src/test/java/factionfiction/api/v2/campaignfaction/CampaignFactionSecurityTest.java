package factionfiction.api.v2.campaignfaction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignFactionSecurityTest {

  CampaignFaction sample;
  UUID owner;
  AuthInfo authInfo;
  CampaignFactionServiceImpl impl;
  CampaignFactionSecurity security;
  CampaignRepository campaignRepository;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    sample = makeSampleCampaignFaction();
    authInfo = mock(AuthInfo.class);
    impl = mock(CampaignFactionServiceImpl.class);
    campaignRepository = mock(CampaignRepository.class);
    security = new CampaignFactionSecurity(impl, campaignRepository, authInfo);
  }

  @Test
  public void testCreateCampaignFaction() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(true);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    var result = security.newCampaignFaction(sample);

    assertThat(result, is(sample));
  }

  @Test
  public void testCreateCampaignFactionNotOwned() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  public void testCannotCreateCampaignFactionWithoutPermission() {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

  @Test
  public void testCannotCreateCampaignFactionWithoutOwner() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(authInfo.getUserUUID()).willReturn(owner);
    given(campaignRepository.isOwner(sample.campaignName(), owner)).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

}
