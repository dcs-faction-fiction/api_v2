package factionfiction.api.v2.auth;

import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_ROLES;
import static com.github.apilab.rest.auth.JavalinJWTFilter.REQ_ATTR_SUBJECT;
import com.github.apilab.rest.exceptions.NotAuthenticatedException;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import static java.util.Optional.ofNullable;
import java.util.Set;
import java.util.UUID;

public class AuthInfo {

  final UUID userId;
  final Set<Role> roles;

  private AuthInfo(UUID userId, Set<Role> roles) {
    this.userId = userId;
    this.roles = roles;
  }

  public UUID getUserUUID() {
    return userId;
  }

  public boolean isFactionManager() {
    return roles.contains(FACTION_MANAGER);
  }

  public static AuthInfo fromContext(Context ctx) {
    var user = UUID.fromString(getTokenSubject(ctx));
    var roles = getTokenRoles(ctx);
    return new AuthInfo(user, roles);
  }

  private static Set<Role> getTokenRoles(Context ctx) {
    return ofNullable((Set<Role>) ctx.attribute(REQ_ATTR_ROLES))
      .orElseThrow(() -> new NotAuthenticatedException("Missing subject in token"));
  }

  private static String getTokenSubject(Context ctx) {
    return ofNullable((String) ctx.attribute(REQ_ATTR_SUBJECT))
      .orElseThrow(() -> new NotAuthenticatedException("Missing subject in token"));
  }
}
