package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.ImmutableFactionAirbase;
import base.game.ImmutableFactionSituation;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import factionfiction.api.v2.campaign.CampaignRepository;
import static factionfiction.api.v2.campaignfaction.CampaignFaction.fromCampaignAndFactionAndOptions;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.makeSampleCampaignFaction;
import static factionfiction.api.v2.faction.FactionHelper.makeSampleFaction;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.game.GameOptionsLoader;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import factionfiction.api.v2.units.UnitRepository;
import static factionfiction.api.v2.warehouse.WarehouseHelper.makeSampleWarehouseMap;
import factionfiction.api.v2.warehouse.WarehouseRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CampaignFactionServiceTest {

  CampaignFaction sample;
  CampaignFactionServiceImpl impl;
  CampaignRepository campRepo;
  FactionRepository facRepo;
  CampaignFactionRepository repository;
  WarehouseRepository warehouseRepository;
  UnitRepository unitRepository;

  UUID cfId;
  UUID unitUUID;

  @BeforeEach
  public void setup() throws IOException {
    cfId = UUID.randomUUID();
    unitUUID = UUID.randomUUID();
    sample = makeSampleCampaignFaction();
    campRepo = mock(CampaignRepository.class);
    facRepo = mock(FactionRepository.class);
    repository = mock(CampaignFactionRepository.class);
    unitRepository = mock(UnitRepository.class);
    warehouseRepository = mock(WarehouseRepository.class);
    impl = new CampaignFactionServiceImpl(
      campRepo, facRepo, repository,
      warehouseRepository, unitRepository
    );
  }

  @Test
  public void testCreateCampaignFaction() throws IOException {
    given(campRepo.find(sample.campaignName()))
      .willReturn(makeSampleCampaign());
    given(facRepo.find(sample.factionName()))
      .willReturn(makeSampleFaction());

    impl.newCampaignFaction(sample);

    verify(repository).newCampaignFaction(sample);
  }

  @Test
  public void testConverter() throws IOException {
    var options = new GameOptionsLoader().loadDefaults();
    var result = fromCampaignAndFactionAndOptions(
      sample.campaignName(),
      sample.factionName(),
      sample.airbase(),
      options);

    assertThat(result, is(sample));
  }

  @Test
  public void testGetSituation() {
    mockFactionSituation();

    var situation = impl.getSituation(
      sample.campaignName(),
      sample.factionName()
    );

    assertThat(situation, is(sampleSituation()));
  }

  void mockFactionSituation() {
    given(repository.getCampaignFactionId(sample.campaignName(), sample.factionName()))
      .willReturn(cfId);
    given(repository.getCampaignFaction(cfId))
      .willReturn(sample);
    given(warehouseRepository.getWarehouseFromCampaignFaction(sample.campaignName(), sample.airbase()))
      .willReturn(makeSampleWarehouseMap());
    given(unitRepository.getUnitsFromCampaignFaction(cfId))
      .willReturn(List.of(makeSampleFactionUnit(unitUUID)));
  }

  FactionSituation sampleSituation() {
    return ImmutableFactionSituation.builder()
      .id(cfId)
      .campaign(sample.campaignName())
      .faction(sample.factionName())
      .airbases(List.of(ImmutableFactionAirbase.builder()
        .name(sample.airbase().name())
        .code(sample.airbase())
        .waypoints(List.of())
        .warehouse(makeSampleWarehouseMap())
        .build()))
      .units(List.of(makeSampleFactionUnit(unitUUID)))
      .credits(sample.credits())
      .zoneSizeFt(sample.zoneSizeFt())
      .build();
  }
}
