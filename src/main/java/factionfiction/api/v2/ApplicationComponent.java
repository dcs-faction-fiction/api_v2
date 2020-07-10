package factionfiction.api.v2;

import com.github.apilab.core.ApplicationLifecycle;
import com.github.apilab.core.GSONModule;
import com.github.apilab.jdbi.JdbiModule;
import com.github.apilab.rest.JavalinModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
  ComponentsModule.class,
  GSONModule.class,
  JavalinModule.class,
  JdbiModule.class})
public interface ApplicationComponent {
  ApplicationLifecycle instance();
}
