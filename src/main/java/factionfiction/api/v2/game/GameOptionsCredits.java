package factionfiction.api.v2.game;

import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsCredits {
  BigDecimal starting();
  GameOptionsCreditsGain gain();

  public static GameOptionsCredits from(Map map) {
    return ImmutableGameOptionsCredits.builder()
      .starting(new BigDecimal(map.get("starting").toString()))
      .gain(GameOptionsCreditsGain.from((Map) map.get("gain")))
      .build();
  }
}
