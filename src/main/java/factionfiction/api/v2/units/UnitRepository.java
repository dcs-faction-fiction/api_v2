package factionfiction.api.v2.units;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.Unit;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

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
      .map((rs, st) ->
        (FactionUnit) ImmutableFactionUnit.builder()
          .id(UUID.fromString(rs.getString("id")))
          .type(Unit.valueOf(rs.getString("type")))
          .location(ImmutableLocation.builder()
            .latitude(rs.getBigDecimal("y"))
            .longitude(rs.getBigDecimal("x"))
            .altitude(rs.getBigDecimal("z"))
            .angle(rs.getBigDecimal("angle"))
            .build())
          .build()
      )
      .list()
    );
  }

}
