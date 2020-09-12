package factionfiction.api.v2.mappers;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.Unit;
import java.math.BigDecimal;
import java.util.UUID;
import org.jdbi.v3.core.mapper.RowMapper;
import factionfiction.api.v2.game.RecoShot;
import factionfiction.api.v2.game.ImmutableRecoShot;
import static java.util.Optional.ofNullable;

public final class RowMappers {

  private RowMappers() {
  }

  public static RowMapper<RecoShot> recoShotMapper() {
    return (rs, st) ->
      ImmutableRecoShot.builder()
        .id(UUID.fromString(rs.getString("id")))
        .minLat(new BigDecimal(rs.getString("latmin")))
        .maxLat(new BigDecimal(rs.getString("latmax")))
        .minLon(new BigDecimal(rs.getString("lonmin")))
        .maxLon(new BigDecimal(rs.getString("lonmax")))
        .build();
  }

  public static RowMapper<FactionUnit> factionUnitMapper() {
    return (rs, st) ->
    (FactionUnit) ImmutableFactionUnit
      .builder()
      .id(ofNullable(rs.getString("id")).map(UUID::fromString))
      .type(Unit.valueOf(rs.getString("type")))
      .location(ImmutableLocation.builder()
        .longitude(new BigDecimal(rs.getString("x")))
        .latitude(new BigDecimal(rs.getString("y")))
        .altitude(new BigDecimal(rs.getString("z")))
        .angle(new BigDecimal(rs.getString("angle")))
        .build())
      .build();
  }

}
