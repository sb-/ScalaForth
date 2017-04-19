import collection.mutable.Stack
import org.scalatest._

class ArithmeticSpec extends FlatSpec with Matchers {

    "A ForthInterpreter" should "add two numbers correctly" in {
        val forth = new ForthInterpreter(List("1", "2", "add"))
        forth.execute()
        assert(forth.getStackState() == List(3))
    }

    it should "subtract two numbers correctly" in {
        val forth = new ForthInterpreter(List("5", "4", "sub"))
        forth.execute()
        assert(forth.getStackState() == List(1))
    }

    it should "do integer division correctly" in {
        var forth = new ForthInterpreter(List("4", "2", "div"))
        forth.execute()
        assert(forth.getStackState() == List(2))

        forth = new ForthInterpreter(List("4", "3", "div"))
        forth.execute()
        assert(forth.getStackState() == List(1))
    }

    it should "multiply correctly" in {
        val forth = new ForthInterpreter(List("5", "3", "mul"))
        forth.execute()
        assert(forth.getStackState() == List(15))
    }

    it should "do remainder correctly" in {
        var forth = new ForthInterpreter(List("4", "2", "mod"))
        forth.execute()
        assert(forth.getStackState() == List(0))

        forth = new ForthInterpreter(List("5", "3", "mod"))
        forth.execute()
        assert(forth.getStackState() == List(2))
    }

}