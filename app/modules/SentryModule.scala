package modules

import com.google.inject.AbstractModule
import services.SentryService

class SentryModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SentryService]).asEagerSingleton()
  }
}