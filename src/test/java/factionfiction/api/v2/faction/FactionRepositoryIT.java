package factionfiction.api.v2.faction;

import static factionfiction.api.v2.faction.FactionHelper.cleanFactionTable;
import static factionfiction.api.v2.faction.FactionHelper.insertSampleFaction;
import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import static factionfiction.api.v2.test.InMemoryDB.jdbi;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FactionRepositoryIT {

  Jdbi jdbi;
  FactionRepository repository;
  UUID owner;

  @BeforeEach
  public void setup() {
    owner = UUID.randomUUID();
    jdbi = jdbi();
    repository = new FactionRepository(jdbi);
  }

  @Test
  public void testFactionListZero() {
    cleanFactionTable(jdbi);

    var factions = repository.getFactions(owner);

    assertThat(factions, is(emptyList()));
  }

  @Test
  public void testFactionListOne() {
    cleanFactionTable(jdbi);

    insertSampleFaction(jdbi, owner);

    var factions = repository.getFactions(owner);

    assertThat(factions, is(List.of(makeSampleFaction())));
  }

  @Test
  public void testNewFaction() {
    cleanFactionTable(jdbi);

    var faction = repository.newFaction("name", owner);
    var factions = repository.getFactions(owner);

    assertThat(faction.name(), is("name"));
    assertThat(factions, is(List.of(makeSampleFaction())));
  }

}
