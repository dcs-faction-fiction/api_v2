package factionfiction.api.v2.faction;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class FactionServiceImpl {

  // Any alphanumeric with space
  private static final String VALID_FACTION_NAME_REGEX = "[a-zA-Z0-9\\s]+";
  private static final Pattern VALID_FACTION_NAME_PATTERN = Pattern.compile(VALID_FACTION_NAME_REGEX);

  final FactionRepository repository;

  public FactionServiceImpl(FactionRepository repository) {
    this.repository = repository;
  }

  public List<Faction> listFactions(UUID owner) {
    return repository.listFactions(owner);
  }

  public Faction newFaction(String name, UUID owner) {
    Objects.requireNonNull(name);

    if (!validFactionName(name))
      throw invalidNameError();

    return repository.newFaction(name, owner);
  }

  static boolean validFactionName(String name) {
    return VALID_FACTION_NAME_PATTERN.matcher(name).matches();
  }

  private RuntimeException invalidNameError() {
    return new RuntimeException("Invalid name");
  }

}
