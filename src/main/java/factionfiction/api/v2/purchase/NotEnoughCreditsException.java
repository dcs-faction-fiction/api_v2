package factionfiction.api.v2.purchase;

import com.github.apilab.rest.exceptions.UnprocessableEntityException;

public class NotEnoughCreditsException extends UnprocessableEntityException {//NOSONAR

  public NotEnoughCreditsException() {
    super("Not enough credits");
  }

}
