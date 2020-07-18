package factionfiction.api.v2.daemon;

import static factionfiction.api.v2.auth.Roles.DAEMON;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DaemonEndpointTest {

  UUID user;
  Context ctx;
  Javalin javalin;
  DaemonEndpoints endpoints;

  @BeforeEach
  public void setup() {
    user = UUID.randomUUID();
    ctx = mock(Context.class);
    javalin = mock(Javalin.class);
    endpoints = new DaemonEndpoints();
  }

  @Test
  public void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/daemon-api"), any(), eq(roles(DAEMON)));
  }

  @Test
  public void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }
}
