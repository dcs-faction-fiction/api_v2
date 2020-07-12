package factionfiction.api.v2.test;

import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_ROLES;
import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_SUBJECT;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import java.util.Set;
import java.util.UUID;
import static org.mockito.BDDMockito.given;

public final class AuthProvider {

  private AuthProvider() {
  }

  public static void mockUser(Context ctx, UUID uuid, Set<Role> roles) {
    given(ctx.attribute(REQ_ATTR_SUBJECT))
      .willReturn(uuid.toString());
    given(ctx.attribute(REQ_ATTR_ROLES))
      .willReturn(roles);
  }
}
