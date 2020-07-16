package factionfiction.api.v2.warehouse;

import base.game.warehouse.WarehouseItemCode;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public final class WarehouseHelper {

  public WarehouseHelper() {
  }

  public static void cleanWarehouseTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table campaign_airfield_warehouse"));
    jdbi.useHandle(h -> h.execute("truncate table campaign_airfield_warehouse_item"));
  }

  public static void insertSampleWarehouseItems(Jdbi jdbi) {
    UUID id = UUID.randomUUID();
    var cf = makeSampleCampaignFaction();
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse"
        + " (id, campaign_name, airbase)"
        + " values(?, ?, ?)",
      id,
      cf.campaignName(),
      cf.airbase()
    ));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse_item"
        + " (id, warehouse_id, item_code, item_quantity)"
        + " values(?, ?, ?, ?)",
      UUID.randomUUID(), id,
      WarehouseItemCode.F_A_18_C, 1
    ));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse_item"
        + " (id, warehouse_id, item_code, item_quantity)"
        + " values(?, ?, ?, ?)",
      UUID.randomUUID(), id,
      WarehouseItemCode.AGM_65_D, 2
    ));
  }

  public static Map<WarehouseItemCode, Integer> makeSampleWarehouseMap() {
    return Map.of(
      WarehouseItemCode.F_A_18_C, 1,
      WarehouseItemCode.AGM_65_D, 2);
  }
}
