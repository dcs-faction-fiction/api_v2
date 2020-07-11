package factionfiction.api.v2.faction;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface Faction {
  String name();
}
