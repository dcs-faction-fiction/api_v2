package factionfiction.api.v2.auth;

import static factionfiction.api.v2.auth.Roles.ADMIN;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import static factionfiction.api.v2.test.AuthProvider.mockUser;
import io.javalin.http.Context;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

public class AuthInfoTest {

  @Test
  public void testFromContextFactionManager() {
    var uuid = UUID.randomUUID();
    Context ctx = mock(Context.class);
    mockUser(ctx, uuid, Set.of(FACTION_MANAGER));

    var info = AuthInfo.fromContext(ctx);

    assertThat(info.isFactionManager(), is(true));
    assertThat(info.getUserUUID(), is(uuid));
  }

  @Test
  public void testFromContextCampaignManager() {
    var uuid = UUID.randomUUID();
    Context ctx = mock(Context.class);
    mockUser(ctx, uuid, Set.of(CAMPAIGN_MANAGER));

    var info = AuthInfo.fromContext(ctx);

    assertThat(info.isCampaignManager(), is(true));
    assertThat(info.getUserUUID(), is(uuid));
  }

  @Test
  public void testFromContextAdmin() {
    var uuid = UUID.randomUUID();
    Context ctx = mock(Context.class);
    mockUser(ctx, uuid, Set.of(ADMIN));

    var info = AuthInfo.fromContext(ctx);

    assertThat(info.isAdmin(), is(true));
  }
}
