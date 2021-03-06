package factionfiction.api.v2.campaign;

import base.game.units.ImmutableMissionConfiguration;
import base.game.units.ImmutableMissionOptions;
import base.game.units.ImmutableMissionTime;
import base.game.units.ImmutableMissionWeather;
import static base.game.units.MissionOptionExternalView.optview_all;
import com.github.apilab.core.GSONModule;
import com.google.gson.Gson;
import static factionfiction.api.v2.campaign.CampaignHelper.cleanCampaignTable;
import static factionfiction.api.v2.campaign.CampaignHelper.insertSampleCampaign;
import static factionfiction.api.v2.campaign.CampaignHelper.makeSampleCampaign;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.cleanCampaignFactionTable;
import static factionfiction.api.v2.campaignfaction.CampaignFactionHelper.insertSampleCampaignFaction;
import factionfiction.api.v2.daemon.ImmutableServerInfo;
import factionfiction.api.v2.game.GameOptions;
import factionfiction.api.v2.game.GameOptionsLoader;
import factionfiction.api.v2.test.InMemoryDB;
import java.io.IOException;
import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CampaignRepositoryIT {

  UUID owner;
  Jdbi jdbi;
  CampaignRepository repository;
  Gson gson;
  GameOptions defaultOptions;

  @BeforeEach
  void setup() throws IOException {
    owner = UUID.randomUUID();
    jdbi = InMemoryDB.jdbi();
    gson = new GSONModule().gson();
    defaultOptions = new GameOptionsLoader().loadDefaults();
    repository = new CampaignRepository(jdbi, gson, defaultOptions);
  }

  @Test
  void testCampaignListZero() {
    cleanCampaignTable(jdbi);

    var campaigns = repository.listCampaigns(owner);

    assertThat(campaigns, is(emptyList()));
  }

  @Test
  void testCampaignListOne() throws IOException {
    cleanCampaignTable(jdbi);

    insertSampleCampaign(jdbi, owner);

    var campaigns = repository.listCampaigns(owner);

    assertThat(campaigns, is(List.of(makeSampleCampaign())));
  }

  @Test
  void testCampaignSetOptions() throws IOException {
    cleanCampaignTable(jdbi);
    insertSampleCampaign(jdbi, owner);

    var options = new GameOptionsLoader().loadDefaults();

    repository.setGameOptions("Campaign name", options);
    var campaign = repository.listCampaigns(owner).get(0);

    assertThat(campaign.gameOptions(), is(options));
  }

  @Test
  void testNewCampaign() throws IOException {
    cleanCampaignTable(jdbi);
    var campaignToCreate = makeSampleCampaign();

    var campaign = repository.newCampaign(campaignToCreate.name(), owner, defaultOptions);
    var gampaigns = repository.listCampaigns(owner);

    assertThat(campaign, is(campaignToCreate));
    assertThat(gampaigns, is(List.of(campaignToCreate)));
  }

  @Test
  void testIsOwner() throws IOException {
    cleanCampaignTable(jdbi);
    insertSampleCampaign(jdbi, owner);
    var sample = makeSampleCampaign();

    var isOwner = repository.isOwner(sample.name(), owner);

    assertThat(isOwner, is(true));
  }

  @Test
  void testIsNotOwner() throws IOException {
    cleanCampaignTable(jdbi);
    insertSampleCampaign(jdbi, owner);
    var sample = makeSampleCampaign();

    var isOwner = repository.isOwner(sample.name(), UUID.randomUUID());

    assertThat(isOwner, is(false));
  }

  @Test
  void testGetAvailableCampaignsForFaction() throws IOException {
    cleanCampaignFactionTable(jdbi);
    insertSampleCampaignFaction(jdbi, UUID.randomUUID(), owner);

    var result = repository.getAvailableCampaignsForFaction("faction name");

    assertThat(result, is(List.of("campaign name")));
  }

  @Test
  void testCanManageServer() {
    var uuid = UUID.randomUUID();
    cleanUserServerTable(jdbi);

    var resultFalse = repository.userCanManageServer(uuid, "server1");

    insertSampleUserServer(jdbi, uuid, "server1");

    var resultTrue = repository.userCanManageServer(uuid, "server1");

    assertThat(resultFalse, is(false));
    assertThat(resultTrue, is(true));
  }

  @Test
  void testGetServerInfo() {
    cleanServerTable(jdbi);
    insertSampleServer(jdbi, "campaign");

    var info = repository.getInfoFromCampaign("campaign");

    assertThat(info, is(Optional.of(ImmutableServerInfo.builder()
      .address("addr")
      .port(10)
      .password("pwd")
      .build())));
  }

  @Test
  void testStartMissionNoServerExisting() {
    var conf = makeMissionConfiguration();

    cleanServerTable(jdbi);
    cleanAndInsertMissionData(jdbi);

    repository.startMission("campaign", "server1", conf);

    var result = jdbi.withHandle(h -> h.select(
      "select running from server where name = ?",
      "server1")
      .mapTo(Boolean.class)
      .findFirst()
      .get());

    assertThat(result, is(true));
  }

  @Test
  void testStartMissionServerExisting() {
    var conf = makeMissionConfiguration();
    cleanServerTable(jdbi);
    cleanAndInsertMissionData(jdbi);
    insertSampleServer(jdbi, "campaign");

    repository.startMission("campaign", "server1", conf);

    var result = jdbi.withHandle(h -> h.select(
      "select running from server where name = ?",
      "server1")
      .mapTo(Boolean.class)
      .findFirst()
      .get());

    assertThat(result, is(true));
  }

  ImmutableMissionConfiguration makeMissionConfiguration() {
    var conf = ImmutableMissionConfiguration.builder()
      .options(ImmutableMissionOptions.builder()
        .externalViews(true)
        .externalViewType(optview_all)
        .build())
      .time(ImmutableMissionTime.builder()
        .year(2020)
        .month(6)
        .day(1)
        .hours(8)
        .minutes(0)
        .seconds(0)
        .build())
      .missionDurationSeconds(3600 * 2)
      .gameMasterEnabled(true)
      .tacticalCommanderSlots(8)
      .weather(ImmutableMissionWeather.builder()
        .random(true)
        .build())
      .build();
    return conf;
  }

  void cleanUserServerTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table user_server"));
  }

  void insertSampleUserServer(Jdbi jdbi, UUID uuid, String server) {
    jdbi.useHandle(h -> h.execute("insert into user_server(user_id, server_name) values(?, ?)",
      uuid, server));
  }

  void cleanServerTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table server"));
  }

  void insertSampleServer(Jdbi jdbi, String campaignName) {
    jdbi.useHandle(h -> h.execute("insert into server(name, address, port, password, campaign_name) values(?, ?, ?, ?, ?)",
      "server1", "addr", 10, "pwd", campaignName));
  }

  void cleanAndInsertMissionData(Jdbi jdbi) {
    UUID cfid = UUID.randomUUID();
    UUID cfid2 = UUID.randomUUID();

    jdbi.useHandle(h -> h.execute("truncate table campaign_faction"));
    jdbi.useHandle(h -> h.execute("truncate table campaign_airfield_warehouse"));
    jdbi.useHandle(h -> h.execute("truncate table campaign_airfield_warehouse_item"));
    jdbi.useHandle(h -> h.execute("truncate table campaign_faction_units"));

    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
      + " values(?, ?, ?, ?, false)",
      cfid, "campaign", "faction", "ANAPA"));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse(id, campaign_name, airbase)"
      + " values(?, ?, ?)",
      cfid, "campaign", "ANAPA"));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse_item(id, warehouse_id, item_code, item_quantity)"
      + " values(?, ?, ?, ?)",
      cfid, cfid, "JF_17", 1));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction_units(id, campaign_faction_id, type, x, y, z, angle)"
      + " values(?, ?, ?, ?, ?, ?, ?)",
      cfid, cfid, "T_80", 1, 1, 1, 1));

    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction(id, campaign_name, faction_name, airbase, is_blue)"
      + " values(?, ?, ?, ?, true)",
      cfid2, "campaign", "faction2", "KUTAISI"));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse(id, campaign_name, airbase)"
      + " values(?, ?, ?)",
      cfid2, "campaign", "KUTAISI"));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_airfield_warehouse_item(id, warehouse_id, item_code, item_quantity)"
      + " values(?, ?, ?, ?)",
      cfid2, cfid2, "JF_17", 1));
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction_units(id, campaign_faction_id, type, x, y, z, angle)"
      + " values(?, ?, ?, ?, ?, ?, ?)",
      cfid2, cfid2, "T_80", 1, 1, 1, 1));
  }
}
