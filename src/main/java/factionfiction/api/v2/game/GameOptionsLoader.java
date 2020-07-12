package factionfiction.api.v2.game;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class GameOptionsLoader {

  private static final String DEFAULTS_FILE = "game_defaults.yml";

  public GameOptions loadFile(String file) throws IOException {
    try {
      var map = loadYaml(file);
      return GameOptions.from((Map) map.get("game"));
    } catch (IOException | RuntimeException e) {
      return loadDefaults();
    }
  }

  public GameOptions loadDefaults() throws IOException {
    var map = loadYamlFromDefault();
    return GameOptions.from((Map) map.get("game"));
  }

  Map<String, Object> loadYamlFromDefault() throws IOException {
    try (InputStream input = getDefaultsResourceAsStream()) {
      return new Yaml().load(input);
    }
  }

  Map<String, Object> loadYaml(String file) throws IOException {
    try (InputStream input = getStreamFromFileName(file)) {
      return new Yaml().load(input);
    }
  }

  static FileInputStream getStreamFromFileName(String file) throws IOException {
    return new FileInputStream(file);
  }

  static InputStream getDefaultsResourceAsStream() {
    return GameOptionsLoader.class
      .getClassLoader()
      .getResourceAsStream(DEFAULTS_FILE);
  }

}
