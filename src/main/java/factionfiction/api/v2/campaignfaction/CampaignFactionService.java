package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import base.game.Location;
import factionfiction.api.v2.game.GameOptions;
import java.util.List;
import java.util.UUID;

public interface CampaignFactionService {
  CampaignFaction newCampaignFaction(CampaignFaction campaignFaction);
  FactionSituation getSituation(String campaignName, String factionName);
  GameOptions getGameOptions(String campaignName, String factionName);
  List<String> getAvailableCampaigns(String factionName);
  List<FactionSituation> getAllFactions(String campaignName);
  List<FactionSituation> getAlliedFactions(String campaignName);
  void moveUnit(String campaignName, String factionName, UUID uid, Location location);
}
