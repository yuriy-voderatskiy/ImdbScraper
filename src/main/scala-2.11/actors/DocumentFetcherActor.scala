package actors

import actors.DocumentFetcherActor._
import akka.actor._
import akka.pattern._
import dispatch.Defaults._
import dispatch._
import net.ruippeixotog.scalascraper.browser.Browser
import org.jsoup.nodes.Document

import scala.concurrent.Future

class DocumentFetcherActor extends Actor with ActorLogging {
  private val parser = new Browser

  override def receive: Receive = {
    case m@FetchDocumentRequest(url) =>
      log.info(s"[${self.path.name}] Got message " + m)
      val documentFuture = fetchDocument(url)
      documentFuture pipeTo sender()
  }

  private def fetchDocument(documentUrl: String): Future[Either[FetchDocumentResultFailure, FetchDocumentResultSuccess]] = {
    val req: Req = url(documentUrl).GET
    req.addHeader("Accept-Language", "en-US,en;q=0.8")
    val respFuture: dispatch.Future[Res] = Http.configure(_.setFollowRedirect(true))(req)
    val future = respFuture.map {
      case response =>
        Right(FetchDocumentResultSuccess(parseDocument(response)))
    } recover {
      case ex: Exception => Left(FetchDocumentResultFailure("Failed to parse :(", ex))
    }
    future
  }

  private def parseDocument(response: Res): Document = parser.parseString(response.getResponseBody)
}

object DocumentFetcherActor {
  val actorName = "DocumentFetcher"
  val actorProps = Props[DocumentFetcherActor]

  sealed trait Message

  case class FetchDocumentRequest(url: String) extends Message

  case class FetchDocumentResultSuccess(doc: Document) extends Message

  case class FetchDocumentResultFailure(message: String, cause: Exception) extends Message

}
