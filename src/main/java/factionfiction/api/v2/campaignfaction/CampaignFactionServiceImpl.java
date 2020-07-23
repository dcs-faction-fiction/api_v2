package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.FactionUnit;
import base.game.ImmutableFactionAirbase;
import base.game.ImmutableFactionSituation;
import base.game.warehouse.WarehouseItemCode;
import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.faction.FactionRepository;
import factionfiction.api.v2.units.UnitRepository;
import factionfiction.api.v2.warehouse.WarehouseRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import static java.util.stream.Collectors.toList;

public class CampaignFactionServiceImpl {

  final CampaignRepository campRepo;
  final FactionRepository facRepo;
  final CampaignFactionRepository repository;
  final WarehouseRepository warehouseRepository;
  final UnitRepository unitRepository;

  public CampaignFactionServiceImpl(
    CampaignRepository campRepo,
    FactionRepository facRepo,
    CampaignFactionRepository repository,
    WarehouseRepository warehouseRepository,
    UnitRepository unitRepository) {

    this.campRepo = campRepo;
    this.facRepo = facRepo;
    this.repository = repository;
    this.warehouseRepository = warehouseRepository;
    this.unitRepository = unitRepository;
  }

  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {

    Objects.requireNonNull(campaignFaction);

    Objects.requireNonNull(campRepo.find(campaignFaction.campaignName()));
    Objects.requireNonNull(facRepo.find(campaignFaction.factionName()));

    return repository.newCampaignFaction(campaignFaction);
  }

  public FactionSituation getSituation(String campaignName, String factionName) {
    UUID id = repository.getCampaignFactionId(campaignName, factionName);
    CampaignFaction cf = repository.getCampaignFaction(id);

    Map<WarehouseItemCode, Integer> warehouse = warehouseRepository
      .getWarehouseFromCampaignFaction(cf.campaignName(), cf.airbase());

    List<FactionUnit> units = unitRepository
      .getUnitsFromCampaignFaction(id);

    var airbase = ImmutableFactionAirbase.builder()
      .name(cf.airbase().name())
      .code(cf.airbase())
      .waypoints(List.of())
      .warehouse(warehouse)
      .build();

    return ImmutableFactionSituation.builder()
      .id(id)
      .campaign(campaignName)
      .faction(factionName)
      .credits(cf.credits())
      .zoneSizeFt(cf.zoneSizeFt())
      .airbases(List.of(airbase))
      .units(units)
      .build();
  }

  public List<FactionSituation> getAllFactions(String campaignName, UUID userId) {
    List<String> factionNames = repository.getAllFactionNamesOfCampaign(campaignName, userId);
    return factionNames.stream()
      .map(name -> getSituation(campaignName, name))
      .collect(toList());
  }

  public List<FactionSituation> getAlliedFactions(String campaignName, UUID userId) {
    List<String> factionNames = repository.getAlliedFactionNamesOfCampaign(campaignName, userId);
    return factionNames.stream()
      .map(name -> getSituation(campaignName, name))
      .collect(toList());
  }

}
