package services

import javax.inject._
import play.api._
import io.sentry.{Sentry, SentryOptions}

@Singleton
class SentryService @Inject()(
    environment: Environment,
    config: Configuration
  ) {
  private val logger = Logger(this.getClass)

  private def initSentry(): Unit = {
    val sentryDsn = config.getOptional[String]("sentry.dsn")
    val environment = config.getOptional[String]("sentry.environment")

    sentryDsn.foreach { dsn =>
      logger.info(s"Инициализация Sentry с DSN: $dsn")
      Sentry.init((options: SentryOptions) => {
        options.setDsn(dsn)
        environment.foreach(env => options.setEnvironment(env))
        options.setTracesSampleRate(1.0)
        logger.info("Sentry успешно инициализирован")
      })
    }
  }

  initSentry()
}
