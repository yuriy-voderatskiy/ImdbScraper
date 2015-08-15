import java.sql.Timestamp
import java.util.UUID

import models._
import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.joda.time.{Minutes, DateTime, Duration}
import org.jsoup.nodes.Document

object ExtractorApp extends App {
  val browser: Browser = new Browser
  val doc: Document = browser.parseFile("./src/main/resources/tt2381249.html")

  val name = doc >> extractor("span[itemprop=name]", text)

  val maybeDatePublished: Option[Timestamp] = for {
    timestamp <- doc >?> extractor("meta[itemprop=datePublished]", attr("content"))
    dateTime = new DateTime(timestamp)
  } yield new Timestamp(dateTime.getMillis)

  val maybeContentRating = doc >?> extractor("div.infobar > [itemprop=contentRating]", attr("content"))

  val maybeDuration = for {
    text <- doc >?> extractor("div.infobar > time[itemprop=duration]", text)
    minutes = text.filter(_.isDigit).toInt
  } yield Minutes.minutes(minutes).toStandardDuration

  val maybeGenres = (doc >?> extractor("span[itemprop=genre]", texts)).map(genres => genres.map(Genre(_)))

  val maybeDirector = for {
    name <- doc >?> extractor("[itemprop=director] > a > span", text)
  } yield Director(name)

  val maybeCreator = for {
    name <- doc >?> extractor("[itemprop=creator] > a > span", text)
  } yield Creator(name)

  val maybeActors = (doc >?> extractor("[itemprop=actors] > a > span", texts)).map(actors => actors.map(Actor(_)))

  val movie = Movie(
    name,
    id = Some(UUID.randomUUID()),
    contentRating = maybeContentRating,
    datePublished = maybeDatePublished,
    duration = maybeDuration,
    genres = maybeGenres,
    creator = maybeCreator,
    actors = maybeActors,
    director = maybeDirector
  )

  println(movie)
}
