package factionfiction.api.v2;

import com.github.apilab.core.Env;
import com.github.apilab.rest.Endpoint;
import com.github.apilab.rest.auth.AuthConfiguration;
import com.github.apilab.rest.auth.ImmutableAuthConfiguration;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import factionfiction.api.v2.auth.Roles;
import factionfiction.api.v2.faction.Faction;
import factionfiction.api.v2.faction.FactionEndpoints;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.faction.FactionServiceImpl;
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
}
