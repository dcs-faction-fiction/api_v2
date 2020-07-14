package factionfiction.api.v2.campaignfaction;

import factionfiction.api.v2.campaign.CampaignRepository;
import factionfiction.api.v2.faction.FactionRepository;
import java.util.Objects;

public class CampaignFactionServiceImpl {

  final CampaignRepository campRepo;
  final FactionRepository facRepo;
  final CampaignFactionRepository repository;

  public CampaignFactionServiceImpl(
    CampaignRepository campRepo,
    FactionRepository facRepo,
    CampaignFactionRepository repository) {

    this.campRepo = campRepo;
    this.facRepo = facRepo;
    this.repository = repository;
  }

  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {

    Objects.requireNonNull(campaignFaction);

    Objects.requireNonNull(campRepo.find(campaignFaction.campaignName()));
    Objects.requireNonNull(facRepo.find(campaignFaction.factionName()));

    return repository.newCampaignFaction(campaignFaction);
  }

}
