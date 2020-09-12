package factionfiction.api.v2.faction;

import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import static factionfiction.api.v2.test.AuthProvider.mockUser;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FactionEndpointsTest {

  UUID user;
  Context ctx;
  Javalin javalin;
  FactionService service;
  FactionEndpoints endpoints;

  @BeforeEach
  void setup() {
    user = UUID.randomUUID();
    ctx = mock(Context.class);
    javalin = mock(Javalin.class);
    service = mock(FactionService.class);
    endpoints = new FactionEndpoints(v -> service);
  }

  @Test
  void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/faction-api"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).post(eq("/v2/faction-api/factions"), any(), eq(roles(FACTION_MANAGER)));
    verify(javalin).get(eq("/v2/faction-api/factions"), any(), eq(roles(FACTION_MANAGER)));
  }

  @Test
  void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  void testNewFaction() {
    var faction = makeSampleFaction();
    mockUserWIthFactionRole();
    given(ctx.bodyAsClass(String.class)).willReturn(faction.name());
    given(service.newFaction(faction.name())).willReturn(faction);

    endpoints.newFaction(ctx);

    verify(ctx).json(faction);
  }

  @Test
  void testGetFactions() {
    var faction = makeSampleFaction();
    var factions = List.of(faction);
    mockUserWIthFactionRole();
    given(service.listFactions()).willReturn(factions);

    endpoints.getFactions(ctx);

    verify(ctx).json(factions);
  }

  void mockUserWIthFactionRole() {
    mockUser(ctx, user, Set.of(FACTION_MANAGER));
  }
}
