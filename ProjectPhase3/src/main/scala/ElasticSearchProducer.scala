import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.typesafe.config.ConfigFactory
import akka.util.ByteString
import akka.http.javadsl.model._
import akka.http.javadsl.model.headers._
import java.util.Optional

import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpMethods._

/**
  * Created by Dan on 5/3/2017.
  */

object ElasticSearchProducer extends App {
  implicit  val system = ActorSystem()
  implicit  val executor = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop

  val initialActor = classOf[ElasticSearchProducer].getName
  akka.Main.main(Array(initialActor))
}

class ElasticSearchProducer extends Actor with ActorLogging{
  import ElasticSearchProducer._


//  val headerString = new BufferedReader(new FileReader("inputFile.filename")).readLine();
  val headerString = scala.io.Source.fromFile(config.getString("inputFile.filename")).getLines().next()
  val headerList = CSVParser.parse(headerString,'\\', ',' , '"')

  override def preStart(): Unit ={
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing ElasticSearch producer ...")

      val done = ElasticSearchSource.create("logging Consumer")(context.system)
        .mapAsync(2)(processMessage)
        .via(connectionFlow).runWith(Sink.ignore)
//        .toMat(Sink.ignore)(Keep.both)
//        .run()

  }

  private def sendtoES (jsonString: String, id:String): HttpRequest = {
    val request = HttpRequest(
      POST,
      uri = config.getString("elastic.uri") + "/" + id,
      entity = HttpEntity(ContentTypes.`application/json`,ByteString(jsonString))
    )

    request
  }

  private def processMessage(msg:Message): Future[HttpRequest] = {
//    log.info(s"Consumed number: ${msg.record.value()}. " + " And begin to process ...")
    val valueList = CSVParser.parse(msg.record.value(), '\\', ',', '"').get
    val id = valueList(0).toString.replace("\"","")

    val combineList = headerList.get.zip(valueList.toList)
//    println(combineList)
    val combineListFormatted = combineList.map(item => "\"" + item._1.toString().replace("\"", "") + "\":\"" + JsonValueFormatter(item._2.toString()) + "\"")
    val jsonString = "{ " + combineListFormatted.mkString(",") + "}"
//    println(jsonString)
    Future.successful(sendtoES(jsonString, id))
  }

  private def JsonValueFormatter(str: String ): String = {
    if (str.indexOf("\"").equals(-1)) {
      if (str.length() == 0) ""
      else str
    } else {
      "\"" + str.replace("\"", "")
    }
  }

  private def connectionFlow:Flow[HttpRequest, HttpResponse, Any] = {
    //Connecting to the elastic search, hostname and port refers to elastic search
    Http().outgoingConnection(config.getString("http.interface"), config.getInt("http.port"))
  }
}
