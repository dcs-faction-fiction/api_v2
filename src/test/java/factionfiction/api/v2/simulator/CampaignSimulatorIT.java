package factionfiction.api.v2.simulator;

import com.jayway.jsonpath.JsonPath;
import factionfiction.api.v2.Main;
import factionfiction.api.v2.test.HttpCaller;
import java.math.BigDecimal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class CampaignSimulatorIT {

  static HttpCaller http = new HttpCaller();

  static String factionName = "New faction"+System.currentTimeMillis();
  static String campaignName = "New campaign"+System.currentTimeMillis();

  @BeforeAll
  public static void startup() {
    Main.main(new String[]{});
  }

  @AfterAll
  public static void teardown() {
    Main.stop();
  }

  @Test
  public void testHealthChecks() throws JSONException {
    var result = http.get("/status/health");
    JSONAssert.assertEquals("{\"database\": true, \"javalin\": true}", result, false);

    result = http.get("/v2/faction-api");
    JSONAssert.assertEquals("{\"version\": \"2\"}", result, false);

    result = http.get("/v2/common-api");
    JSONAssert.assertEquals("{\"version\": \"2\"}", result, false);
  }

  @Test
  public void testDefaultGameOptions() throws JSONException {
    var result = http.get("/v2/common-api/default-game-options");

    BigDecimal credits = new BigDecimal(JsonPath.read(result, "$.credits.starting").toString());
    assertThat(credits, is(new BigDecimal(30)));
  }
}
