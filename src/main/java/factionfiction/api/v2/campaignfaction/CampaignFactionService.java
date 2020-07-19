package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;
import factionfiction.api.v2.game.GameOptions;

public interface CampaignFactionService {
  CampaignFaction newCampaignFaction(CampaignFaction campaignFaction);
  FactionSituation getSituation(String campaignName, String factionName);
  GameOptions getGameOptions(String campaignName, String factionName);
}
