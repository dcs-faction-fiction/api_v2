package factionfiction.api.v2.faction;

import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public final class FactionHelper {

  private FactionHelper() {
  }

  public static void cleanFactionTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table "
      + FactionRepository.TABLE_NAME));
  }

  public static void insertSampleFaction(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute(
      "insert into faction (name, commander_user) values(?, ?)",
      "name",
      UUID.randomUUID()));
  }

  public static Faction makeSampleFaction() {
    return ImmutableFaction.builder()
      .name("name")
      .build();
  }
}
