package factionfiction.api.v2.game;

import base.game.warehouse.WarehouseItemCategory;
import base.game.warehouse.WarehouseItemCode;
import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsWarehouseItem {
  WarehouseItemCode code();
  BigDecimal cost();
  default WarehouseItemCategory category() {
    return code().category();
  }

  public static GameOptionsWarehouseItem from(Map<?, ?> map) {
    return ImmutableGameOptionsWarehouseItem.builder()
      .code(WarehouseItemCode.valueOf(map.get("code").toString()))
      .cost(new BigDecimal(map.get("cost").toString()))
      .build();
  }
}
