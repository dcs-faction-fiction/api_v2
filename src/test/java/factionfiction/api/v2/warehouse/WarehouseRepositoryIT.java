package factionfiction.api.v2.warehouse;

import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import factionfiction.api.v2.test.InMemoryDB;
import static factionfiction.api.v2.warehouse.WarehouseHelper.cleanWarehouseTable;
import static factionfiction.api.v2.warehouse.WarehouseHelper.insertSampleWarehouseItems;
import static factionfiction.api.v2.warehouse.WarehouseHelper.makeSampleWarehouseMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseRepositoryIT {

  Jdbi jdbi;
  WarehouseRepository repository;

  @BeforeEach
  void setup() {
    jdbi = InMemoryDB.jdbi();
    repository = new WarehouseRepository(jdbi);
  }

  @Test
  void testGetInventory() {
    cleanWarehouseTable(jdbi);
    insertSampleWarehouseItems(jdbi);
    var cf = makeSampleCampaignFaction();

    var map = repository.getWarehouseFromCampaignFaction(cf.campaignName(), cf.airbase());

    assertThat(map, is(makeSampleWarehouseMap()));
  }
}
