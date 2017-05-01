import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

Thread.currentThread().getId()

val x = Future {
  println("This is my first future")
  println("Thread ID: " + Thread.currentThread().getId)
}

