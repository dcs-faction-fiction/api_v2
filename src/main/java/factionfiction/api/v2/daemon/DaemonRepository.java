package factionfiction.api.v2.daemon;

import base.game.Airbases;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import com.github.apilab.rest.exceptions.NotFoundException;
import static factionfiction.api.v2.daemon.ServerAction.MISSION_STARTED;
import static factionfiction.api.v2.daemon.ServerAction.STOP_MISSION;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static java.math.BigDecimal.ZERO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class DaemonRepository {

  final Jdbi jdbi;

  public DaemonRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public void reportWarehouses(String serverName, WarehousesSpent request) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverName);
      ensureServerHasACampaignAssigned(h, serverName);

      request.toWarehouseDelta().entrySet().forEach(e -> {
        var airbase = e.getKey();
        var baseInventory = e.getValue();
        var warehouseUUID = ensureWarehouseExists(h, getCampaignNameFromServerId(serverName), airbase);
        baseInventory.entrySet().forEach(inventoryItem -> {
          var itemCode = inventoryItem.getKey();
          var amount = Optional.ofNullable(inventoryItem.getValue()).orElse(ZERO);
          var itemUUID = ensureWarehouseItemExists(h, warehouseUUID, itemCode);
          h.execute(
            "update campaign_airfield_warehouse_item"
              + " set item_quantity = greatest(item_quantity + ?, 0)"
              + " where id = ?",
            amount, itemUUID);
        });
      });
    });
  }

  public void reportDeadUnits(String serverName, List<UUID> uuids) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverName);
      ensureServerHasACampaignAssigned(h, serverName);

      uuids.forEach(uuid ->
        h.execute("delete from campaign_faction_units where id = ?", uuid)
      );
    });
  }

  public void reportMovedUnits(String serverName, List<ImmutableFactionUnitPosition> units) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverName);
      ensureServerHasACampaignAssigned(h, serverName);

      units.stream().forEach(u ->
        h.execute("update campaign_faction_units set "
          + "x = ?, "
          + "y = ?, "
          + "z = ?, "
          + "angle = ? "
          + "where id = ?",
          u.location().longitude(),
          u.location().latitude(),
          u.location().altitude(),
          u.location().angle(),
          u.id())
      );
    });
  }

  public void downloadMission(String serverid, Consumer<InputStream> consumer) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverid);
      h.select("select mission_zip from server where name = ?", serverid)
        .mapTo(byte[].class)
        .findFirst()
        .ifPresent(b ->
          consumer.accept(new ByteArrayInputStream(b))
        );
    });
  }

  public Optional<ServerAction> pullNextAction(String serverId) {
    return jdbi.withHandle(h -> {
      ensureServerExists(h, serverId);
      var action = h.select("select next_action from server where name = ?", serverId)
        .mapTo(ServerAction.class)
        .findOne();
      action.ifPresent(a -> h.execute("update server set next_action = null, current_action = ? where name = ?", a, serverId));
      return action;
    });
  }

  public void setNextAction(String serverid, ServerAction action, Optional<ServerInfo> info) {
    jdbi.useHandle(h -> {
      ensureServerExists(h, serverid);
      h.execute("update server set next_action = ? where name = ?", action, serverid);
      info.ifPresent(i -> h.execute("update server set address = ?, port = ?, password = ? where name = ?", i.address(), i.port(), i.password(), serverid));
      if (action == MISSION_STARTED) {
        h.execute("update server set running = true, started_at = CURRENT_TIMESTAMP where name = ?", serverid);
      }
      if (action == STOP_MISSION) {
        h.execute("update server set running = false, started_at = null where name = ?", serverid);
        // At mission stop, need to change campaign status
        // Need to clear out the mission data
        h.execute("update server set mission_zip = null, campaign_name = null where name = ?", serverid);
      }
    });
  }

  private String getCampaignNameFromServerId(String serverId) {
    return jdbi.withHandle(h -> {
      ensureServerExists(h, serverId);
      return h.select("select campaign_name from server where name = ?", serverId)
        .mapTo(String.class)
        .findOne()
        .orElseThrow(() -> new NotFoundException("No campaing found running on this server."));
    });
  }

  private void ensureServerExists(Handle h, String serverid) {
    boolean exists = h.select("select name from server where name = ?", serverid)
      .mapTo(String.class)
      .findFirst()
      .isPresent();
    if (!exists) {
      h.execute("insert into server (name) values(?)", serverid);
    }
  }

  private void ensureServerHasACampaignAssigned(Handle h, String server) {
    boolean exists = h.select("select name from server where name = ? and campaign_name is not null", server)
      .mapTo(String.class)
      .findFirst()
      .isPresent();
    if (!exists)
      throw new NotAuthorizedException("Cannot operate on a server with no mission assigned");
  }

  private UUID ensureWarehouseExists(Handle h, String campaignName, Airbases airbase) {
    return h.select("select id from campaign_airfield_warehouse"
      + " where campaign_name = ? and airbase = ?", campaignName, airbase.name())
      .mapTo(UUID.class)
      .findFirst()
      .orElseGet(() -> {
        var generatedUUID = UUID.randomUUID();
        h.execute("insert into campaign_airfield_warehouse (id, campaign_name, airbase) values(?, ?, ?)",
          generatedUUID, campaignName, airbase.name());
        return generatedUUID;
      });
  }

  private UUID ensureWarehouseItemExists(Handle h, UUID whid, WarehouseItemCode itemCode) {
    return h.select("select id from campaign_airfield_warehouse_item where warehouse_id = ? and item_code = ?", whid, itemCode.name())
      .mapTo(UUID.class)
      .findFirst()
      .orElseGet(() -> {
        var generatedUUID = UUID.randomUUID();
        h.execute("insert into campaign_airfield_warehouse_item (id, warehouse_id, item_code, item_quantity) values(?, ?, ?, 0)", generatedUUID, whid, itemCode.name());
        return generatedUUID;
      });
  }
}
