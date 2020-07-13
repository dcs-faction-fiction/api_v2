package factionfiction.api.v2.campaignfaction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;

public class CampaignFactionSecurity implements CampaignFactionService {

  final AuthInfo authInfo;
  final CampaignFactionServiceImpl impl;

  public CampaignFactionSecurity(CampaignFactionServiceImpl impl, AuthInfo authInfo) {
    this.authInfo = authInfo;
    this.impl = impl;
  }

  @Override
  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {
    if (!canCreateCampaignFactions())
      throw cannotCreateCampaignFactionsError();

    return impl.newCampaignFaction(campaignFaction);
  }

  private boolean canCreateCampaignFactions() {
    return authInfo.isCampaignManager();
  }

  private RuntimeException cannotCreateCampaignFactionsError() {
    return new NotAuthorizedException("Cannot create campaignFactions");
  }
}
