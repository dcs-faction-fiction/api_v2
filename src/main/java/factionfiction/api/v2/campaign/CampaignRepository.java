package factionfiction.api.v2.campaign;

import com.github.apilab.rest.exceptions.NotFoundException;
import com.google.gson.Gson;
import factionfiction.api.v2.game.GameOptions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

public class CampaignRepository {

  public static final String TABLE_NAME = "campaign";

  Jdbi jdbi;
  Gson gson;
  GameOptions defaultOptions;

  public CampaignRepository(Jdbi jdbi, Gson gson, GameOptions defaultOptions) {
    this.jdbi = jdbi;
    this.gson = gson;
    this.defaultOptions = defaultOptions;
  }

  public List<Campaign> listCampaigns(UUID owner) {
    return jdbi.withHandle(h -> h
      .select("select name, game_options from campaign where manager_user = ?", owner)
      .map(mapToCampaign())
      .list());
  }

  public Campaign newCampaign(String name, UUID owner, GameOptions options) {
    insert(name, owner, options);
    return find(name);
  }

  private void insert(String name, UUID owner, GameOptions options) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign(name, manager_user, game_options)"
        + " values(?, ?, ?)",
      name,
      owner,
      gson.toJson(options)));
  }

  public Campaign find(String name) {
    return jdbi.withHandle(h -> h.select("select name, game_options from campaign where name = ?", name)
      .map(mapToCampaign())
      .findFirst())
      .orElseThrow(() -> new NotFoundException("not found"));
  }

  private RowMapper<Campaign> mapToCampaign() {
    return (rs, ctx) -> campaignFromResultSet(rs);
  }

  private Campaign campaignFromResultSet(ResultSet rs) throws SQLException {
    return ImmutableCampaign.builder()
      .name(rs.getString("name"))
      .gameOptions(parseOptionOrDefaults(rs))
      .build();
  }

  private GameOptions parseOptionOrDefaults(ResultSet rs) throws SQLException {
    return Optional.ofNullable(rs.getString("game_options"))
      .map(s -> gson.fromJson(s, GameOptions.class))
      .orElse(defaultOptions);
  }
}
