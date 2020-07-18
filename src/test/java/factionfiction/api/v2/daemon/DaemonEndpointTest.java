package factionfiction.api.v2.daemon;

import base.game.ImmutableLocation;
import static factionfiction.api.v2.auth.Roles.DAEMON;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DaemonEndpointTest {

  UUID user;
  Context ctx;
  Javalin javalin;
  DaemonRepository repository;
  DaemonEndpoints endpoints;

  @BeforeEach
  public void setup() {
    user = UUID.randomUUID();
    ctx = mock(Context.class);
    javalin = mock(Javalin.class);
    repository = mock(DaemonRepository.class);
    endpoints = new DaemonEndpoints(repository);
  }

  @Test
  public void testRegister() {
    endpoints.register(javalin);

    verify(javalin).get(eq("/v2/daemon-api"), any(), eq(roles(DAEMON)));
    verify(javalin).post(eq("/v2/daemon-api/servers/:server/warehouse-changed"), any(), eq(roles(DAEMON)));
    verify(javalin).post(eq("/v2/daemon-api/servers/:server/units-moved"), any(), eq(roles(DAEMON)));
    verify(javalin).post(eq("/v2/daemon-api/servers/:server/units-destroyed"), any(), eq(roles(DAEMON)));
  }

  @Test
  public void testVersion() throws Exception {
    endpoints.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  public void testWarehouses() {
    var request = sampleWarehouseSpent();
    given(ctx.pathParam("server", String.class))
      .willReturn(Validator.create(String.class, "server1"));
    given(ctx.bodyAsClass(ImmutableWarehousesSpent.class))
      .willReturn(request);

    endpoints.warehouseChanged(ctx);

    verify(repository).reportWarehouses("server1", request);
  }

  @Test
  public void testDeadUnits() {
    var uuid = UUID.randomUUID();
    var request = new String[]{ uuid.toString() };
    given(ctx.bodyAsClass(String[].class))
      .willReturn(request);

    endpoints.destroyedUnits(ctx);

    verify(repository).reportDeadUnits(List.of(uuid));
  }

  @Test
  public void testMovedUnits() {
    var item = sampleMovedUnit();
    var request = new ImmutableFactionUnitPosition[] { item };
    given(ctx.bodyAsClass(ImmutableFactionUnitPosition[].class))
      .willReturn(request);

    endpoints.movedUnits(ctx);

    verify(repository).reportMovedUnits(List.of(item));
  }

  static ImmutableFactionUnitPosition sampleMovedUnit() {
    return ImmutableFactionUnitPosition.builder()
      .id(UUID.randomUUID())
      .location(ImmutableLocation.builder()
        .latitude(BigDecimal.ONE)
        .longitude(BigDecimal.ONE)
        .altitude(BigDecimal.ONE)
        .angle(BigDecimal.ONE)
        .build())
      .build();
  }

  static ImmutableWarehousesSpent sampleWarehouseSpent() {
    return ImmutableWarehousesSpent.builder()
      .data(List.of())
      .build();
  }
}
