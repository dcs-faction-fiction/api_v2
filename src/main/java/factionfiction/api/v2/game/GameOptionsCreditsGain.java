package factionfiction.api.v2.game;

import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsCreditsGain {
  BigDecimal mission();

  public static GameOptionsCreditsGain from(Map map) {
    return ImmutableGameOptionsCreditsGain.builder()
      .mission(new BigDecimal(map.get("mission").toString()))
      .build();
  }
}
