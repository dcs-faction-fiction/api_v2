package factionfiction.api.v2.game;

import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface GameOptions {
  GameOptionsCredits credits();
  GameOptionsZones zones();
  List<GameOptionsUnit> units();
  List<GameOptionsWarehouseItem> warehouseItems();

  public static GameOptions from(Map map) {
    List<Map> units = (List<Map>) map.get("units");
    List<Map> warehouseItems = (List<Map>) map.get("warehouseItems");
    return ImmutableGameOptions.builder()
      .credits(GameOptionsCredits.from((Map) map.get("credits")))
      .zones(GameOptionsZones.from((Map) map.get("zones")))
      .units(units.stream()
        .map(GameOptionsUnit::from)
        .collect(toList()))
      .warehouseItems(warehouseItems.stream()
        .map(GameOptionsWarehouseItem::from)
        .collect(toList()))
      .build();
  }
}
