package Part4FaultTolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

object ActorLifeCycle extends App{

  case object StartChild
  class LifeCycle extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I am starting")

    override def postStop(): Unit = log.info("I have stopped")
    override def receive: Receive = {
      case StartChild => context.actorOf(Props[LifeCycle], "child")
    }
  }

  val system = ActorSystem("ActorLifeCycle")
//  val parent = system.actorOf(Props[LifeCycle], "parent")
//  parent ! StartChild
//  parent ! PoisonPill




  object Fail
  object FailChild
  object Check
  object CheckChild

  class Parent extends Actor{
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild =>  child ! Check
    }

  }

  class Child extends Actor with ActorLogging{
    override def preStart(): Unit = {
      log.info("supervisedChild starting")
    }

    override def postStop(): Unit = {
      log.info("supervisedChild stopped")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"supervisedChild actor restarting because of ${reason.getMessage}")
    }

    override def postRestart(reason: Throwable): Unit = {
      log.info(s"supervisedChild actor restarded")
    }

    override def receive: Receive = {
      case Fail =>
        log.info("child will fail now")
        throw new RuntimeException("child fail")
      case Check => log.info("all is good")
    }

  }

  val supervisor = system.actorOf(Props[Parent], "parent")
  supervisor ! FailChild
  supervisor ! CheckChild

}
