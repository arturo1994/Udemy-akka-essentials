package Part5Infra

import Part4FaultTolerance.StaringStoppingActor.Parents.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}
import com.sun.xml.internal.ws.api.Cancelable

import java.time.LocalDate.now
import scala.concurrent.duration._
import java.util.Calendar

object TimersSchedulers extends App{

  class SimpleActor extends Actor with ActorLogging {

    override def receive: Receive =  {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("SchedulersTimersDemo")
//  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simpleActor")

  import  system.dispatcher

//  system.scheduler.scheduleOnce(1 second){
//    simpleActor ! "reminder"
//  }
//
//  val routine = system.scheduler.schedule(1 second, 2 seconds){
//    simpleActor ! "hearbeat"
//  }
//
//  system.scheduler.scheduleOnce(10 seconds){
//    routine.cancel()
//  }

  /**
   * Exercise: implement a self-closing actor
   * - if the actor receives a message (anything), you have 1 second to send iit another message
   * - if the time window expires, the actor will stop itself
   * - if you sen another message, the time window is reset
   */

  class ActorWindow extends Actor with ActorLogging {

    var shedule = createTimeoutwindow()
    def createTimeoutwindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 seconds) {
        self ! "timeout"
      }
    }
    override def receive: Receive = {
      case "timeout" =>
        context.stop(self)
        log.info("morire")
      case message =>
        log.info(message.toString)
        shedule.cancel()
        shedule =  createTimeoutwindow()

    }

  }
//
//  val actorW = system.actorOf(Props[ActorWindow])
//
//  system.scheduler.scheduleOnce(250 millis){
//    actorW ! "ping"
//  }
//  system.scheduler.scheduleOnce(250 millis) {
//    actorW ! "pong"
//  }
//
//  system.scheduler.scheduleOnce(250 millis) {
//    actorW ! "ping"
//  }
//
//  system.scheduler.scheduleOnce(250 millis) {
//    actorW ! "pong"
//  }
//
//  system.scheduler.scheduleOnce(2 seconds) {
//    actorW ! "estas vivo"
//  }



  case object TimerKey
  case object Start
  case object Reminder
  case object Stop

  class TimerBaseHeartbeatActor extends Actor with ActorLogging with Timers {

    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Starting")
        timers.startPeriodicTimer(TimerKey, Reminder, 100 millis)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerBaseHeartbeatActor = system.actorOf(Props[TimerBaseHeartbeatActor], "timerActor")

  system.scheduler.scheduleOnce(5 seconds){
    timerBaseHeartbeatActor ! Stop
  }








//
//  import java.util.Calendar._
//
//  var dat = getInstance()
//  Thread.sleep(5000)
//
//
//  var dat1 = getInstance().getTime
//  dat1.
//  println(dat1.compareTo(dat))
//  val c1 = now.atTime()
//  Thread.sleep(3)
//  val c2 = now
//
//  println(c1 - c2)


}
