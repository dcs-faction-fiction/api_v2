package factionfiction.api.v2;

import com.github.apilab.core.ApplicationLifecycle;

public final class Main {

  static ApplicationLifecycle app;
  static {
    System.setProperty("API_ENABLE_ENDPOINTS", "true");
    System.setProperty("API_ENABLE_MIGRATION", "true");
    app = DaggerApplicationComponent.create().instance();
  }

  private Main() {
  }

  public static void main(String[] args) {
    app.start();
  }

  public static void stop() {
    app.stop();
  }
}
