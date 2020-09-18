package factionfiction.api.v2.purchase;

import base.game.warehouse.WarehouseItemCode;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface PurchaseBucket {
  Map<WarehouseItemCode, Integer> basket();
}
