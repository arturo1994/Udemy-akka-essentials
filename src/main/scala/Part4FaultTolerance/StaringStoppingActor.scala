package Part4FaultTolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}

object StaringStoppingActor extends App
{

  val system = ActorSystem("StaringStoppingActor")

  object Parents {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }
  class Parents extends Actor with ActorLogging {
    import Parents._
    override def receive: Receive = withChildren(Map())

    def withChildren(Children: Map[String, ActorRef]): Receive = {

      case StartChild(name) =>
        log.info(s"Start child $name")
        context.become(withChildren(Children + (name -> context.actorOf(Props[Child], name))))

      case StopChild(name) =>
        log.info(s"Stopping child with the name $name")
        val childOption = Children.get(name)
        childOption.foreach(childRef => context.stop(childRef))

      case Stop =>
        log.info("Stopping myself")
        context.stop(self)

      case message => log.info(message.toString)

    }

  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /*
  Method #1, using context.stop
   */
//
  import Parents._
//
  val parent = system.actorOf(Props[Parents], "parent")
  parent ! StartChild("child1")

  val child1 = system.actorSelection("/user/parent/child1")

  child1 ! "Hi kid!"

  parent ! StopChild("child1")

//  for (_ <- 1 to 50 ) child1 ! "Are you still there"

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "Hi, second child"

  parent ! Stop
  for (_ <- 1 to 10 ) parent ! "parent, Are you still there"
  for (i <- 1 to 100 ) child2 ! s"[$i] child 2, Are you still there"

  /*
  Method #2 using special messages
   */

  val looserActor = system.actorOf(Props[Child])
  looserActor ! "Hi, looser child"
  looserActor ! PoisonPill
  looserActor ! s"looser child, Are you still there"

  val abruptlyTerminatorActor = system.actorOf(Props[Child])
  abruptlyTerminatorActor ! "Hi, abruptlyTerminatorActor child"
  abruptlyTerminatorActor ! "you are about to be terminated"
  abruptlyTerminatorActor ! Kill
  abruptlyTerminatorActor ! s"abruptlyTerminatorActor child, Are you still there"

  /*
  Dead Watch
   */

  class Watched extends Actor with ActorLogging{
    import Parents._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Starting and watching child $name")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"the reference that I'm watching $ref has been stopped")
    }
  }

  val watched = system.actorOf(Props[Watched], "watched")
  watched ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watched/watchedChild")

  Thread.sleep(500)
  watchedChild ! PoisonPill


}
