package factionfiction.api.v2.faction;

import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FactionServiceTest {
  FactionRepository repository;
  FactionServiceImpl service;

  @BeforeEach
  public void setup() {
    repository = mock(FactionRepository.class);
    service = new FactionServiceImpl(repository);
  }

  @Test
  public void testNoFactionsAtStart() {
    given(repository.getFactions(any())).willReturn(emptyList());

    var factions = service.getFactions(UUID.randomUUID());

    assertThat(factions, is(emptyList()));
  }

  @Test
  public void testFactionsReturned() {
    var list = List.of(makeSampleFaction());
    given(repository.getFactions(any())).willReturn(list);

    var factions = service.getFactions(UUID.randomUUID());

    assertThat(factions, is(list));
  }

  @Test
  public void testCreateNewFaction() {
    var uuid = UUID.randomUUID();
    String name = "test name";
    service.newFaction(name, uuid);

    verify(repository).newFaction(name, uuid);
  }

  @Test
  public void testCreateNewFactionNull() {
    assertThrows(NullPointerException.class, () -> {
      service.newFaction(null, null);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "",
    "with, comma",
    "with bang!",
    "with question?"})
  public void testInvalidFactionNames(String name) {
    var ex = assertThrows(RuntimeException.class, () -> {
      service.newFaction(name, UUID.randomUUID());
    });

    assertThat(ex.getMessage(), containsString("Invalid name"));
    verify(repository, times(0)).newFaction(any(), any());
  }
}
