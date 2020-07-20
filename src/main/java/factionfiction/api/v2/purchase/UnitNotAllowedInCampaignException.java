package factionfiction.api.v2.purchase;

import com.github.apilab.rest.exceptions.UnprocessableEntityException;

public class UnitNotAllowedInCampaignException extends UnprocessableEntityException {//NOSONAR

  public UnitNotAllowedInCampaignException() {
    super("Unit is not allowed to be bought in this campaign");
  }

}
