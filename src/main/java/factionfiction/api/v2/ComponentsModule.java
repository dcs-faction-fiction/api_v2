package factionfiction.api.v2;

import com.github.apilab.core.Env;
import com.github.apilab.rest.Endpoint;
import com.github.apilab.rest.auth.AuthConfiguration;
import com.github.apilab.rest.auth.ImmutableAuthConfiguration;
import dagger.Provides;
import factionfiction.api.v2.auth.Roles;
import static java.util.Collections.emptySet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

@dagger.Module
public class ComponentsModule {

  @Provides @Singleton Env env() {
    return new Env();
  }

  @Provides @Singleton AuthConfiguration authInitializer() {
    return ImmutableAuthConfiguration.builder()
    .roleMapper(Roles::valueOf)
    .build();
  }

  @Provides @Singleton Set<Endpoint> endpoints() {
    return emptySet();
  }

  @Provides @Singleton @Named("jdbiImmutables") Set<Class<?>> jdbiImmutables() {
    return emptySet();
  }
}
