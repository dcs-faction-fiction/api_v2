package factionfiction.api.v2.units;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.Unit;
import com.github.apilab.rest.exceptions.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;

public class UnitRepository {

  public static final String TABLE_NAME = "campaign_faction_units";

  final Jdbi jdbi;

  public UnitRepository(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public List<FactionUnit> getUnitsFromCampaignFaction(UUID owner) {
    return jdbi.withHandle(h -> h.select(
      "select"
        + " id, campaign_faction_id, type, x, y, z, angle"
        + " from campaign_faction_units"
        + " where campaign_faction_id = ?", owner)
      .map(unitMapper())
      .list()
    );
  }

  public FactionUnit getUnit(UUID id) {
    return jdbi.withHandle(h -> h.select(
      "select"
        + " id, campaign_faction_id, type, x, y, z, angle"
        + " from campaign_faction_units"
        + " where id = ?", id)
      .map(unitMapper())
      .findFirst()
      .orElseThrow(() -> new NotFoundException("Unit not found"))
    );
  }

  static RowMapper<FactionUnit> unitMapper() {
    return (rs, st) ->
      (FactionUnit) ImmutableFactionUnit.builder()
        .id(UUID.fromString(rs.getString("id")))
        .type(Unit.valueOf(rs.getString("type")))
        .location(ImmutableLocation.builder()
          .latitude(rs.getBigDecimal("y"))
          .longitude(rs.getBigDecimal("x"))
          .altitude(rs.getBigDecimal("z"))
          .angle(rs.getBigDecimal("angle"))
          .build())
        .build();
  }

}
