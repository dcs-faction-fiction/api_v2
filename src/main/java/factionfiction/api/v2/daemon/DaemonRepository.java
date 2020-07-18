package factionfiction.api.v2.daemon;

import base.game.Airbases;
import base.game.warehouse.WarehouseItemCode;
import com.github.apilab.rest.exceptions.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class DaemonRepository {

  final Jdbi jdbi;

  public DaemonRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public void reportWarehouses(String serverName, WarehousesSpent request) {
    jdbi.useHandle(h ->
      request.toWarehouseDelta().entrySet().forEach(e -> {
        var airbase = e.getKey();
        var baseInventory = e.getValue();
        var warehouseUUID = ensureWarehouseExists(h, getCampaignNameFromServerId(serverName), airbase);
        baseInventory.entrySet().forEach(inventoryItem -> {
          var itemCode = inventoryItem.getKey();
          var amount = Optional.ofNullable(inventoryItem.getValue()).orElse(0);
          var itemUUID = ensureWarehouseItemExists(h, warehouseUUID, itemCode);
          h.execute(
            "update campaign_airfield_warehouse_item"
              + " set item_quantity = greatest(item_quantity + ?, 0)"
              + " where id = ?",
            amount, itemUUID);
        });
      })
    );
  }

  public void reportDeadUnits(List<UUID> uuids) {
    jdbi.useHandle(h ->
      uuids.forEach(uuid ->
        h.execute("delete from campaign_faction_units where id = ?", uuid)
      )
    );
  }

  public void reportMovedUnits(List<ImmutableFactionUnitPosition> units) {
    jdbi.useHandle(h ->
      units.stream().forEach(u ->
        h.execute("update campaign_faction_units set "
          + "x = ?, "
          + "y = ?, "
          + "z = ?, "
          + "angle = ?"
          + "where id = ?",
          u.location().longitude().toString(),
          u.location().latitude().toString(),
          u.location().altitude().toString(),
          u.location().angle().toString(),
          u.id())
      )
    );
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
