package Part6Patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App{

  /*
  ResourceActor
    -open => it can receive read/write request to the source
    -otherwise it will postpone all read/write request until the state is open

    ResourceActor is close
      -open => switch to the open state
      - Read, Write are postponed

    ResourceActor is open
      - Read/Write are handled
      - Close => Switch to the close state

    [Open, Read, Read, Write]
      -Switch to the open state
      -read the data
      -read the data again
      -write the data

    [Read, Open, Write]
      -Stash Read
        Stash: [Read]
      - Open => Switch the open state
        Mailbox: [Read, write]
      - Read and Write are handle

   */

  case object Open
  case object Read
  case object Close
  case class Write(data: String)

  // Step 1- mix-in the stash trait

  class ResourceActor extends Actor with ActorLogging with Stash {

    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("opening resource")
        //Step 3 - unstashAll when you switch the message handler
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing $message because I cant handle it in close state")
        // Step 2 - stash away what you cant handle
        stash()
    }

    def open: Receive = {
      case Read =>
        // do some computational
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I am writing data $data")
        innerData = data
      case Close =>
        log.info("Closing resource")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I cant handle it in open state")
        stash()
    }

  }


  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])

  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Open
  resourceActor ! Write("I love Scala")
  resourceActor ! Close
  resourceActor ! Read


















}
