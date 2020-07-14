package factionfiction.api.v2.campaignfaction;

import base.game.Airbases;
import factionfiction.api.v2.game.GameOptions;
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

  public static CampaignFaction fromCampaignAndFactionAndOptions(
    String campaignName,
    String factionName,
    Airbases airbase,
    GameOptions options) {

    return ImmutableCampaignFaction.builder()
      .campaignName(campaignName)
      .factionName(factionName)
      .airbase(airbase)
      .zoneSizeFt(options.zones().sizes().min())
      .credits(options.credits().starting())
      .build();
  }
}
