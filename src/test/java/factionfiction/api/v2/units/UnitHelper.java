package factionfiction.api.v2.units;

import base.game.FactionUnit;
import base.game.ImmutableFactionUnit;
import base.game.ImmutableLocation;
import base.game.units.Unit;
import static base.game.units.Unit.T_80;
import factionfiction.api.v2.game.ImmutableRecoShot;
import factionfiction.api.v2.game.RecoShot;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;

public final class UnitHelper {

  public UnitHelper() {
  }

  public static void cleanUnitTable(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table "
      + UnitRepository.TABLE_NAME));
  }

  public static void cleanRecoShots(Jdbi jdbi) {
    jdbi.useHandle(h -> h.execute("truncate table recoshot_item"));
    jdbi.useHandle(h -> h.execute("truncate table recoshot"));
  }

  public static void insertRecoShot(Jdbi jdbi, UUID id, UUID cfId) {
    jdbi.useHandle(h -> h.execute(
      "insert into recoshot"
        + " (id, campaign_faction_id, latmin, latmax, lonmin, lonmax)"
        + " values(?, ?, ?, ?, ?, ?)",
      id,
      cfId,
      1,
      2,
      1,
      2
    ));
    jdbi.useHandle(h -> h.execute(
      "insert into recoshot_item"
        + " (id, recoshot_id, type, x, y, z, angle)"
        + " values(?, ?, ?, ?, ?, ?, ?)",
      UUID.randomUUID(),
      id,
      Unit.T_80,
      new BigDecimal(1.5),
      new BigDecimal(1.5),
      new BigDecimal(1.5),
      new BigDecimal(1.5)
    ));
  }

  public static void insertSampleFactionUnit(Jdbi jdbi, UUID id, UUID cfId) {
    jdbi.useHandle(h -> h.execute(
      "insert into campaign_faction_units"
        + " (id, campaign_faction_id, type, x, y, z, angle)"
        + " values(?, ?, ?, ?, ?, ?, ?)",
      id,
      cfId,
      Unit.T_80,
      new BigDecimal(2),
      new BigDecimal(1),
      new BigDecimal(3),
      new BigDecimal(4)
    ));
  }

  public static RecoShot makeSampleRecoShot(UUID id) {
    return ImmutableRecoShot.builder()
      .id(id)
      .minLat(new BigDecimal(1))
      .maxLat(new BigDecimal(2))
      .minLon(new BigDecimal(1))
      .maxLon(new BigDecimal(2))
      .units(List.of(ImmutableFactionUnit.builder()
        .type(T_80)
        .location(ImmutableLocation.builder()
          .latitude(new BigDecimal(1.5))
          .longitude(new BigDecimal(1.5))
          .altitude(new BigDecimal(1.5))
          .angle(new BigDecimal(1.5))
          .build())
        .build()))
    .build();
  }

  public static FactionUnit makeSampleFactionUnit(UUID id) {
    return ImmutableFactionUnit.builder()
      .id(id)
      .type(Unit.T_80)
      .location(ImmutableLocation.builder()
        .latitude(new BigDecimal(1))
        .longitude(new BigDecimal(2))
        .altitude(new BigDecimal(3))
        .angle(new BigDecimal(4))
        .build())
      .build();
  }

  public static FactionUnit makeSampleFactionUnit() {
    return ImmutableFactionUnit.builder()
      .type(Unit.T_80)
      .location(ImmutableLocation.builder()
        .latitude(new BigDecimal(1))
        .longitude(new BigDecimal(2))
        .altitude(new BigDecimal(3))
        .angle(new BigDecimal(4))
        .build())
      .build();
  }
}
