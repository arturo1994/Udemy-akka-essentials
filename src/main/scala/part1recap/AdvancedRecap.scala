package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App{

  // partial functions
  val partialFunction: PartialFunction[Int,Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val modifiedList = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }

  // lifting
  val lifted = partialFunction.lift // total funtion Int => Option[Int]
  lifted(2) //65
  lifted(5000) // None

  //orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }
//
  pfChain(5) // 999 per partialfunction
  pfChain(60) // 9000
//  pfChain(232) // throw a MatchError

  // type aliases
  type ReciveFunction = PartialFunction[Any, Unit]

  def recive: ReciveFunction = {
    case 1 => println("hello")
    case _ => println("confused ...")
  }

  // Implicits

  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  // implicit conversion
  // 1) implicit defs
  case class Person(name: String){
    def great = s"Hi, mi name is $name"
  }

  implicit def fromStringToPerson(string:String): Person = Person(string)

  "Peter".great
  // fromStringToPerson("Peter").greet - automatically done by the compiler

  // 2) implicit classes

  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  "Lassie".bark

  // new Dog("lassie").bark - automatically done by the compiler

  // organize
  // Local scope

  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1,2,3).sorted // List(3, 2, 1)

  // imported Scope

  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future{
    println("hello, future")
  }

  // companion objects of the types included in the call

  object Person{
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a,b) => a.name.compareTo(b.name) < 0)
  }

  println(List(Person("Bob"), Person("Alice")).sorted)

  // List(Person(Alice), Person(Bob))
























}
