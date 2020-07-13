package factionfiction.api.v2.faction;

import java.util.List;

public interface FactionService {
  List<Faction> listFactions();
  Faction newFaction(String name);
}
