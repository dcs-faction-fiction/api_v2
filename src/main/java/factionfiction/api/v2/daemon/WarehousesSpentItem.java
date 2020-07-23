package factionfiction.api.v2.daemon;

import java.math.BigDecimal;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface WarehousesSpentItem {
  String airbase();
  String type();
  BigDecimal amount();
}
