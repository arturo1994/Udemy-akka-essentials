package part1recap

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
  pfChain(232) // throw a MatchError



}
