package factionfiction.api.v2;

import com.github.apilab.core.Env;
import com.github.apilab.rest.Endpoint;
import com.github.apilab.rest.auth.AuthConfiguration;
import com.github.apilab.rest.auth.ImmutableAuthConfiguration;
import com.github.apilab.rest.exceptions.ServerException;
import com.google.gson.Gson;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import factionfiction.api.v2.auth.Roles;
import factionfiction.api.v2.campaign.Campaign;
import factionfiction.api.v2.campaign.CampaignEndpoints;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaign.CampaignServiceImpl;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.campaignfaction.CampaignFactionServiceImpl;
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
    return Set.of(
      Faction.class,
      Campaign.class,
      CampaignFaction.class);
  }

  @Provides
  public FactionRepository factionRepository(Jdbi jdbi) {
    return new FactionRepository(jdbi);
  }

  @Provides
  public CampaignRepository campaignRepository(Jdbi jdbi, Gson gson, @Named("defaults") GameOptions defaultOptions) {
    return new CampaignRepository(jdbi, gson, defaultOptions);
  }

  @Provides
  public CampaignFactionRepository campaignFactionRepository(Jdbi jdbi) {
    return new CampaignFactionRepository(jdbi);
  }

  @Provides
  public FactionServiceImpl factionService(FactionRepository facRepo) {
    return new FactionServiceImpl(facRepo);
  }

  @Provides
  public CampaignServiceImpl campaignService(CampaignRepository campRepo) {
    return new CampaignServiceImpl(campRepo);
  }

  @Provides
  public CampaignFactionServiceImpl campaignFactionService(
    CampaignRepository campRepo,
    FactionRepository facRepo,
    CampaignFactionRepository repo) {

    return new CampaignFactionServiceImpl(campRepo, facRepo, repo);
  }

  @Provides
  @IntoSet
  public Endpoint factionEndpoints(FactionServiceImpl impl) {
    return new FactionEndpoints(impl);
  }

  @Provides
  @IntoSet
  public Endpoint campaignEndpoints(
    CampaignServiceImpl impl,
    CampaignFactionServiceImpl cfImpl) {

    return new CampaignEndpoints(impl, cfImpl);
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
