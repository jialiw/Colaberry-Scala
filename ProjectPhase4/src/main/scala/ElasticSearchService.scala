import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.ExecutionContext.Implicits.global
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future


/**
  * Created by Dan on 5/4/2017.
  */
object ElasticSearchService extends App with DefaultJsonProtocol {

  case class search()

  case class searchInfo(a: String, b: String)

  val config = ConfigFactory.load()

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val ipApiConnectionFlow: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection(config.getString("http.link"), config.getInt("http.port"))

  def fetchFromElastic(keyword: String): Future[Either[String, String]] = {
    Source.single(buildRequest(keyword)).via(ipApiConnectionFlow).runWith(Sink.head)
      .flatMap {
        response =>
          response.status match {
            case OK => Future.successful(Right(response.entity.toString()))
            case BadRequest => Future.failed(new Exception("Bad request"))
            case NotFound => Future.successful(Right("ID not found"))
            case _ => {
              println(response.status)
              Future.failed(new Exception("Invalid"))
            }
          }
      }
  }

  def buildRequest(keyword: String): HttpRequest = {
    println("Searching " + keyword + "...")
    val request = RequestBuilding.Get(config.getString("elastic.uri") + "/" + keyword)
    println(request)
    request
  }

  val routes = {
    pathPrefix("colaberry" / "samples" ) {
      (get & path ( Segment )) { keyword =>
        complete { // complete means the following code will be executed once the path has been checked
          // Either has the similar function as Option, it will return either left parameter or right parameter
          // by default, left parameter contains error message, while right parameter contains the REAL VALUE
          val res: Future[Either[String, String]] = fetchFromElastic(keyword)
          res.map[String] {
            case Right(s) => s
            case Left(errorMessage) => errorMessage
          }
        }
      }
    }
  }
  /**
    * Http() starts the http service
    * bindAndHandle method is used to listen the request
    * once the request is coming, it will call 'routes' to check the path and extract the keyword
    */
  Http().bindAndHandle(routes, config.getString("local.link"), config.getInt("local.port"))
}
