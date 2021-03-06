package factionfiction.api.v2.faction;

import com.github.apilab.rest.exceptions.NotAuthorizedException;
import factionfiction.api.v2.auth.AuthInfo;
import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FactionSecurityTest {

  AuthInfo authInfo;
  FactionServiceImpl impl;
  FactionSecurity security;

  @BeforeEach
  void setup() {
    authInfo = mock(AuthInfo.class);
    impl = mock(FactionServiceImpl.class);
    security = new FactionSecurity(impl, authInfo);
  }

  @Test
  void testCanCreateFactionWhenFactionManager() {
    String name = "valid name";
    var uuid = UUID.randomUUID();
    given(authInfo.getUserUUID()).willReturn(uuid);
    given(authInfo.isFactionManager()).willReturn(true);

    security.newFaction(name);

    verify(impl).newFaction(name, uuid);
  }

  @Test
  void testCannotCreateFactionWhenNotFactionManager() {
    String name = "valid name";
    given(authInfo.isFactionManager()).willReturn(false);

    var ex = assertThrows(NotAuthorizedException.class, () -> {
      security.newFaction(name);
    });

    assertThat(ex.getMessage(), containsString("Cannot create factions"));
    verify(impl, times(0)).newFaction(any(), any());
  }

  @Test
  void testCanViewFactionsWhenFactionManager() {
    var list = List.of(makeSampleFaction());
    given(impl.listFactions(any())).willReturn(list);
    given(authInfo.isFactionManager()).willReturn(true);

    var factions = security.listFactions();

    assertThat(factions, is(list));
  }

  @Test
  void testCannotViewFactionsWhenNotFactionManager() {
    given(authInfo.isFactionManager()).willReturn(false);

    var ex = assertThrows(NotAuthorizedException.class, () -> {
      security.listFactions();
    });

    assertThat(ex.getMessage(), containsString("Cannot view factions"));
    verify(impl, times(0)).listFactions(any());
  }
}
