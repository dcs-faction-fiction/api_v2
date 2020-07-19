package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.game.GameOptions;

public class CampaignFactionSecurity implements CampaignFactionService {

  final AuthInfo authInfo;
  final CampaignFactionServiceImpl impl;
  final CampaignRepository campaignRepository;
  final FactionRepository factionRepository;

  public CampaignFactionSecurity(
    CampaignFactionServiceImpl impl,
    CampaignRepository campaignRepository,
    FactionRepository factionRepository,
    AuthInfo authInfo) {

    this.authInfo = authInfo;
    this.impl = impl;
    this.campaignRepository = campaignRepository;
    this.factionRepository = factionRepository;
  }

  @Override
  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {
    if (!canCreateCampaignFactions())
      throw cannotCreateCampaignFactionsError();

    if (!isCampaignOwner(campaignFaction))
      throw cannotCreateWithoutOwner();

    return impl.newCampaignFaction(campaignFaction);
  }

  @Override
  public FactionSituation getSituation(String campaignName, String factionName) {
    if (!canGetSituation(campaignName, factionName))
      throw cannotGetSituation();

    return impl.getSituation(campaignName, factionName);
  }

  @Override
  public GameOptions getGameOptions(String campaignName, String factionName) {
    if (!canGetSituation(campaignName, factionName))
      throw cannotGetOptions();

    return campaignRepository.find(campaignName).gameOptions();
  }

  boolean isCampaignOwner(CampaignFaction campaignFaction) {
    return campaignRepository.isOwner(campaignFaction.campaignName(), authInfo.getUserUUID());
  }

  private boolean canCreateCampaignFactions() {
    return authInfo.isCampaignManager();
  }

  private boolean canGetSituation(String campaignName, String factionName) {
    var ownsCampaign = authInfo.isCampaignManager() && campaignRepository.isOwner(campaignName, authInfo.getUserUUID());
    var ownsFaction = authInfo.isFactionManager() && factionRepository.isOwner(factionName, authInfo.getUserUUID());
    return ownsCampaign || ownsFaction;
  }

  private RuntimeException cannotCreateWithoutOwner() {
    return new NotAuthorizedException("Need to be campaign manager for the campaign in order to add factions to it.");
  }

  private RuntimeException cannotCreateCampaignFactionsError() {
    return new NotAuthorizedException("Cannot create campaignFactions");
  }

  private RuntimeException cannotGetSituation() {
    return new NotAuthorizedException("Cannot get situation for this faction.");
  }

  private RuntimeException cannotGetOptions() {
    return new NotAuthorizedException("Cannot get campaign options.");
  }
}
