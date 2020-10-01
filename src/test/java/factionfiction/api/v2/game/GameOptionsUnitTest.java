package factionfiction.api.v2.game;

import static base.game.units.UnitType.AIR;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class GameOptionsUnitTest {
  @Test
  void testType() {
    var unit = GameOptionsUnit.from(Map.of("code", "AWACS", "cost", "1"));
    assertThat(unit.type(), is(AIR));
  }
}
