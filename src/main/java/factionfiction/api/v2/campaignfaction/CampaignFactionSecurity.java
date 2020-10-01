package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.Location;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.RecoShot;
import java.util.List;
import java.util.UUID;

public class CampaignFactionSecurity implements CampaignFactionService {

  static final String NOT_OWNING_THIS_FACTION = "Not owning this faction.";

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

    if (!isCampaignOwner(campaignFaction.campaignName()))
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

  @Override
  public void setGameOptions(String campaignName, GameOptions options) {
    if (!canCreateCampaignFactions())
      throw cannotCreateCampaignFactionsError();

    if (!isCampaignOwner(campaignName))
      throw cannotCreateWithoutOwner();

    campaignRepository.setGameOptions(campaignName, options);
  }

  @Override
  public List<String> getAvailableCampaigns(String factionName) {
    return campaignRepository.getAvailableCampaignsForFaction(factionName);
  }

  @Override
  public List<FactionSituation> getAllFactions(String campaignName) {
    if (!authInfo.isCampaignManager())
      throw new NotAuthorizedException("Only campaign manager can view all factions of a campaign.");

    return impl.getAllFactions(campaignName, authInfo.getUserUUID());
  }

  @Override
  public List<FactionSituation> getAlliedFactions(String campaignName) {
    if (!authInfo.isFactionManager())
      throw new NotAuthorizedException("Only faction manager can view allied factions.");

    return impl.getAlliedFactions(campaignName, authInfo.getUserUUID());
  }

  @Override
  public List<Location> getEnemyFactionLocations(String campaignName) {
    if (!authInfo.isFactionManager())
      throw new NotAuthorizedException("Only faction manager can view enemy faction locations.");

    return impl.getEnemyFactionLocations(campaignName, authInfo.getUserUUID());
  }

  @Override
  public void moveUnit(String campaignName, String factionName, UUID uid, Location location) {
    if (!authInfo.isFactionManager())
      throw new NotAuthorizedException("Only faction manager can move own units.");
    if (!isFactionOwner(factionName))
      throw new NotAuthorizedException(NOT_OWNING_THIS_FACTION);

    impl.moveUnit(campaignName, factionName, uid, location);
  }

  @Override
  public void deleteRecoShot(String campaignName, String factionName, UUID id) {
    if (!authInfo.isFactionManager())
      throw new NotAuthorizedException("Only faction manager can delete reco shots.");
    if (!isFactionOwner(factionName))
      throw new NotAuthorizedException(NOT_OWNING_THIS_FACTION);

    impl.deleteRecoShot(id);
  }

  @Override
  public List<RecoShot> getRecoShots(String campaignName, String factionName) {
    if (!authInfo.isFactionManager())
      throw new NotAuthorizedException("Only faction manager can get shots.");
    if (!isFactionOwner(factionName))
      throw new NotAuthorizedException(NOT_OWNING_THIS_FACTION);

    return impl.getRecoShots(campaignName, factionName);
  }

  boolean isFactionOwner(String factionName) {
    return factionRepository.isOwner(factionName, authInfo.getUserUUID());
  }

  boolean isCampaignOwner(String campaignName) {
    return campaignRepository.isOwner(campaignName, authInfo.getUserUUID());
  }

  private boolean canCreateCampaignFactions() {
    return authInfo.isCampaignManager();
  }

  private boolean canGetSituation(String campaignName, String factionName) {
    var ownsCampaign = authInfo.isCampaignManager() && isCampaignOwner(campaignName);
    var ownsFaction = authInfo.isFactionManager() && isFactionOwner(factionName);
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
