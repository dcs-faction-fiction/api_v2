package factionfiction.api.v2.game;

import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsZones {
  GameOptionsZonesSizes sizes();
  GameOptionsZonesIncrease increase();
  GameOptionsZonesDecrease decrease();
  GameOptionsRecoShot recoShot();

  public static GameOptionsZones from(Map<?, ?> map) {
    return ImmutableGameOptionsZones.builder()
      .sizes(GameOptionsZonesSizes.from((Map) map.get("sizes")))
      .increase(GameOptionsZonesIncrease.from((Map) map.get("increase")))
      .decrease(GameOptionsZonesDecrease.from((Map) map.get("decrease")))
      .recoShot(GameOptionsRecoShot.from((Map) map.get("reco-shot")))
      .build();
  }
}
