/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factionfiction.api.v2;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import static io.javalin.http.staticfiles.Location.EXTERNAL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Raffaele Ragni
 */
public class StaticFilesTest {
  Javalin javalin;
  StaticFiles files;

  @BeforeEach
  public void setup() {
    javalin = mock(Javalin.class);
    javalin.config = mock(JavalinConfig.class);
    files = new StaticFiles(javalin);
  }

  @Test
  public void testVarIsOn() {
    System.setProperty("API_STATIC_FOLDER", ".");

    files.start();

    verify(javalin.config).addStaticFiles(".", EXTERNAL);
  }

  @Test
  public void testVarIsOff() {
    System.setProperty("API_STATIC_FOLDER", "");

    files.start();

    verify(javalin.config, times(0)).addStaticFiles(any(), any());
  }
}
