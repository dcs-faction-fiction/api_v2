package factionfiction.api.v2.campaign;

import factionfiction.api.v2.daemon.ServerInfo;
import factionfiction.api.v2.game.GameOptions;
import java.util.List;
import java.util.Optional;

public interface CampaignService {
  public Campaign find(String name);
  public List<Campaign> listCampaigns();
  public Campaign newCampaign(String name, GameOptions options);
  public void startMission(String campaignName, String serverName);
  public Optional<ServerInfo> getServerInfo(String campaignName);
}
