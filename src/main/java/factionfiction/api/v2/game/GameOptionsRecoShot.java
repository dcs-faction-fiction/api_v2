package factionfiction.api.v2.game;

import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsRecoShot {
  BigDecimal cost();
  Long edgeSize();

  public static GameOptionsRecoShot from(Map<?, ?> map) {
    return ImmutableGameOptionsRecoShot.builder()
      .edgeSize(Long.valueOf(map.get("edge-size").toString()))
      .cost(new BigDecimal(map.get("cost").toString()))
      .build();
  }
}
