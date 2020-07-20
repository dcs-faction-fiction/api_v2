package factionfiction.api.v2.purchase;

import com.github.apilab.rest.exceptions.ServerException;

public class ZoneAtMinumum extends ServerException {

  public ZoneAtMinumum() {
    super(422, "zone at minumum");
  }

}
