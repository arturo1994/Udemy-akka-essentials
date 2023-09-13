package Part3testing

import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {

    "Register a Slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "Send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadString = "I love Akka"

      master ! Work(workloadString)

      // the interation between the master and the slave actor
      slave.expectMsg(SlaveWork(workloadString, testActor))
      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3)) // test actor recives the Report(3)

    }

    "aggragate data correctly" in {

      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadString = "I love Akka"
      master ! Work(workloadString)
      master ! Work(workloadString)

      slave.receiveWhile(){
        case SlaveWork(`workloadString`, `testActor` ) => slave.reply(WorkCompleted(3, testActor))
      }

      expectMsg(Report(3))
      expectMsg(Report(6))
    }

  }

}

object TestProbeSpec {
  case class Work(test: String)
  case class SlaveWork(test: String, OriginalRequest: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Register(slaveRef: ActorRef)
  case object RegistrationAck
  case class Report(totalCount: Int)

  class Master extends Actor {

    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0 ))
      case _ => //ignore
    }

    def online(slaveRef: ActorRef, TotalWordCount: Int): Receive ={
      case Work(test) => slaveRef ! SlaveWork(test, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = TotalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }
}
