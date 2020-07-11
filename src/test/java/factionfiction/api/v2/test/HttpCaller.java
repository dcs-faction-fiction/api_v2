package factionfiction.api.v2.test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.io.IOException;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpCaller {
  public static final String TEST_URL = "http://localhost:8080";
  public static final String TEST_SECRET = "test";
  public static final String[] ROLES = new String[]{"admin", "daemon", "campaign_manager", "faction_manager"};
  public static final String SAME_USER = UUID.randomUUID().toString();

  public boolean loggedIn = true;
  public boolean withUUID = true;
  public boolean sameUUID = true;
  public OkHttpClient client = new OkHttpClient();

  public String get(String url) {
    return call(url, "GET", null);
  }

  public String post(String url, String body) {
    return call(url, "POST", body);
  }

  public String call(String url, String method, String body) {
    var request = createRequest(url);
    request = addBody(body, request, method);
    request = addAuthentication(request);

    try {
      var response = client.newCall(request.build()).execute();
      checkErrors(response);
      var responseBody = response.body();
      return responseBody == null ? "" : responseBody.string();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  void checkErrors(Response response) throws RuntimeException {
    if (response.code() >= 400)
      throw new RuntimeException(String.valueOf(response.code()));
  }

  private Request.Builder addAuthentication(Request.Builder request) throws JWTCreationException, IllegalArgumentException {
    if (loggedIn) {
      var user = SAME_USER;
      if (!sameUUID)
        user = UUID.randomUUID().toString();
      var alg = Algorithm.HMAC256(TEST_SECRET);
      var token = JWT.create()
        .withArrayClaim("roles", ROLES)
        .withSubject(withUUID ? user : null)
        .sign(alg);
      request = request.header("Authorization", "Bearer " + token);
    }
    return request;
  }

  private Request.Builder createRequest(String url) {
    var request = new Request.Builder()
      .url(TEST_URL + url);
    return request;
  }

  private Request.Builder addBody(String body, Request.Builder request, String method) {
    if (body != null)
      request = request.method(method, RequestBody.create(
        MediaType.get("application/json"),
        body
      ));
    return request;
  }

}
