package Part4FaultTolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem,AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SupervisionSpec extends TestKit(ActorSystem("SupervisionSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{
  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import SupervisionSpec._

  "A supervisor" should {
    "resume its child in case of a minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! "I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka I love Akka"
      child ! Report
      expectMsg(3)

    }

    "restar its child in case of an empty sentence" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)
    }

    "terminated its child in case of a mayor error" in {

      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)

      child ! "i love Akka"

      val terminatedMessage = expectMsgType[Terminated]

      assert(terminatedMessage.actor == child)

    }

    "escalate an error when it dosent know what to do" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)

      child ! 43

      val terminatedMessage = expectMsgType[Terminated]

      assert(terminatedMessage.actor == child)
    }

  }

  "A kinder Supervisor" should {
    "not kill children in case it's restarted or escalate in case of failure" in {
      val supervisor = system.actorOf(Props[NoDeathOnRestartSupervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! 45
      child ! Report
      expectMsg(0 )


    }
  }

  "An all-for-one supervisor" should {
    "apply the all-for-one-strategy" in {
      val supervisor = system.actorOf(Props[AllForOneSupervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      supervisor ! Props[FussyWordCounter]
      val secondChild = expectMsgType[ActorRef]

      secondChild ! "I love Akka"
      secondChild ! Report
      expectMsg(3)

      EventFilter[NullPointerException]() intercept{
        child ! ""
      }

      Thread.sleep(500)
      secondChild ! Report
      expectMsg(0)



    }
  }

}

object SupervisionSpec {

  class Supervisor extends Actor {

    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate

    }
    override def receive: Receive = {
      case props: Props =>
        val chilRef = context.actorOf(props)
        sender() ! chilRef
    }
  }

  class AllForOneSupervisor extends Supervisor {


    override val supervisorStrategy = AllForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate

    }
  }

  class NoDeathOnRestartSupervisor extends Supervisor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      // empty
    }
  }
  case object Report
  class FussyWordCounter extends Actor {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        if(sentence.length > 20) throw new RuntimeException("the sentnce is too big!")
        else if(!Character.isUpperCase(sentence(0)))  throw new IllegalArgumentException("sentence must start with uppercase!")
        else words += sentence.split(" ").length
      case _ => throw new Exception("can only received strings")
    }
  }
}