package factionfiction.api.v2.campaignfaction;

import base.game.Airbases;
import java.math.BigDecimal;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
@Value.Style(jdkOnly = true)
public interface CampaignFaction {
  String factionName();
  String campaignName();
  Airbases airbase();
  Integer zoneSizeFt();
  BigDecimal credits();
}
