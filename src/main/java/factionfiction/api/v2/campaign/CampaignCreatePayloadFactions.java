package factionfiction.api.v2.campaign;

import base.game.Airbases;
import base.game.CampaignCoalition;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface CampaignCreatePayloadFactions {
  Airbases airbase();
  CampaignCoalition coalition();
  String faction();
}
