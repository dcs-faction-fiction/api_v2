package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.faction.FactionRepository;
import java.math.BigDecimal;

public class PurchaseSecurity implements PurchaseService {

  final AuthInfo authInfo;
  final PurchaseServiceImpl impl;
  final CampaignRepository campaignRepository;
  final FactionRepository factionRepository;

  public PurchaseSecurity(PurchaseServiceImpl impl,
    CampaignRepository campaignRepository,
    FactionRepository factionRepository,
    AuthInfo authInfo) {

    this.impl = impl;
    this.campaignRepository = campaignRepository;
    this.authInfo = authInfo;
    this.factionRepository = factionRepository;
  }

  @Override
  public BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits) {
    if (!managerAndOwnsCampaign(campaignName))
      throw errorNotCampaignManager();

    return impl.giveCredits(campaignName, factionName, credits);
  }

  @Override
  public FactionUnit buyUnit(String campaignName, String factionName, FactionUnit unit) {
    if (!managerAndOwnsFaction(factionName))
      throw errorNotFactionManager();

    return impl.buyUnit(campaignName, factionName, unit);
  }

  @Override
  public void buyWarehouseItem(String campaignName, String factionName, WarehouseItemCode item) {
    if (!managerAndOwnsFaction(factionName))
      throw errorNotFactionManager();

    impl.buyWarehouseItem(campaignName, factionName, item);
  }

  boolean managerAndOwnsCampaign(String campaignName) {
    return authInfo.isCampaignManager() && isCampaignOwner(campaignName);
  }

  boolean isCampaignOwner(String campaignName) {
    return campaignRepository.isOwner(campaignName, authInfo.getUserUUID());
  }

  static NotAuthorizedException errorNotCampaignManager() {
    return new NotAuthorizedException("Not a campaign manager");
  }


  boolean managerAndOwnsFaction(String factionName) {
    return authInfo.isFactionManager() && isFactionOwner(factionName);
  }

  boolean isFactionOwner(String factionName) {
    return factionRepository.isOwner(factionName, authInfo.getUserUUID());
  }

  static NotAuthorizedException errorNotFactionManager() {
    return new NotAuthorizedException("Not a faction manager");
  }
}
