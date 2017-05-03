/**
  * Created by Dan on 5/1/2017.
  */
import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.{ActorMaterializer}
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps

object MessageProcesser extends  App {
  implicit  val system = ActorSystem()
  implicit  val executor = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop

  val initialActor = classOf[MessageProcesser].getName
  akka.Main.main(Array(initialActor))
}

class MessageProcesser extends Actor with ActorLogging {
  import MessageProcesser._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing message processor...")
      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("192.168.99.100:9092")
      val kafkaSink = Producer.plainSink(producerSettings)

      val (control, future) = MessageProcessorSource.create("MessageProcessor")(context.system) // Within Source, we have aldready get the message back
        .mapAsync(2)(processMessage)
//        .map(_.committableOffset)
//        .groupedWithin(15, 10 seconds)
//        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
//        .mapAsync(1)(_.commitScaladsl())
        .map(new ProducerRecord[Array[Byte],String](MessageTopic2.Topic, _))
        .toMat(kafkaSink)(Keep.both)
        .run()

      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
    log.info("Logging consumer started...")
  }

  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }
//    private def processMessage(msg:Message): Message = {
//      log.info(s"Consumed number: ${msg.record.value()}. " + " And begin to process ...")
//      //Future.successful(msg)
//      msg
//    }

  private def processMessage(msg:Message): Future[String] = {
    log.info(s"Consumed number: ${msg.record.value()}. " + " And begin to process ...")
    Future.successful(msg.record.value())
  }
}
