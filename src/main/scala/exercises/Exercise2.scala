package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Exercise2 extends App {

  /**
   * Bank account as an actor receive
   * a) Deposit an amount
   * b) Withdraw an amount
   * c) Statement
   *
   * Replies with
   * - Success
   * - Failure
   */


  val system = ActorSystem("exerciseBankAccount")

  object BankAccount{
    def props(amount: Int = 0) = Props(new BankAccount(amount))
    case class Deposit(var value: Int)
    case class Withdraw(var value: Int)
    case object Statement


  }

  class BankAccount(var amount: Int) extends Actor{

    import BankAccount._
    import Person._
    override def receive: Receive = {
      case Deposit(value) => {
        if(value<=0) {
          sender ! TransationFailure("value have to be greater than zero")
        } else {
          amount += value
          sender ! TransationSucces(s"Deposit $value done correctly")
        }
      }
      case Withdraw(value) => {
        if (value <= 0) {
          sender ! TransationFailure("value have to be greater than zero")
        }
        else if (value > amount){
          sender ! TransationFailure("value is greater than account amount")
        }
        else {
          amount -= value
          sender ! TransationSucces(s"Withdraw $value done correctly")
        }
      }
      case Statement => println(s"[${self.path}] has $amount")
    }

  }

  object Person{

    def props() = Props(new Person())
    case class Operation( ref: ActorRef)

    case class TransationFailure(errorMes: String)

    case class TransationSucces(errorMes: String)

  }

  class Person extends Actor {

    import Person._
    import BankAccount._

    override def receive: Receive = {

    case Operation(ref) =>{
      ref ! Deposit(100)
      ref ! Withdraw(40)
      ref ! Withdraw(2000)
      ref ! Statement
    }
    case TransationSucces(msg) => println(s"[${self.path}] $msg")
    case TransationFailure(msg) => println(s"[${self.path}] $msg")

    }

  }
//
  val lisaAcount = system.actorOf(BankAccount.props(100), name = "account")
  val lisa= system.actorOf(Person.props(), "lisa")
  lisa ! Person.Operation(lisaAcount)
}
