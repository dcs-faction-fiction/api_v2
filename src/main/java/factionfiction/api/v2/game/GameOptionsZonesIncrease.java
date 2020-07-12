package factionfiction.api.v2.game;

import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsZonesIncrease {
  Integer amount();
  BigDecimal cost();

  public static GameOptionsZonesIncrease from(Map map) {
    return ImmutableGameOptionsZonesIncrease.builder()
      .amount(Integer.valueOf(map.get("amount").toString()))
      .cost(new BigDecimal(map.get("cost").toString()))
      .build();
  }
}
