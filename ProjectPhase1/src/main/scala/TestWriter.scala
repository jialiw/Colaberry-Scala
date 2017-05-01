/**
  * Created by Dan on 4/28/2017.
  */
import java.io.{BufferedReader, FileReader}

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.Promise
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable}
import akka.event.Logging
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global

object TestWriter extends App{
  implicit  val system = ActorSystem()
  implicit  val executor = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  case object Read
  case object Stop

  val initialActor = classOf[TestWriter].getName
  akka.Main.main(Array(initialActor))
}


class TestWriter extends Actor with ActorLogging{

  import TestWriter._
  val filename = "C:\\Users\\Dan\\Desktop\\1000-genomes Fother Fsample_info Fsample_info.csv"

  override def preStart(): Unit = {
    super.preStart()
    self ! Read
  }

  override def receive: Receive = {
    case Read =>
      val producerSettings
          = ProducerSettings(context.system, new ByteArraySerializer,new StringSerializer)
          .withBootstrapServers("192.168.99.100:9092")
//        val producerSettings
//          = ProducerSettings(context.system, new ByteArraySerializer,new StringSerializer)
//          .withBootstrapServers(config.getString("kafka.producer.interface") + ":" + config.getString("kafka.producer.port") )

      // sink is receive destination
      // flow is the pipeline between sink and source, there are input flow and output flow, flow controls the pipe speed
      // once the sink has too much stuff to process, flow will back pressure on source and ask source to slow down
      // source is the source file
      val kafkaSink = Producer.plainSink(producerSettings)

//      // Synchronous version
//      // in the unfoldResource there are 3 status: create, read, close
//      val p = Source.unfoldResource[String, BufferedReader](
//        () => new BufferedReader(new FileReader(filename)),
//        reader => Option(reader.readLine()),
//        reader => reader.close()
//      )

        // Asynchronous version
        val p = Source.unfoldResourceAsync[String, BufferedReader](
              () => Promise.successful(new BufferedReader(new FileReader(filename))).future, // create
              reader => Promise.successful(Option(reader.readLine())).future, // read
              reader => {
                reader.close()
                self ! Stop
                Promise.successful(Done).future
              }// close
            )

      log.info("Start running ...")

      val (control, future) = p
      .map(new ProducerRecord[Array[Byte],String](TestWriterTopic.Topic, _))
      .toMat(kafkaSink)(Keep.both) // connect source to Sink: toMat
      .run() // map is going to send what read from file to kafka (batch based). it is a streaming method, so map will not wait till the entire file read, once one batch is ready, it will send to Kafka
      // also map can control the size of one batch (1 line or n lines for one batch) by using .groupedWithin methods (this method can be time/message based)
      // or .mapAsync(n)(message) method [n parallelism] --> then there will be n (akka) actors/writers : each actor will have one mailbox --> so there will be n mailbox under ONE KAFKA TOPIC

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
//      context.become(running(control))
      log.info(s"Writer now running, writing file to topic ${TestWriterTopic.Topic}")
  }

  def running(control: Cancellable): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      control.cancel()
      context.stop(self)
  }
}


