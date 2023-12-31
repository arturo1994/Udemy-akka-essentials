package Part3testing

import akka.actor.FSM.Event
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogsSpec extends TestKit(ActorSystem("InterceptingLogsSpec", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override protected def afterAll(): Unit = {TestKit.shutdownActorSystem(system)}

  import InterceptingLogsSpec._

  val item = "Rock the JVM akka course"
  val creditcard = "1234-1234-1234-1234"
  val invalidCreditcard = "0234-1234-1234-1234"
  "A checkout flow" should{
    "Correctly log the dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.", occurrences = 1) intercept{
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditcard)
      }

    }
    "freak out if the payment is denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept{
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, invalidCreditcard)
      }
    }
  }
}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case class DispatchOrder(item: String)
  case object OrderConfirmed
  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fullfilmentManager = context.actorOf(Props[FullfilmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>
        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fullfilmentManager ! DispatchOrder(item)
        context.become(pendindFulfillment(item))
      case PaymentDenied => throw new RuntimeException("I can't handle this anymore!")
    }

    def pendindFulfillment(item:String): Receive ={
      case OrderConfirmed => context.become(awaitingCheckout)
    }

  }

  class PaymentManager extends Actor{
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if(card.startsWith("0")) sender() ! PaymentDenied
        else {
          Thread.sleep(4000)
          sender() ! PaymentAccepted
        }
    }


  }

  class FullfilmentManager extends Actor with ActorLogging{

    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched.")
        sender() ! OrderConfirmed
    }
  }

}