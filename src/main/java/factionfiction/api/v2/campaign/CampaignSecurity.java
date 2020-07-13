package factionfiction.api.v2.campaign;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.game.GameOptions;
import java.util.List;

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

  boolean canViewCampaigns() {
    return authInfo.isCampaignManager();
  }

  static RuntimeException cannotViewCampaignsError() {
    return new NotAuthorizedException("Cannot view campaigns");
  }

  boolean canCreateCampaigns() {
    return authInfo.isCampaignManager();
  }

  static RuntimeException cannotCreateCampaignsError() {
    return new NotAuthorizedException("Cannot create campaigns");
  }
}
