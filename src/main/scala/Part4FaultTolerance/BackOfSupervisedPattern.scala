package Part4FaultTolerance

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import scala.concurrent.duration._
import java.io.File
import scala.io.Source

object BackOfSupervisedPattern extends App {

  case object ReadFile
  class FileBasedPersistenActor extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit = {
      log.info("persistent actor starting")
    }

    override def postStop(): Unit = {
      log.info("persistent actor has been stopped")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info("persistent actor restarting")
    }
    override def receive: Receive = {

      case ReadFile =>
        if(dataSource == null) {
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_.txt"))
          log.info("I've just read and import file " + dataSource.getLines().toList)
        }
    }


  }

  val system = ActorSystem("BackOfSupervisedPattern")
//
//  val simpleActor = system.actorOf(Props[FileBasedPersistenActor], "simpleActor")
//
//  simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(

    Backoff.onFailure(
      Props[FileBasedPersistenActor],
      "simpleActorBackOff",
      3 seconds,
      30 seconds,
      0.2
    )
  )

//  val simpleSupervisor = system.actorOf(simpleSupervisorProps, "simpleSupervisor")
//
//  simpleSupervisor ! ReadFile


  /*
  simpleSupervisor
    - child called simpleBackoffActor (props of type FileBasedPersistenActor)
    - SupervisorStrategy is the default one (restarting on everything)
      - First attempt after 3 sec
      - next attempt is 2x the previous attempt
   */

  val stopSuperviserProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[FileBasedPersistenActor],
      "stopBackoffsupervisor",
      3 seconds,
      30 seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy(){
        case _ => Stop
      }
    )
  )

//  val simpleStopSupervisor = system.actorOf(stopSuperviserProps, "simpleStopSupervisor")
//
//  simpleStopSupervisor ! ReadFile

  class EagerFBPActor extends FileBasedPersistenActor  {
    override def preStart(): Unit = {
      log.info("persistent actor starting")
      dataSource = Source.fromFile(new File("src/main/resources/testfiles/important_.txt"))
    }

  }

//  val eagerFBPActor = system.actorOf(Props[EagerFBPActor])
  // actor Initialization Execption => stop

  val repeatedSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[EagerFBPActor],
      "eagerActor",
      1 seconds,
      30 seconds,
      0.1
    )
  )

  val eagerSupervisor = system.actorOf(repeatedSupervisorProps, "eagerSupervisor")
  /*
  eagerSupervisor
    - Child eagerActor
      - will die on start with actorInitializationException
      - trigger the supervisor strategy in eagerSupervisor => STOP eagerActor
    - Backoff will kick in after 1 second, 2s, 4s, 8s, 16s

   */

}
