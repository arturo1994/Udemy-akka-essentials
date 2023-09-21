package Part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatcher extends App{

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] ${message.toString}")

    }
  }

  val system = ActorSystem("Dispatcher")//, ConfigFactory.load().getConfig("dispatchersDemo"))

  // Method #1 - programmatic/in code
//  val actors = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
//  val r = new Random()
//  for (i <- 1 to 1000){
//    actors(r.nextInt(10)) ! i
//  }

  // Method #2 - from config
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   * Dispatchers implement the ExecutionContext trait
   */

  class DBActor extends Actor with ActorLogging {

    // solution #1
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")

    // solution #2 using routers TODO
    override def receive: Receive = {
      case message => Future {
        // wait on a resource
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }

  val dBActor = system.actorOf(Props[DBActor])
//  dBActor ! "the meaning of life is 42"

  val otherActor = system.actorOf(Props[Counter])

  for (i <- 1 to 1000){
    dBActor ! s"importan message $i"
    otherActor ! s"importan message $i"
  }

}
