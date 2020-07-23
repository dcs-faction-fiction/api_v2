package factionfiction.api.v2.campaignfaction;

import static base.game.CampaignCoalition.BLUE;
import com.github.apilab.rest.exceptions.NotAuthorizedException;
import com.github.apilab.rest.exceptions.NotFoundException;
import java.util.List;
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

  public UUID getCampaignFactionId(String campaignName, String factionName) {
    return jdbi.withHandle(h -> h.select(
      "select id from campaign_faction where campaign_name = ? and faction_name = ?",
      campaignName,
      factionName)
      .mapTo(UUID.class)
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  public CampaignFaction getCampaignFaction(UUID id) {
    return find(id);
  }

  public List<String> getAllFactionNamesOfCampaign(String campaignName, UUID userId) {
    return jdbi.withHandle(h -> h.select(
      "select cf.faction_name from campaign_faction cf"
        + " left join campaign c on cf.campaign_name = c.name"
        + " where cf.campaign_name = ? and c.manager_user = ?",
      campaignName,
      userId)
      .mapTo(String.class)
      .list());
  }

  public List<String> getAlliedFactionNamesOfCampaign(String campaignName, UUID userId) {
    // Important on security, in this case permission filtering is done
    // through joining in selects for owner for allied factions.
    var isBlue = jdbi.withHandle(h -> h.select(
      "select is_blue from campaign_faction cf"
        + " left join faction f on cf.faction_name = f.name"
        + " where cf.campaign_name = ? and f.commander_user = ?",
      campaignName,
      userId)
      .mapTo(Boolean.class)
      .findFirst()
      .orElseThrow(() -> new NotAuthorizedException("Not part of this campaign"))
    );

    return jdbi.withHandle(h -> h.select(
      "select cf.faction_name from campaign_faction cf"
        + " left join campaign c on cf.campaign_name = c.name"
        + " where cf.campaign_name = ? and is_blue = ?",
      campaignName,
      isBlue)
      .mapTo(String.class)
      .list());
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
