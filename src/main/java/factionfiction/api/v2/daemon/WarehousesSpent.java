package factionfiction.api.v2.daemon;

import base.game.Airbases;
import base.game.warehouse.WarehouseItemCode;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface WarehousesSpent {
  List<WarehousesSpentItem> data();

  default Map<Airbases, Map<WarehouseItemCode, BigDecimal>> toWarehouseDelta() {
    Map<Airbases, Map<WarehouseItemCode, BigDecimal>> result = new EnumMap<>(Airbases.class);
    data().forEach(item -> {
      var airbase = Airbases.fromName(item.airbase());
      airbase.ifPresent(base -> {
        var baseMap = result.computeIfAbsent(base, i -> new EnumMap<>(WarehouseItemCode.class));
        WarehouseItemCode.byCode(item.type()).ifPresent(type -> {
          var value = baseMap.computeIfAbsent(type, i -> ZERO);
          value = value.add(item.amount());
          baseMap.put(type, value);
        });
      });
    });
    return result;
  }
}
