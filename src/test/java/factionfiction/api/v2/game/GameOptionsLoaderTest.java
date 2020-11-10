package factionfiction.api.v2.game;

import java.io.IOException;
import java.math.BigDecimal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameOptionsLoaderTest {
  GameOptionsLoader loader;

  @BeforeEach
  void setup() {
    loader = new GameOptionsLoader();
  }

  @Test
  void testLoadDefaults() throws IOException {
    var option = loader.loadDefaults();

    assertThat(option.credits().starting(), is(new BigDecimal(400)));
  }

  @Test
  void testLoadNotFoundFileReturnsDefaults() throws IOException {
    var option = loader.loadFile("anyfile.yml");

    assertThat(option.credits().starting(), is(new BigDecimal(400)));
  }

  @Test
  void testLoadRootFile() throws IOException {
    var option = loader.loadFile("fixtures/test_game_values.yml");

    assertThat(option.credits().starting(), is(new BigDecimal(1.5)));
  }
}
