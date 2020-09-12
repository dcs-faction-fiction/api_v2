package factionfiction.api.v2.game;

import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsZonesSizes {
  Integer min();
  Integer max();

  public static GameOptionsZonesSizes from(Map<?, ?> map) {
    return ImmutableGameOptionsZonesSizes.builder()
      .min(Integer.valueOf(map.get("min").toString()))
      .max(Integer.valueOf(map.get("max").toString()))
      .build();
  }
}
