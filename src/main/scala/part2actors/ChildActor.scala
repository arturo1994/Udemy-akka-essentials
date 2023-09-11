package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActor.CreditCard.{AttachToAccount, CheckStatus}

object ChildActor extends App {

  // Actor can create other actors

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {

    import  Parent._
    override def receive: Receive = {
      case CreateChild(name) => {
        println(s"${self.path} creating child")
        //  create a new actor here
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
      }
    }

    def withChild(chilRef: ActorRef): Receive = {
      case TellChild(message) => chilRef forward message
    }


  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }


  import Parent._
  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")

  parent ! CreateChild("child")
  parent ! TellChild("hey Kid")

  // Actor Hierarchies
  //parent -> child -> grandchild
  //       -> child2 ->

  /*
  Guardian Actors (Top-level)
  - /system = system guardian
  - /user = user-level guardian
  - / = root guardian
   */

  /**
   * Actor selection
   */

  var childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  /**
   * Danger!
   *
   * Never pass mutable actor state, or the "this" reference, to child actors.
   */




    object NaiveBankAccount {
      case class Deposit(amount: Int)
      case class Withdraw(amount: Int)
      case object InitializeAccount
    }

  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._
    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this)
      case Deposit(fund) => deposit(fund)
      case Withdraw(fund) => withdraw(fund)

    }

    def deposit(fund: Int) = {
      println(s"${self.path} depositing $fund on top of $amount")
      amount += fund
    }
    def withdraw(fund: Int) = {
      println(s"${self.path} withdrawing $fund from $amount")
      amount -= fund
    }

  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount)
    case object CheckStatus

  }

  class CreditCard extends Actor {
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }

    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your message as been processed.")
        account.withdraw(1)
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  var ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus


  // WRONG

}
