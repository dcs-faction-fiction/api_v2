package factionfiction.api.v2.game;

import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsZonesDecrease {
  Integer amount();
  BigDecimal cost();

  public static GameOptionsZonesDecrease from(Map<?, ?> map) {
    return ImmutableGameOptionsZonesDecrease.builder()
      .amount(Integer.valueOf(map.get("amount").toString()))
      .cost(new BigDecimal(map.get("gain").toString()))
      .build();
  }
}
