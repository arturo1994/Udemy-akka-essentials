package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Exercise3 extends App{

  val system = ActorSystem("Exercise3")

  object Counter{
    case object Decrement
    case object Increment
    case object Print

  }
  class Counter extends Actor {
    import Counter._

    var amount = 0
    override def receive: Receive = Pri

    def Dec: Receive = {
      case Print => {
        println(amount)
        context.become(Pri)
      }
      case Decrement => amount -= 1
      case Increment => {
        amount += 1
        context.become(Inc)
      }
    }
    def Inc: Receive = {
      case Print => {
        println(amount)
        context.become(Pri)
      }
      case Decrement => {
        amount -= 1
        context.become(Dec)
      }
      case Increment => {
        amount += 1
      }
    }
    def Pri: Receive = {
      case Print => println(amount)
      case Decrement => {
        amount -= 1
        context.become(Dec)
      }
      case Increment => {
        amount += 1
        context.become(Inc)
      }


    }
  }

  object test{
    case class StartTest(ref: ActorRef)
  }
  class test extends Actor {

    import test._
    import Counter._
    override def receive: Receive = {
      case StartTest(ref) => {
        ref ! Print
        ref ! Decrement
        ref ! Decrement
        ref ! Decrement
        ref ! Increment
        ref ! Increment
        ref ! Increment
        ref ! Increment
        ref ! Print
      }
    }

  }

  val counter1 = system.actorOf(Props[Counter])
  val test1 = system.actorOf(Props[test])

  test1 ! test.StartTest(counter1)

}
