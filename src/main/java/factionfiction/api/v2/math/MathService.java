package factionfiction.api.v2.math;

import base.game.Location;
import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

/**
 * This is a static service
 */
public final class MathService {

  private MathService() {
  }

  /**
   * Finds if a point is outside a circle and returns the new point shrank to the edge
   * of the circle if it was outside the circle, otherwise the center.
   * @param center center of the circle in lat/lon
   * @param sizeFt radius in feet
   * @param point point to check
   * @return new point returned or center if was outside
   */
  public static Location shrinkToCircle(Location center, int sizeFt, Location point) {
    var dist = distance(
      center.latitude().doubleValue(),
      point.latitude().doubleValue(),
      center.longitude().doubleValue(),
      point.longitude().doubleValue());
    var distFt = dist * 3.28084;

    if (distFt > sizeFt) {
      return center;
    } else {
      return point;
    }
  }

  /**
   * @param lat1
   * @param lat2
   * @param lon1
   * @param lon2
   * @return
   */
  public static double distance(double lat1, double lat2, double lon1, double lon2) {

    final int R = 6371; // Radius of the earth

    double latDistance = toRadians(lat2 - lat1);
    double lonDistance = toRadians(lon2 - lon1);
    double sqlat = pow(sin(latDistance / 2), 2);
    double sqlon = pow(sin(lonDistance / 2), 2);
    double coslat1 = Math.cos(Math.toRadians(lat1));
    double coslat2 = Math.cos(Math.toRadians(lat2));
    double a = sqlat + coslat1 * coslat2 * sqlon;
    double c = 2 * atan2(sqrt(a), sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    distance = pow(distance, 2);
    return sqrt(distance);
  }

}
