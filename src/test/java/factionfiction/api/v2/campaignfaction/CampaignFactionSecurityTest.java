package factionfiction.api.v2.campaignfaction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CampaignFactionSecurityTest {

  CampaignFaction sample;
  AuthInfo authInfo;
  CampaignFactionServiceImpl impl;
  CampaignFactionSecurity security;

  @BeforeEach
  public void setup() {
    sample = makeSampleCampaignFaction();
    authInfo = mock(AuthInfo.class);
    impl = mock(CampaignFactionServiceImpl.class);
    security = new CampaignFactionSecurity(impl, authInfo);
  }

  @Test
  public void testCreateCampaignFaction() {
    given(authInfo.isCampaignManager()).willReturn(true);
    given(impl.newCampaignFaction(sample)).willReturn(sample);

    var result = security.newCampaignFaction(sample);

    assertThat(result, is(sample));
  }

  @Test
  public void testCannotCreateCampaignFactionWithoutPermission() {
    given(authInfo.isCampaignManager()).willReturn(false);

    assertThrows(NotAuthorizedException.class, () -> {
      security.newCampaignFaction(sample);
    });
  }

}
