package part1recap

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








}
