package factionfiction.api.v2.warehouse;

import base.game.Airbases;
import base.game.warehouse.WarehouseItemCode;
import java.util.EnumMap;
import java.util.Map;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultSetAccumulator;
import org.jdbi.v3.core.statement.Query;

public class WarehouseRepository {

  final Jdbi jdbi;

  public WarehouseRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public Map<WarehouseItemCode, Integer> getWarehouseFromCampaignFaction(String campaignName, Airbases airbase) {
    return jdbi.withHandle(h ->
      selectInventory(campaignName, airbase, h)
      .reduceResultSet(newMap(), mapToMap()));
  }

  static Query selectInventory(String campaignName, Airbases airbase, Handle h) {
    return h.select(
      "select wi.item_code, wi.item_quantity"
        + " from campaign_airfield_warehouse_item wi"
        + " left join campaign_airfield_warehouse w on wi.warehouse_id = w.id"
        + " where w.campaign_name = ? and w.airbase = ?",
      campaignName, airbase);
  }

  static Map<WarehouseItemCode, Integer> newMap() {
    return new EnumMap<>(WarehouseItemCode.class);
  }

  static ResultSetAccumulator<Map<WarehouseItemCode, Integer>> mapToMap() {
    return (prev, rs, ctx) -> {
      prev.put(
        WarehouseItemCode.valueOf(rs.getString(1)),
        rs.getInt(2));
      return prev;
    };
  }

}
