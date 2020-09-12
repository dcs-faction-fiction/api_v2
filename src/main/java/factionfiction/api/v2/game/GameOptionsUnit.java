package factionfiction.api.v2.game;

import base.game.units.Unit;
import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsUnit {
  Unit code();
  BigDecimal cost();

  public static GameOptionsUnit from(Map<?, ?> map) {
    return ImmutableGameOptionsUnit.builder()
      .code(Unit.valueOf(map.get("code").toString()))
      .cost(new BigDecimal(map.get("cost").toString()))
      .build();
  }
}
