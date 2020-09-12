package factionfiction.api.v2.mappers;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.Unit;
import java.math.BigDecimal;
import java.util.UUID;
import org.jdbi.v3.core.mapper.RowMapper;

public final class RowMappers {

  private RowMappers() {
  }

  public static RowMapper<FactionUnit> factionUnitMapper() {
    return (rs, st) ->
    (FactionUnit) ImmutableFactionUnit
      .builder()
      .id(UUID.fromString(rs.getString(1)))
      .type(Unit.valueOf(rs.getString(2)))
      .location(ImmutableLocation.builder()
        .longitude(new BigDecimal(rs.getString(3)))
        .latitude(new BigDecimal(rs.getString(4)))
        .altitude(new BigDecimal(rs.getString(5)))
        .angle(new BigDecimal(rs.getString(6)))
        .build())
      .build();
  }

}
