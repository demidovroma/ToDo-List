package modules

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile

class DatabaseModule extends AbstractModule {

  override def configure(): Unit = {
    // Можно оставить пустым или добавить другие привязки
  }

  @Provides
  @Singleton
  def provideDatabaseConfig(): DatabaseConfig[MySQLProfile] = {
    DatabaseConfig.forConfig[MySQLProfile]("slick.dbs.default")
  }
}