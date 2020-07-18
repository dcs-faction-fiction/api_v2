package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import base.game.warehouse.WarehouseItemCode;
import java.math.BigDecimal;

public interface PurchaseService {
  BigDecimal giveCredits(String campaignName, String factionName, BigDecimal credits);
  FactionUnit buyUnit(String campaignName, String factionName, FactionUnit unit);
  void buyWarehouseItem(String campaignName, String factionName, WarehouseItemCode item);
}
