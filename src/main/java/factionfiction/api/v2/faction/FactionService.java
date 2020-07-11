package factionfiction.api.v2.faction;

import java.util.List;

public interface FactionService {
  List<Faction> getFactions();
  Faction newFaction(String name);
}
