package factionfiction.api.v2.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import factionfiction.api.v2.ComponentsModule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.immutables.JdbiImmutables;

public final class InMemoryDB {

  static final String UUID_FUNCTION =
    "CREATE ALIAS UUID_NIL AS $$\n"
    + "@CODE\n"
    + "void run() {\n"
    + "  java.util.UUID.randomUUID();\n"
    + "}\n"
    + "$$;";
  static final String MIGRATION_PATH = "db/changelog.xml";
  static final String DB_URL = "jdbc:h2:mem:dcs-faction-fiction;DB_CLOSE_DELAY=-1";
  static DataSource dataSource;

  private InMemoryDB() {
  }

  public static Jdbi jdbi() {
    var jdbi = Jdbi.create(getDataSource());

    jdbi.getConfig(JdbiImmutables.class)
      .registerImmutable(new ComponentsModule().jdbiImmutables());

    return jdbi;
  }

  private static DataSource getDataSource() {
    if (unitializedDataSource())
      createDataSource();

    return dataSource;
  }

  private static boolean unitializedDataSource() {
    return dataSource == null;
  }

  private static void createDataSource() {
    var hikConf = new HikariConfig();
    hikConf.setJdbcUrl(DB_URL);
    hikConf.setUsername("sa");
    hikConf.setPassword("");
    hikConf.setMaximumPoolSize(2);

    dataSource = new HikariDataSource(hikConf);

    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement st = conn.prepareStatement(UUID_FUNCTION)) {
        st.execute();
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }

    runMigrations();
  }

  private static void runMigrations() {
    try (var liquibase = new Liquibase(MIGRATION_PATH, new ClassLoaderResourceAccessor(),
      DatabaseFactory.getInstance().openDatabase(DB_URL, "sa", "", null, new FileSystemResourceAccessor()))) {
      liquibase.update(new Contexts(), new LabelExpression());
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

}
