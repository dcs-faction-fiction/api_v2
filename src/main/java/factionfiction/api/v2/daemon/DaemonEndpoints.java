package factionfiction.api.v2.daemon;

import com.github.apilab.rest.Endpoint;
import static factionfiction.api.v2.auth.Roles.DAEMON;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiParam;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.util.Arrays;
import java.util.Map;
import static java.util.Optional.empty;
import java.util.UUID;
import static java.util.stream.Collectors.toList;

public class DaemonEndpoints implements Endpoint {

  final DaemonRepository repository;

  public DaemonEndpoints(DaemonRepository repository) {
    this.repository = repository;
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/v2/daemon-api", this, roles(DAEMON));
    javalin.post("/v2/daemon-api/servers/:server/warehouse-changed", this::warehouseChanged, roles(DAEMON));
    javalin.post("/v2/daemon-api/servers/:server/units-moved", this::movedUnits, roles(DAEMON));
    javalin.post("/v2/daemon-api/servers/:server/units-destroyed", this::destroyedUnits, roles(DAEMON));
  }

  @OpenApi(ignore = true)
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.json(Map.of("version", "2"));
  }

  @OpenApi(
    description = "Daemon reserved: submits spent ammo from mission",
    pathParams = {
      @OpenApiParam(name = "serverid", type = String.class)
    },
    requestBody = @OpenApiRequestBody(
      content = @OpenApiContent(from = WarehousesSpent.class)),
    responses = {@OpenApiResponse(status = "201")})
  public void warehouseChanged(Context ctx) {
    var serverId = ctx.pathParam("server", String.class).get();
    var spent = ctx.bodyAsClass(ImmutableWarehousesSpent.class);

    repository.reportWarehouses(serverId, spent);

    ctx.result("{}");
    ctx.status(201);
  }

  @OpenApi(
    description = "Receives dead units from the mission",
    requestBody = @OpenApiRequestBody(
      content = @OpenApiContent(from = FactionUnitPosition.class, isArray = true)),
    responses = {@OpenApiResponse(status = "201")}
  )
  public void movedUnits(Context ctx) {
    var moved = Arrays.asList(ctx.bodyAsClass(ImmutableFactionUnitPosition[].class));

    repository.reportMovedUnits(moved);

    ctx.result("{}");
    ctx.status(201);
  }

  @OpenApi(
    description = "Receives dead units from the mission",
    requestBody = @OpenApiRequestBody(
      content = @OpenApiContent(from = String.class, isArray = true)),
    responses = {@OpenApiResponse(status = "201")}
  )
  public void destroyedUnits(Context ctx) {
    var deadUnits = ctx.bodyAsClass(String[].class);
    var uuids = Arrays.asList(deadUnits).stream().map(UUID::fromString).collect(toList());

    repository.reportDeadUnits(uuids);

    ctx.result("{}");
    ctx.status(201);
  }
}
