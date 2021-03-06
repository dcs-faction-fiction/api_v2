package factionfiction.api.v2.faction;

import com.github.apilab.rest.exceptions.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public class FactionRepository {

  public static final String TABLE_NAME = "faction";

  final Jdbi jdbi;

  public FactionRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public Faction newFaction(String name, UUID owner) {
    insert(name, owner);
    return find(name);
  }

  public List<Faction> listFactions(UUID owner) {
    return jdbi.withHandle(h -> h
      .select("select * from faction where commander_user = ?", owner)
      .mapTo(Faction.class)
      .list());
  }

  public boolean isOwner(String name, UUID owner) {
    return jdbi.withHandle(h -> h.select(
      "select name from faction where name = ? and commander_user = ?",
      name,
      owner)
      .mapTo(String.class)
      .findFirst())
      .isPresent();
  }

  void insert(String name, UUID owner) {
    jdbi.useHandle(h -> h.execute(
      "insert into faction (name, commander_user) values(?, ?)",
      name,
      owner));
  }

  public Faction find(String name) {
    return jdbi.withHandle(h -> h.select(
      "select * from faction where name = ?",  name)
      .mapTo(Faction.class)
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

}
