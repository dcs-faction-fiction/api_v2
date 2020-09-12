package factionfiction.api.v2.game;

import base.game.FactionUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface RecoShot {
  UUID id();
  BigDecimal minLat();
  BigDecimal maxLat();
  BigDecimal minLon();
  BigDecimal maxLon();
  List<FactionUnit> units();
}
