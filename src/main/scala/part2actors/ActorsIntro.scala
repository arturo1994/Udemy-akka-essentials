package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // parte 1 actor systems

  val actorSystem = ActorSystem("FirstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  // word count actor

  class WordCountActor extends Actor {

    // internal data
    var totalWords = 0

    // behavior
    def receive: PartialFunction[Any, Unit] ={
      case message: String =>
        println(s"[world counter] I have received $message")
        totalWords += message.split(" ").length
      case msg => println(s"I can't understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor

  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter") // "tell"
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "AnotherWordCounter")

  // part4 - comunication

  wordCounter ! "I'm learning AKKA and it's pretty damn cool!"
  anotherWordCounter ! "A different massege"
  // asynchronous!

  object Person {
    def props(name: String) = Props(new Person(name))
  }

  class Person(name:String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi mi name is $name")
      case _ =>
    }
  }

  val person = actorSystem.actorOf(Person.props("Bob"))
  person ! "hi"

}
