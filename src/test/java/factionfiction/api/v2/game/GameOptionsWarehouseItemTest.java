package factionfiction.api.v2.game;

import static base.game.warehouse.WarehouseItemCategory.PLANES;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

class GameOptionsWarehouseItemTest {
  @Test
  void testType() {
    var unit = GameOptionsWarehouseItem.from(Map.of("code", "F_14_B", "cost", "1"));
    assertThat(unit.category(), is(PLANES));
  }
}
