package factionfiction.api.v2.campaign;

import factionfiction.api.v2.game.GameOptionsLoader;
import java.io.IOException;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public final class CampaignHelper {

  private CampaignHelper() {
  }

  public static void cleanCampaignTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table "
      + CampaignRepository.TABLE_NAME));
  }

  public static void insertSampleCampaign(Jdbi jdbi, UUID owner) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign (name, manager_user) values(?, ?)",
      "Campaign name",
      owner));
  }

  public static Campaign makeSampleCampaign() throws IOException {
    return ImmutableCampaign.builder()
      .name("Campaign name")
      .gameOptions(new GameOptionsLoader().loadDefaults())
      .build();
  }

}
