package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}


object Exercise4 extends App{

  val system = ActorSystem("exrecise4")


  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest

  }

  class Citizen extends Actor {

    import Citizen._
    import VoteAggregate._
    override def receive: Receive = candidateVote(Option(null)) /// to do

    def candidateVote(select: Option[String] ): Receive = {

      case Vote(candidate) => {
        context.become(candidateVote(Some(candidate)))
      }

      case VoteStatusRequest => {
        sender ! VoteStatusReply(select)
      }


    }

  }


  object VoteAggregate {
    case class AggregateVotes(citizens: Set[ActorRef])
    case class VoteStatusReply(candidate: Option[String])

  }

  class VoteAggregate extends Actor {
    import Citizen._
    import VoteAggregate._

    var  PendingCitizen: Set[ActorRef] = Set()
    override def receive: Receive = CountVotes(scala.collection.mutable.Map("Martin" -> 0, "Jonas" -> 0, "Roland" -> 0))

    def CountVotes(results: scala.collection.mutable.Map[String, Int]): Receive = {



      case AggregateVotes(citizens) => {
        PendingCitizen = citizens
        citizens.foreach(x => x ! VoteStatusRequest )

      }

      case VoteStatusReply(select) => {

         val NewPendingCitizen = PendingCitizen - sender()

        results(select.get) += 1

        context.become(CountVotes(results))

        if (NewPendingCitizen.isEmpty) {
          println(results)
        } else {
          PendingCitizen = NewPendingCitizen
        }

      }


    }

  }


    val alice = system.actorOf(Props[Citizen], "alice")
    val bob = system.actorOf(Props[Citizen], "bob")
    val charlie = system.actorOf(Props[Citizen], "charlie")
    val daniel = system.actorOf(Props[Citizen], "daniel")

    alice ! Citizen.Vote("Martin")
    bob ! Citizen.Vote("Jonas")
    charlie ! Citizen.Vote("Roland")
    daniel ! Citizen.Vote("Roland")

    val voteAgregator = system.actorOf(Props[VoteAggregate])

    voteAgregator ! VoteAggregate.AggregateVotes(Set(alice, bob, charlie, daniel))

//
//
//

//

//
//
}
