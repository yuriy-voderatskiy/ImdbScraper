package models

import java.net.URL
import java.sql.Timestamp
import java.util.UUID

import models.Gender.Gender
import org.joda.time.Duration

trait Identifier[T] extends Serializable {
  val id: Option[T]
}

trait IMDBIdentifier extends Serializable {
  def imdbId: Option[String]
  def imdbUrl: Option[URL]
}

trait MovieIMDBIdentifier extends IMDBIdentifier {
  override def imdbUrl : Option[URL] = imdbId.map(id => new URL(s"http://www.imdb.com/title/$id"))
}

trait PersonIMDBIdentifier extends IMDBIdentifier {
  override def imdbUrl: Option[URL] = imdbId.map(id => new URL(s"http://www.imdb.com/name/$id"))
}

object Gender extends Enumeration {
  type Gender = Value
  val Male, Female = Value
}

abstract class Person extends Identifier[UUID] {
  def name: String

  def gender: Option[Gender] = None

  def birthDate: Option[Timestamp]
}

case class Actor(
  name: String,
  id: Option[UUID] = None,
  override val gender: Option[Gender] = None,
  birthDate: Option[Timestamp] = None,
  imdbId: Option[String] = None) extends Person with PersonIMDBIdentifier

object Actor {
  def apply(id: UUID, name: String): Actor = Actor(name, id=Some(id))
}

case class Director(
  name: String,
  id: Option[UUID] = None,
  override val gender: Option[Gender] = None,
  birthDate: Option[Timestamp] = None,
  imdbId: Option[String] = None) extends Person with PersonIMDBIdentifier

object Director {
  def apply(id: UUID, name: String): Director = Director(name, id = Some(id))
}

case class Creator(
  name: String,
  id: Option[UUID] = None,
  override val gender: Option[Gender] = None,
  birthDate: Option[Timestamp] = None,
  imdbId: Option[String] = None) extends Person with PersonIMDBIdentifier

object Creator {
  def apply(id: UUID, name: String): Creator = Creator(name, id = Some(id))
}

case class Genre(name: String, id: Option[UUID] = None) extends Identifier[UUID]

case class Movie(
  name: String,
  id: Option[UUID] = None,
  datePublished: Option[Timestamp] = None,
  contentRating: Option[String] = None,
  duration: Option[Duration] = None,
  genres: Option[Seq[Genre]] = None,
  director: Option[Person] = None,
  creator: Option[Person] = None,
  actors: Option[Seq[Actor]] = None,
  imdbId: Option[String] = None) extends Identifier[UUID] with MovieIMDBIdentifier

object Movie {
  def apply(id: UUID, name: String): Movie = Movie(name, id=Some(id))

  def apply(id: UUID, name: String, imdbId: String): Movie = Movie(name, id = Some(id), imdbId = Some(imdbId))
}
