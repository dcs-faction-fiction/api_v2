package factionfiction.api.v2.campaign;

import factionfiction.api.v2.game.GameOptions;
import java.util.List;

public interface CampaignService {
  public List<Campaign> listCampaigns();
  public Campaign newCampaign(String name, GameOptions options);
}
