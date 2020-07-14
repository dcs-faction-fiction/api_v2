package factionfiction.api.v2;

import com.github.apilab.core.Env;
import com.github.apilab.rest.Endpoint;
import com.github.apilab.rest.auth.AuthConfiguration;
import com.github.apilab.rest.auth.ImmutableAuthConfiguration;
import com.github.apilab.rest.exceptions.ServerException;
import com.google.gson.Gson;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import factionfiction.api.v2.auth.AuthInfo;
import factionfiction.api.v2.auth.Roles;
import factionfiction.api.v2.campaign.Campaign;
import factionfiction.api.v2.campaign.CampaignEndpoints;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.campaign.CampaignSecurity;
import factionfiction.api.v2.campaign.CampaignService;
import factionfiction.api.v2.campaign.CampaignServiceImpl;
import factionfiction.api.v2.campaignfaction.CampaignFaction;
import factionfiction.api.v2.campaignfaction.CampaignFactionEndpoints;
import factionfiction.api.v2.campaignfaction.CampaignFactionRepository;
import factionfiction.api.v2.campaignfaction.CampaignFactionSecurity;
import factionfiction.api.v2.campaignfaction.CampaignFactionService;
import factionfiction.api.v2.campaignfaction.CampaignFactionServiceImpl;
import factionfiction.api.v2.common.CommonEndpoints;
import factionfiction.api.v2.faction.Faction;
import factionfiction.api.v2.faction.FactionEndpoints;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.faction.FactionSecurity;
import factionfiction.api.v2.faction.FactionService;
import factionfiction.api.v2.faction.FactionServiceImpl;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import io.javalin.http.Context;
import java.io.IOException;
import java.util.Set;
import java.util.function.Function;
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
  @Named("factionProvider")
  public Function<Context, FactionService> factionProvider(FactionServiceImpl impl) {
    return ctx -> new FactionSecurity(impl, AuthInfo.fromContext(ctx));
  }

  @Provides
  @Named("campaignProvider")
  public Function<Context, CampaignService> campaignProvider(CampaignServiceImpl impl) {
    return ctx -> new CampaignSecurity(impl, AuthInfo.fromContext(ctx));
  }

  @Provides
  @Named("campaignFactionProvider")
  public Function<Context, CampaignFactionService> campaignFactionProvider(
    CampaignFactionServiceImpl impl,
    CampaignRepository campaignRepository) {

    return ctx -> new CampaignFactionSecurity(impl, campaignRepository, AuthInfo.fromContext(ctx));
  }

  @Provides
  @IntoSet
  public Endpoint factionEndpoints(
    @Named("factionProvider")
    Function<Context, FactionService> fn) {

    return new FactionEndpoints(fn);
  }

  @Provides
  @IntoSet
  public Endpoint campaignEndpoints(
    @Named("campaignProvider")
    Function<Context, CampaignService> campProvider,
    @Named("campaignFactionProvider")
    Function<Context, CampaignFactionService> cfProvider) {

    return new CampaignEndpoints(campProvider, cfProvider);
  }

  @Provides
  @IntoSet
  public Endpoint campaignFactionEndpoints(
    @Named("campaignProvider")
    Function<Context, CampaignService> campProvider,
    @Named("campaignFactionProvider")
    Function<Context, CampaignFactionService> cfProvider) {

    return new CampaignFactionEndpoints(campProvider, cfProvider);
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
