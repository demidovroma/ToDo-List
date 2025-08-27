package modules

import anorm._
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import javax.inject._
import java.sql.{Connection, DriverManager}

@Singleton
class DatabaseModule @Inject()(config: Configuration) {

  private val user: String = sys.env.getOrElse("DB_USER", "sysdba")
  private val password: String = sys.env.getOrElse("DB_PASS", "masterkey")

  private val projectDir = System.getProperty("user.dir")
  private val dbPath    = s"$projectDir/data/DATABASE.FDB"
  private val url       = s"jdbc:firebirdsql://localhost:3050/$dbPath"

  // Подключение к БД
  def withConnection[A](block: Connection => A): A = {
    Class.forName("org.firebirdsql.jdbc.FBDriver")
    val conn = DriverManager.getConnection(url, user, password)
    try {
      block(conn)
    } finally {
      conn.close()
    }
  }
}