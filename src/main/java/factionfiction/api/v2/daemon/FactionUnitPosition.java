package factionfiction.api.v2.daemon;

import base.game.Location;
import java.util.UUID;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface FactionUnitPosition {
  UUID id();
  Location location();
}
