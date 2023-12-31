package Part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._
  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(BasicSpec.propsSimpleActor)
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)
    }

    "A blackhole actor" in {
      val blackhole = system.actorOf(BasicSpec.propsBlackhole)
      val message = "hello, test"
      blackhole ! message

      expectNoMessage(1 seconds)
    }

  }

  "A lab test actor" should {
    val labTestActor = system.actorOf(BasicSpec.propsLabTestActor)
    "turn a string to uppercase" in {
      labTestActor ! "I love Akka"
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
    }

    "Reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")

    }

    "Reply with favorite tech" in {

      labTestActor ! "favoriteTech"
      expectMsgAllOf("scala", "akka")

    }

    "Reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2)
    }

    "Reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"
      expectMsgPF() {
        case "akka" => // only care the PF is defined
        case "scala" =>
      }
    }

  }

  object BasicSpec {
    class SimpleActor extends Actor {
      override def receive: Receive = {
        case message => sender() ! message
      }

    }
    val propsSimpleActor = {Props(new SimpleActor)}

    class Blackhole extends Actor {
      override def receive: Receive = Actor.emptyBehavior
    }

    val propsBlackhole = {Props(new Blackhole)}

    class LabTestActor extends Actor {
      val random = new Random()
      override def receive: Receive = {
        case "greeting" =>
          if (random.nextBoolean()) sender()! "hi" else sender() ! "hello"
        case "favoriteTech" =>
          sender() ! "scala"
          sender() ! "akka"

        case message: String => sender() ! message.toUpperCase()
      }
    }

    val propsLabTestActor= {Props(new LabTestActor)}



  }

}
