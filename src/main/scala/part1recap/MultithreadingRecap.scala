package part1recap

import java.lang
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

object MultithreadingRecap extends App{

  val aThread = new Thread(() => println("I'm runing in parallel"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGodBye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGodBye.start()

  // differents runs produce different results!


  class BankAccount(@volatile private var amount: Int){
    override def toString: String = "" + amount

    def withdraw(money:Int) = this.amount -= money

    def safewithdraw(money: Int) = this.synchronized{
      this.amount -= money
    }


  }

  /*
  BA(10000)

  T1 -> Withdraw 1000
  T2 -> Withdraw 2000

  T1 -> this.amount = this.amount - ... // PREEMPTED by the OS
  T2 -> this.amount = this.amount - 2000 = 8000
  T1 -> this.amount = this.amount - 1000 = 9000

  => result 9000

  this.amount = this.amount - 1000 is not atomic
  */
 // inter thread comunications on the JVM
 // wait - notify mechanism

 // Scala features

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future{
    // long computation - on a different thread
    42
  }

  // callbacks
  future.onComplete{
    case Success(43) => println("I found the meaning of life")
    case Failure(_) => println("Something happend with the meaning of life")
  }

  val aProcessedFuture = future.map(_ + 1) // future with 43
  val aFlatFuture = future.flatMap{value =>
    Future(value + 2)
  } // Future with 44

  val filteredFuture = future.filter(_ % 2==0) // NoSuchElementException

  // for comprehension
  val aNonSenseFuture = for {
    meaningOfLife <- future
    filterMeaning <- filteredFuture
  } yield meaningOfLife + filterMeaning

  // andThen, recover/recorverWith

  // Promises











}


