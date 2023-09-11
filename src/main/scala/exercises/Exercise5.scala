package exercises

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Exercise5 extends App {

  val system = ActorSystem("exercise5")

  object WordCounterMaster{
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }

  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    var WorkerList: List[ActorRef] = List()
    var InternalActualWorker = 1
    override def receive: Receive = {
      case Initialize(nChildren) =>
        for (i <- 1 to nChildren) {
          var worker = context.actorOf(Props[WordCounterWorker], s"worker$i")
          WorkerList = WorkerList :+ worker

        }
        context.become(WorkMain())
        println(s"the length is ${WorkerList.length}")
      case _ => println("first initialize the workers")

    }
        def WorkMain(): Receive = {
          case WordCountTask(text) =>
            var actualWorker = context.actorSelection(s"/user/manager/worker$InternalActualWorker")
            actualWorker ! WordCountTask(text)
            if (InternalActualWorker < WorkerList.length){
              InternalActualWorker += 1
            } else {InternalActualWorker = 1}
          case WordCountReply(count) => {
            println(s"[${sender.path}] the result is $count [${self.path}]")
          }
        }
  }


  class WordCounterWorker extends Actor {

    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(text) =>
        sender ! WordCountReply(text.split(" ").length)
    }
  }

  val WorkerMan = system.actorOf(Props[WordCounterMaster], "manager")

  WorkerMan ! WordCounterMaster.Initialize(5)
  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two d words")
  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two d words")

  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two dc cc words")
  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two d words")
  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two dc cc words")
  Thread.sleep(500)
  WorkerMan ! WordCounterMaster.WordCountTask("two d words")







}
