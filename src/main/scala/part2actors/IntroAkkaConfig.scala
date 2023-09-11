package part2actors
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
object IntroAkkaConfig extends App{

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - Inline Configuration
   */

  val configString =
    """
      |akka{
      |loglevel = "Error"
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)

  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))

  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"


  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "Remember me"

  /**
   * 3 - Separate config in the same file
   */

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigFileDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])

  specialConfigActor ! "Remember me, I am special"

  /**
   * 4 - Separate config in other file
   */

  val separeteConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"separate config log level ${separeteConfig.getString("akka.loglevel")}")

  /**
   * Different files formats
   * JSON, Properties
   */
  val jsonConfig = ConfigFactory.load("jsonFiles/jsonConfig.json")
  println(s"separate config log level ${jsonConfig.getString("akka.loglevel")}")

  val propConfig = ConfigFactory.load("prop/propConfig.properties")
  println(s"separate config log level ${propConfig.getString("akka.loglevel")}")

}
