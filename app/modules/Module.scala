package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport
import scala.concurrent.ExecutionContext

class Module extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[ExecutionContext])
      .to(classOf[DatabaseExecutionContext])
  }
}
