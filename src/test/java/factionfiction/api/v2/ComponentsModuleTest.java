package factionfiction.api.v2;

import com.github.apilab.rest.exceptions.ServerException;
import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ComponentsModuleTest {
  @Test
  public void testExceptions() throws IOException {
    var module = new ComponentsModule();
    var loader = mock(GameOptionsLoader.class);

    given(loader.loadDefaults()).willThrow(IOException.class);

    assertThrows(ServerException.class, () -> {
      module.defaultGameOptions(loader);
    });
  }
}
