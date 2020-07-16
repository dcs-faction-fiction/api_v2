package factionfiction.api.v2.campaignfaction;

import base.game.FactionSituation;

public interface CampaignFactionService {
  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction);
  public FactionSituation getSituation(String campaignName, String factionName);
}
