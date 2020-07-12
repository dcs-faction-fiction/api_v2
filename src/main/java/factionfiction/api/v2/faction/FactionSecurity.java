package factionfiction.api.v2.faction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import java.util.List;

public class FactionSecurity implements FactionService {

  final FactionServiceImpl impl;
  final AuthInfo authInfo;

  public FactionSecurity(FactionServiceImpl impl, AuthInfo authInfo) {
    this.impl = impl;
    this.authInfo = authInfo;
  }

  @Override
  public List<Faction> getFactions() {
    if (!canViewFactions())
      throw cannotViewFactionsError();

    return impl.getFactions(authInfo.getUserUUID());
  }

  boolean canCreateFactions() {
    return authInfo.isFactionManager();
  }

  static RuntimeException cannotCreateFactionsError() {
    return new NotAuthorizedException("Cannot create factions");
  }

  @Override
  public Faction newFaction(String name) {
    if (!canCreateFactions())
      throw cannotCreateFactionsError();

    return impl.newFaction(name, authInfo.getUserUUID());
  }

  boolean canViewFactions() {
    return authInfo.isFactionManager();
  }

  static RuntimeException cannotViewFactionsError() {
    return new NotAuthorizedException("Cannot view factions");
  }

}
