package factionfiction.api.v2.campaign;

import factionfiction.api.v2.game.GameOptions;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CampaignServiceImpl {

  CampaignRepository repository;

  public CampaignServiceImpl(CampaignRepository repository) {
    this.repository = repository;
  }

  public List<Campaign> listCampaigns(UUID owner) {
    Objects.requireNonNull(owner);

    return repository.listCampaigns(owner);
  }

  public Campaign newCampaign(String name, UUID owner, GameOptions options) {
    return repository.newCampaign(name, owner, options);
  }

  public Campaign find(String name) {
    return repository.find(name);
  }

  public boolean isOwner(String name, UUID owner) {
    return repository.isOwner(name, owner);
  }
}
