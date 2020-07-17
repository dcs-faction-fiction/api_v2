package factionfiction.api.v2.units.purchase;

import com.github.apilab.rest.exceptions.UnprocessableEntityException;

public class UnitNotAllowedInCampaignException extends UnprocessableEntityException {

  public UnitNotAllowedInCampaignException() {
    super("Unit is not allowed to be bought in this campaign");
  }

}
