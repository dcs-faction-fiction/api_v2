package factionfiction.api.v2.campaign;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.daemon.ServerInfo;
import factionfiction.api.v2.game.GameOptions;
import java.util.List;
import java.util.Optional;

public class CampaignSecurity implements CampaignService {

  final CampaignServiceImpl impl;
  final AuthInfo authInfo;

  public CampaignSecurity(CampaignServiceImpl impl, AuthInfo authInfo) {
    this.authInfo = authInfo;
    this.impl = impl;
  }

  @Override
  public List<Campaign> listCampaigns() {
    if (!canViewCampaigns())
      throw cannotViewCampaignsError();

    return impl.listCampaigns(authInfo.getUserUUID());
  }

  @Override
  public Campaign newCampaign(String name, GameOptions options) {
    if (!canCreateCampaigns())
      throw cannotCreateCampaignsError();

    return impl.newCampaign(name, authInfo.getUserUUID(), options);
  }

  @Override
  public Campaign find(String name) {
    if (!canViewCampaigns())
      throw cannotViewCampaignsError();

    if (!ownsCampaign(name))
      throw cannotOwnCampaign();

    return impl.find(name);
  }

  @Override
  public void startMission(String campaignName, String serverName) {
    if (authInfo.isAdmin()) {
      impl.startMission(campaignName, serverName);
      return;
    }

    if (!canViewCampaigns())
      throw cannotViewCampaignsError();

    if (!ownsCampaign(campaignName))
      throw cannotOwnCampaign();

    if (!canManageServer(serverName))
      throw cannotManageServerError();

    impl.startMission(campaignName, serverName);
  }

  @Override
  public Optional<ServerInfo> getServerInfo(String campaignName) {
    if (!canViewCampaigns())
      throw cannotViewCampaignsError();

    if (!ownsCampaign(campaignName))
      throw cannotOwnCampaign();

    return impl.getServerInfo(campaignName);
  }

  boolean ownsCampaign(String name) {
    return impl.isOwner(name, authInfo.getUserUUID());
  }

  boolean canViewCampaigns() {
    return authInfo.isCampaignManager();
  }

  boolean canCreateCampaigns() {
    return authInfo.isCampaignManager();
  }

  boolean canManageServer(String serverName) {
    return impl.userCanManageServer(authInfo.getUserUUID(), serverName);
  }

  static RuntimeException cannotViewCampaignsError() {
    return new NotAuthorizedException("Cannot view campaigns");
  }

  static RuntimeException cannotOwnCampaign() {
    return new NotAuthorizedException("Not owning this campaign");
  }

  static RuntimeException cannotCreateCampaignsError() {
    return new NotAuthorizedException("Cannot create campaigns");
  }

  static RuntimeException cannotManageServerError() {
    return new NotAuthorizedException("User does not manage this server");
  }
}
