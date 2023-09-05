package part1recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  /*
  Daniel's rants
   */

  /**
   * DR: OOP incapsulation in only valid in the single threaded model
   */

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.synchronized{
      this.amount -= money
    }
    def deposit(money: Int) = this.synchronized{
      this.amount += money
    }
    def getAmount = this.synchronized{ amount }

  }

//  val account = new BankAccount(2000)
//
//  for (_ <- 1 to 1000){
//    new Thread(() => account.withdraw(1)).start()
//  }
//
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.deposit(1)).start()
//  }

//  Thread.sleep(60)

//  println(account.getAmount)

  // OOP encapsulation is broken in a multithread env
  // sincronization! Locks to the rescue

  // deadLocks. livelocks

  /**
   * DR #2: Delegate something to a thread is a Pain.
   */

  // you have a running thread and you want to pass a runnable to the thread.

  var task: Runnable = null

  val runningThread: Thread = new Thread(() => {

    while (true){
      while (task == null){
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }

      task.synchronized{
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }


  })

  def delagateToBackgroundThread(r:Runnable) = {
    if (task==null) task = r
    runningThread.synchronized{
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(1000)
  delagateToBackgroundThread(()=>println("42"))
  Thread.sleep(1000)
  delagateToBackgroundThread(()=>println("This shoud run in the background"))

  /**
   * DR #3: Tracing and dealing with errors in a multithreaded env is a PITN.
   */

  // 1M  numbers in between 10 threads
   import scala.concurrent.ExecutionContext.Implicits.global

  val future = (0 to 9)
    .map(i => 100000*i until 100000*(i+1)) // 0 - 99999, 100000 - 199999, 200000 - 299999, etc
    .map(range => Future{
      if( range.contains(546763)) throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(future)(_+_) // future with the sum of all the numbers
  sumFuture.onComplete(println)


}

