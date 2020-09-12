package factionfiction.api.v2.purchase;

import base.game.FactionUnit;
import base.game.Location;
import base.game.warehouse.WarehouseItemCode;
import java.math.BigDecimal;

public interface PurchaseService {
  BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits);
  FactionUnit buyUnit(String campaignName, String factionName, FactionUnit unit);
  void buyWarehouseItem(String campaignName, String factionName, WarehouseItemCode item);
  void zoneIncrease(String campaign, String faction);
  void zoneDecrease(String campaign, String faction);
  void buyRecoShot(String campaign, String faction, Location location);
}
