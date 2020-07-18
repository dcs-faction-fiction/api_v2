package factionfiction.api.v2.units.purchase;

import base.game.FactionUnit;
import base.game.warehouse.WarehouseItemCode;
import static base.game.warehouse.WarehouseItemCode.JF_17;
import static factionfiction.api.v2.auth.Roles.CAMPAIGN_MANAGER;
import static factionfiction.api.v2.auth.Roles.FACTION_MANAGER;
import static factionfiction.api.v2.units.UnitHelper.makeSampleFactionUnit;
import io.javalin.Javalin;
import static io.javalin.core.security.SecurityUtil.roles;
import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PurchaseEndpointTest {

  static final String FACTION_NAME = "Faction";
  static final String CAMPAIGN_NAME = "Campaign";
  static final String FACTION_PATHPARAM = "faction";
  static final String CAMPAIGN_PATHPARAM = "campaign";

  Context ctx;
  Javalin javalin;
  PurchaseService service;
  PurchaseEndpoints endpoint;

  @BeforeEach
  public void setup() {
    javalin = mock(Javalin.class);
    ctx = mock(Context.class);
    service = mock(PurchaseService.class);
    endpoint = new PurchaseEndpoints(v -> service);
  }

  @Test
  public void testRegister() {
    endpoint.register(javalin);

    verify(javalin).get(eq("/v2/purchase-api"), any(), eq(roles(CAMPAIGN_MANAGER, FACTION_MANAGER)));
    verify(javalin).post(eq("/v2/purchase-api/campaigns/:campaign/factions/:faction/give-credits"), any(), eq(roles(CAMPAIGN_MANAGER)));
    javalin.post(eq("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-unit"), any(), eq(roles(FACTION_MANAGER)));
    javalin.post(eq("/v2/purchase-api/campaigns/:campaign/factions/:faction/buy-warehouse-item"), any(), eq(roles(FACTION_MANAGER)));
  }

  @Test
  public void testVersion() throws Exception {
    endpoint.handle(ctx);

    verify(ctx).json(Map.of("version", "2"));
  }

  @Test
  public void testGiveCredits() throws Exception {
    var credits = new BigDecimal(0.2);
    given(ctx.pathParam(CAMPAIGN_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, CAMPAIGN_NAME));
    given(ctx.pathParam(FACTION_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, FACTION_NAME));
    given(ctx.bodyAsClass(BigDecimal.class))
      .willReturn(credits);
    endpoint.giveCredits(ctx);

    verify(service).giveCredits(CAMPAIGN_NAME, FACTION_NAME, credits);
  }

  @Test
  public void testBuyUnit() throws Exception {
    var unit = makeSampleFactionUnit();
    given(ctx.pathParam(CAMPAIGN_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, CAMPAIGN_NAME));
    given(ctx.pathParam(FACTION_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, FACTION_NAME));
    given(ctx.bodyAsClass(FactionUnit.class))
      .willReturn(unit);
    endpoint.buyUnit(ctx);

    verify(service).buyUnit(CAMPAIGN_NAME, FACTION_NAME, unit);
  }

  @Test
  public void testBuyWarehouseItem() throws Exception {
    var code = JF_17;
    given(ctx.pathParam(CAMPAIGN_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, CAMPAIGN_NAME));
    given(ctx.pathParam(FACTION_PATHPARAM, String.class))
      .willReturn(Validator.create(String.class, FACTION_NAME));
    given(ctx.bodyAsClass(WarehouseItemCode.class))
      .willReturn(code);
    endpoint.buyWarehouseItem(ctx);

    verify(service).buyWarehouseItem(CAMPAIGN_NAME, FACTION_NAME, code);
  }
}
