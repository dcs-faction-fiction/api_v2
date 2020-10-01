package factionfiction.api.v2.game;

import base.game.units.Unit;
import base.game.units.UnitType;
import java.math.BigDecimal;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptionsUnit {
  Unit code();
  BigDecimal cost();
  @Default default UnitType type() {
    return code().type();
  }

  public static GameOptionsUnit from(Map<?, ?> map) {
    return ImmutableGameOptionsUnit.builder()
      .code(Unit.valueOf(map.get("code").toString()))
      .cost(new BigDecimal(map.get("cost").toString()))
      .build();
  }
}
