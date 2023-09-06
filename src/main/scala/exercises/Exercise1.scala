package exercises

import akka.actor.{Actor, ActorSystem, Props}

object Exercise1 extends App {

  /**
   * Counter Actor
   *  a) Increment
   *  b) Decrement
   *  c) Print
   */


  val system = ActorSystem("exerciseCounter")

  object Counter {
    def props(value: Int = 0) = Props(new Counter(value))

    case object Increment
    case object Decrement
    case object Print

  }
  class Counter(var value : Int) extends Actor {
    import Counter._
    override def receive: Receive = {
      case Increment => value = value + 1

      case Decrement => value = value - 1

      case Print => println(value)
    }
  }

  val counter = system.actorOf(Counter.props())

  counter ! Counter.Increment
  counter ! Counter.Print

  val counter2 = system.actorOf(Counter.props(55))

  counter2 ! Counter.Decrement
  counter2 ! Counter.Print




}
