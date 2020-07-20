package factionfiction.api.v2.purchase;

import com.github.apilab.rest.exceptions.UnprocessableEntityException;

public class WarehouseItemNotAllowedInCampaignException extends UnprocessableEntityException {//NOSONAR

  public WarehouseItemNotAllowedInCampaignException() {
    super("Warehouse item not allowed in campaign.");
  }

}
