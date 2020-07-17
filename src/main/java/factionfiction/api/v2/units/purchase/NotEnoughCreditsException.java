package factionfiction.api.v2.units.purchase;

import com.github.apilab.rest.exceptions.UnprocessableEntityException;

public class NotEnoughCreditsException extends UnprocessableEntityException {

  public NotEnoughCreditsException() {
    super("Not enough credits");
  }

}
