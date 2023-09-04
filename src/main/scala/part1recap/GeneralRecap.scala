package part1recap

import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false

  var aVariable = 42
  aVariable +=1 // aVariable = 43

  // expressions

  val aConditionedVal = if (aCondition) 42 else 65

  // code block

  val aCodeBlock = {
    if(aCondition) 74
    56
  }

  //  types

  //unit

  val theUnit = println("hello, Scala")
  def aFuntion(x: Int): Int = x + 1

  // recursion -TAIL recursion

  def factrialFunction(n:Int, acc: Int): Int = {
    if(n<=0) acc
    else factrialFunction(n-1, acc*n)
  }

  // OOP
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }


  class Crocodile extends Animal with Carnivore{
    override def eat(a: Animal): Unit = println("crunch!!!")
  }

  // method notations

  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // anonymus classes

  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar!!!")
  }

  aCarnivore eat aDog

  //generics

  abstract class MyList[+A]

  // companion object

  object MyList

  // case classes

  case class Person(name:String, age:Int) // a lot in this course

  // Exceptions

  val aPotentialFailure = try{
    throw new RuntimeException("I am innocent, I swear!") // Nothing
  } catch {
    case e: Exception => "I caught an exception"
  } finally {
    println("some logs")
  }

  // functional programing

  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42) //43
  // incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x+1
  // Int => === Function1[Int, Int]

  // FP is all about working with functions as first-class
  List(1,2,3).map(incrementer)
  // map = Higher order function

  //for comprehensions

  val pairs = for{
    num <- List(1, 2, 3, 4)
    char <- List("a", "b", "c", "d")
  } yield num + "-" + char

  // List(1,2,3,4).flapMap(num => List("a", "b", "c", "d").map(char => num + "-" + char))
  // Seq, Array, List, Vector, Map, Tuples, Sets

  //collections
  //Option and try
  val anOption = Some(2)
  val aTry = Try(
    throw new RuntimeException
  )

  //pattern matching

  val bob = Person("bob", 22)
  val greating = bob match {
    case Person(n, _) => s"Hi, my name in $n"
    case _ => "I dont know my name"
  }

  // ALL THE PATTERNS

}
