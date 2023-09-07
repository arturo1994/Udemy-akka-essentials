package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App{

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import Mom._
    import FussyKid._

    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case MomAsk(_) => {
        if (state == HAPPY){
          sender ! KidAccept
        }else{
          sender ! KidReject
        }
      }
    }
  }

  class StatelessFussyKid extends  Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) =>
      case MomAsk(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)
      case MomAsk(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class MomAsk(msg: String) // do you want to play?
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._
    override def receive: Receive = {
      case MomStart(kidRef) =>{
        kidRef ! Food(VEGETABLE)
        kidRef ! MomAsk("Do you want to play?")
      }
      case KidAccept => println("Yes, my kid is happy")
      case KidReject => println("My kid is sad, but he is healthy")
    }
  }

  val system = ActorSystem("ChangingActorBehaviorDemo")
  val mom = system.actorOf(Props[Mom])
  val fussyKid = system.actorOf((Props[FussyKid]))
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])

//  mom ! Mom.MomStart(fussyKid)
  mom ! Mom.MomStart(statelessFussyKid)

}
