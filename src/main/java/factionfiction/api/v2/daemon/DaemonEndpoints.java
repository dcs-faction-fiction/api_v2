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
import static java.util.Optional.of;
import java.util.UUID;
import static java.util.stream.Collectors.toList;

public class DaemonEndpoints implements Endpoint {

  static final String SERVER_PATHPARAM = "server";

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
    javalin.get("/v2/daemon-api/servers/:server/download-mission", this::downloadMission, roles(DAEMON));
    javalin.get("/v2/daemon-api/servers/:server/next-action", this::pullNextAction, roles(DAEMON));
    javalin.post("/v2/daemon-api/servers/:server/actions/:action", this::setAction, roles(DAEMON));
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
    var serverId = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
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
    var serverId = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
    var moved = Arrays.asList(ctx.bodyAsClass(ImmutableFactionUnitPosition[].class));

    repository.reportMovedUnits(serverId, moved);

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
    var serverId = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
    var deadUnits = ctx.bodyAsClass(String[].class);
    var uuids = Arrays.asList(deadUnits).stream().map(UUID::fromString).collect(toList());

    repository.reportDeadUnits(serverId, uuids);

    ctx.result("{}");
    ctx.status(201);
  }

  @OpenApi(
    description = "Daemon reserved: download next mission file for campaign",
    pathParams = {
      @OpenApiParam(name = "serverid", type = String.class)
    },
    responses = {
      @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/zip", from = byte[].class))
    })
  public void downloadMission(Context ctx) {
    var serverId = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
    ctx.status(200);
    ctx.contentType("application/zip");
    repository.downloadMission(serverId, ctx::result);
  }

  @OpenApi(
    description = "Daemon reserved: checks next action for server",
    pathParams = {
      @OpenApiParam(name = "serverid", type = String.class)
    },
    responses = {
      @OpenApiResponse(
        content = @OpenApiContent(from = ServerAction.class),
        status = "200")
    })
  public void pullNextAction(Context ctx) {
    var serverId = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
    ctx.json(repository.pullNextAction(serverId));
    ctx.status(200);
  }

  @OpenApi(
    description = "Sets the next action/state",
    pathParams = {@OpenApiParam(name = "action", type = ServerAction.class)},
    requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ServerInfo.class)),
    responses = {@OpenApiResponse(status = "200")}
  )
  public void setAction(Context ctx) {
    var server = ctx.pathParam(SERVER_PATHPARAM, String.class).get();
    var action = ServerAction.valueOf(ctx.pathParam("action", String.class).get());
    var info = ctx.bodyAsClass(ServerInfo.class);

    repository.setNextAction(server, action, of(info));
    ctx.status(200);
  }
}
