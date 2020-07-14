package factionfiction.api.v2.campaignfaction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;

public class CampaignFactionSecurity implements CampaignFactionService {

  final AuthInfo authInfo;
  final CampaignFactionServiceImpl impl;
  final CampaignRepository campaignRepository;

  public CampaignFactionSecurity(
    CampaignFactionServiceImpl impl,
    CampaignRepository campaignRepository,
    AuthInfo authInfo) {

    this.authInfo = authInfo;
    this.impl = impl;
    this.campaignRepository = campaignRepository;
  }

  @Override
  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {
    if (!canCreateCampaignFactions())
      throw cannotCreateCampaignFactionsError();

    if (!isCampaignOwner(campaignFaction))
      throw cannotCreateWithoutOwner();

    return impl.newCampaignFaction(campaignFaction);
  }

  boolean isCampaignOwner(CampaignFaction campaignFaction) {
    return campaignRepository.isOwner(campaignFaction.campaignName(), authInfo.getUserUUID());
  }

  private boolean canCreateCampaignFactions() {
    return authInfo.isCampaignManager();
  }

  private RuntimeException cannotCreateWithoutOwner() {
    return new NotAuthorizedException("Need to be campaign manager for the campaign in order to add factions to it.");
  }

  private RuntimeException cannotCreateCampaignFactionsError() {
    return new NotAuthorizedException("Cannot create campaignFactions");
  }
}
