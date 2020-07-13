package factionfiction.api.v2.campaignfaction;

import static base.game.Airbases.KUTAISI;
import factionfiction.api.v2.campaignfaction.ImmutableCampaignFaction;
import java.math.BigDecimal;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public final class CampaignFactionHelper {

  private CampaignFactionHelper() {
  }

  public static void cleanCampaignFactionTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table "
      + CampaignFactionRepository.TABLE_NAME));
  }

  public static void insertSampleCampaignFaction(Jdbi jdbi, UUID owner) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction ("
        + "campaign_name, "
        + "faction_name, "
        + "airbase, "
        + "is_blue, "
        + "zone_size_ft, "
        + "credits)"
        + " values(?, ?, ?, ?, ?, ?)",
      "campaign name",
      "faction name",
      KUTAISI,
      true,
      50_000,
      new BigDecimal(30)));
  }

  public static CampaignFaction makeSampleCampaignFaction() {
    return ImmutableCampaignFaction.builder()
      .campaignName("campaign name")
      .factionName("faction name")
      .airbase(KUTAISI)
      .zoneSizeFt(50_000)
      .credits(new BigDecimal(30))
      .build();
  }
}
