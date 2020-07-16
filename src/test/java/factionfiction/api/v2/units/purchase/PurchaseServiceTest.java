package factionfiction.api.v2.units.purchase;

import static java.math.BigDecimal.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class PurchaseServiceTest {

  String campaignName;
  String factionName;
  PurchaseServiceImpl service;
  PurchaseRepository repository;

  @BeforeEach
  public void setup() {
    campaignName = "Campaign";
    factionName = "Faction";
    repository = mock(PurchaseRepository.class);
    service = new PurchaseServiceImpl(repository);
  }

  @Test
  public void testGiveCredits() {
    given(repository.giveCredits(campaignName, factionName, ONE))
      .willReturn(ONE);

    var result = service.giveCredits(
      campaignName,
      factionName,
      ONE);

    assertThat(result, is(ONE));
  }
}
