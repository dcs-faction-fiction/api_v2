package factionfiction.api.v2.campaignfaction;

import static base.game.CampaignCoalition.BLUE;
import com.github.apilab.rest.exceptions.NotFoundException;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public class CampaignFactionRepository {

  public static final String TABLE_NAME = "campaign_faction";

  final Jdbi jdbi;

  public CampaignFactionRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public CampaignFaction newCampaignFaction(CampaignFaction campaignFaction) {
    UUID id = UUID.randomUUID();
    insert(id, campaignFaction);
    return find(id);
  }

  CampaignFaction find(UUID id) {
    return jdbi.withHandle(h -> h.select(
      "select campaign_name, faction_name, airbase, zone_size_ft, credits from campaign_faction where id = ?", id)
      .mapTo(CampaignFaction.class)
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  void insert(
    UUID id,
    CampaignFaction campaignFaction) {

    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction ("
        + "id, "
        + "campaign_name, "
        + "faction_name, "
        + "airbase, "
        + "is_blue, "
        + "zone_size_ft, "
        + "credits)"
        + " values(?, ?, ?, ?, ?, ?, ?)",
      id,
      campaignFaction.campaignName(),
      campaignFaction.factionName(),
      campaignFaction.airbase(),
      campaignFaction.airbase().coalition() == BLUE,
      campaignFaction.zoneSizeFt(),
      campaignFaction.credits()
    ));
  }

}
