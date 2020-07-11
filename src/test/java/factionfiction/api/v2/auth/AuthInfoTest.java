package factionfiction.api.v2.auth;

import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_ROLES;
import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_SUBJECT;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import io.javalin.http.Context;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AuthInfoTest {
  @Test
  public void testFromContext() {
    var uuid = UUID.randomUUID();
    Context ctx = mock(Context.class);
    given(ctx.attribute(REQ_ATTR_SUBJECT))
      .willReturn(uuid.toString());
    given(ctx.attribute(REQ_ATTR_ROLES))
      .willReturn(Set.of(FACTION_MANAGER));

    var info = AuthInfo.fromContext(ctx);

    assertThat(info.isFactionManager(), is(true));
    assertThat(info.getUserUUID(), is(uuid));
  }
}
