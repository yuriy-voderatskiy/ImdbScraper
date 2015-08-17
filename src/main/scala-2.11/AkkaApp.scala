import akka.actor.ActorSystem
import akka.pattern._
import akka.routing.RoundRobinPool
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object AkkaApp extends App {

  import actors.DocumentFetcherActor._

  implicit val timeout = Timeout(5 seconds)

  val logger = LoggerFactory.getLogger(this.getClass)

  val system = ActorSystem("Fetcher")

  val fetcher = system.actorOf(actorProps.withRouter(RoundRobinPool(
    nrOfInstances = 2
  )), actorName)

  val movies = Seq(
    "http://www.imdb.com/title/tt0111161",
    "http://www.imdb.com/title/tt0137523",
    "http://www.imdb.com/title/tt0133093"
  )

  val requests = movies.map(FetchDocumentRequest)

  val futures = requests.map {
    r => (fetcher ? r).mapTo[Either[FetchDocumentResultFailure, FetchDocumentResultSuccess]]
  }

  val results = Future.sequence(futures)

  results.onSuccess {
    case responses =>
      responses.foreach {
        case Left(failure) => println(failure.message)
        case Right(success) =>
          println(Extractor.extractMovie(success.doc))
      }
  }

  system.awaitTermination()
}
