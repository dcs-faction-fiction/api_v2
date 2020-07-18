package factionfiction.api.v2.daemon;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface ServerInfo {
  String address();
  int port();
  String password();
}
