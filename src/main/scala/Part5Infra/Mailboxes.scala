package Part5Infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {

  val system = ActorSystem("MailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * Interesting case #1 - custom priority mailbox
   * P0 -> most important
   * P1
   * P2
   * P3
   */

  // Step 1 mailbox definition
  class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    PriorityGenerator{
      case message: String if message.startsWith("[P0]") => 0
      case message: String if message.startsWith("[P1]") => 1
      case message: String if message.startsWith("[P2]") => 2
      case message: String if message.startsWith("[P3]") => 3
      case _ => 4
    }
  )

  // Step 2 - make it know in the config
  // Step 3 - attach the dispatcher to an actor

  val supportTicketLonger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
//  supportTicketLonger ! PoisonPill
//  Thread.sleep(1000)
//  supportTicketLonger ! "[P3] 3"
//  supportTicketLonger ! "[P0] 0"
//  supportTicketLonger ! "[P1] 1"

  //after which time can I send another message and be prioritized accordingly?


  /**
   * Interesting case #2 - control-aware mailbox
   * we'll use UnboundedControlAwareMailbox
   */

  //Step 1 marking important messages as control messages
  case object ManagementTicket extends ControlMessage
  case object ManagementTicket2 extends ControlMessage

  /*
   Step 2 - Configure who gets the mailbox
   - make the actor attach to the mailbox
   */

  // Method #1
  val conAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
//  conAwareActor ! "[P3] 3"
//  conAwareActor ! "[P0] 0"
//  conAwareActor ! "[P1] 1"
//  conAwareActor ! ManagementTicket
//  conAwareActor ! ManagementTicket2

  // Method #2

  val altControlAwareActor = system.actorOf(Props[SimpleActor], " ")
  altControlAwareActor ! "[P0] 0"
  altControlAwareActor ! "[P1] 1"
  altControlAwareActor ! ManagementTicket
  altControlAwareActor ! ManagementTicket2









}
