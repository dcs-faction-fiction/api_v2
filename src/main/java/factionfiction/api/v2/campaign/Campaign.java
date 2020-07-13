package factionfiction.api.v2.campaign;

import factionfiction.api.v2.game.GameOptions;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface Campaign {
  String name();
  GameOptions gameOptions();
}
