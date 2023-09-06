package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "hello, there!"
      case message: String => println(s"[$self] I have received $message")
      case number: Int => println(s"[simple actor] I have received a NUMBER $number")
      case SpecialMessage(content) => println("[simple actor] I have received something special")
      case SendMessageToYourself(content) =>{
        self ! content
      }
      case SayHiTo(ref) => {
        ref ! "Hi"
      }
      case WirelessPhoneMessage(content,ref) => ref forward (content+"s") // I keep the original sender


    }
  }

  val system = ActorSystem("ActorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "SimpleActor")

  simpleActor ! "hello, actor"
  // 1 - message can be of any type
  // a) message must be inmmutable
  // b) message must be serializable
  // in practice use case classes and case objects
  simpleActor ! 22

  case class SpecialMessage(content: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self === this  in OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - How actor can reply messages

  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi"

  // 5 - forwarding messages with the original sender
  case class WirelessPhoneMessage(content:String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)

}
