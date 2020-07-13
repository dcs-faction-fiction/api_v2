package factionfiction.api.v2;

import com.github.apilab.core.Env;
import com.github.apilab.rest.Endpoint;
import com.github.apilab.rest.auth.AuthConfiguration;
import com.github.apilab.rest.auth.ImmutableAuthConfiguration;
import com.github.apilab.rest.exceptions.ServerException;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import factionfiction.api.v2.auth.Roles;
import factionfiction.api.v2.common.CommonEndpoints;
import factionfiction.api.v2.faction.Faction;
import factionfiction.api.v2.faction.FactionEndpoints;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.faction.FactionServiceImpl;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

@dagger.Module
public class ComponentsModule {

  @Provides
  @Singleton
  public Env env() {
    return new Env();
  }

  @Provides
  @Singleton
  public AuthConfiguration authInitializer() {
    return ImmutableAuthConfiguration.builder()
    .roleMapper(Roles::valueOf)
    .build();
  }

  @Provides
  @Singleton
  @Named("jdbiImmutables")
  public Set<Class<?>> jdbiImmutables() {
    return Set.of(Faction.class);
  }

  @Provides
  @IntoSet
  public Endpoint factionEndpoints(Jdbi jdbi) {
    var repository = new FactionRepository(jdbi);
    var impl = new FactionServiceImpl(repository);
    return new FactionEndpoints(impl);
  }

  @Provides
  @IntoSet
  public Endpoint commonEndpoints(@Named("defaults") GameOptions defaultOptions) {
    return new CommonEndpoints(defaultOptions);
  }

  @Provides
  @Singleton
  public GameOptionsLoader gameOptionsLoader() {
    return new GameOptionsLoader();
  }

  @Provides
  @Singleton
  @Named("defaults")
  public GameOptions defaultGameOptions(GameOptionsLoader loader) {
    try {
      return loader.loadDefaults();
    } catch (IOException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }
}
