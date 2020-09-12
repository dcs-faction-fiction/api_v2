package factionfiction.api.v2.units;

import factionfiction.api.v2.test.InMemoryDB;
import static factionfiction.api.v2.units.UnitHelper.cleanUnitTable;
import static factionfiction.api.v2.units.UnitHelper.insertSampleFactionUnit;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnitRepositoryIT {

  UUID id;
  UUID owner;
  Jdbi jdbi;
  UnitRepository repository;

  @BeforeEach
  void setup() {
    jdbi = InMemoryDB.jdbi();
    id = UUID.randomUUID();
    owner = UUID.randomUUID();
    repository = new UnitRepository(jdbi);
  }

  @Test
  void testListUnits() {
    cleanUnitTable(jdbi);

    insertSampleFactionUnit(jdbi, id, owner);

    var units = repository.getUnitsFromCampaignFaction(owner);

    assertThat(units, is(List.of(makeSampleFactionUnit(id))));
  }
}
