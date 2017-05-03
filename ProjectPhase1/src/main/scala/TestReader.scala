import TestWriter.{getClass, initialActor}
import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Dan on 4/28/2017.
  */

object TestReader extends App{
  implicit  val system = ActorSystem()
  implicit  val executor = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  type Message = CommittableMessage[Array[Byte],String]
  case object Start
  case object Stop

  val initialActor = classOf[TestReader].getName
  akka.Main.main(Array(initialActor))
}

class TestReader extends Actor with ActorLogging{
  import TestReader._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing logging reader")
      val (control, future) = TestSource.create("loggingReader")(context.system)
        .mapAsync(2)(processMessage)
        // these 4 lines tell Kafka that this consumer has read some data, and mark these data as "already read", so next time, this consumer will not read them again
//        .map(_.committableOffset)
//        .groupedWithin(10, 15 seconds)
//        .map(group => group.foldLeft(CommittableOffsetBatch.empty){(batch, elem) => batch.updated(elem)}) // CommittalbeOffsetBatch will notice the consumer not to read previous read message
//        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
        .run()

      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }

      log.info("Logging reader started")
  }

  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging reader stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }

  private def processMessage (msg: Message): Future[Message] = {
    log.info(s"Consumed number: ${msg.record.value()}")
    Future.successful(msg)
  }
}


