package factionfiction.api.v2.daemon;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface ServerInfo {
  @Default default String address() {return "localhost";}
  @Default default int port(){return 0;}
  @Default default String password(){return "";}
}
