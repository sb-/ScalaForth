import collection.mutable.Stack
import org.scalatest._

class ForthSpec extends FlatSpec with Matchers {

    "Stack manipulations" should "initialize without crashing" in {
        val forth = new ForthInterpreter(List("dup", "drop"))
    }

    it should "add three numbers to the stack" in {
        val forth = new ForthInterpreter(List("1", "2", "3"))
        forth.execute()
        assert(forth.getStackState() == List(3, 2, 1))
    }

    it should "duplicate the top of the stack" in {
        val forth = new ForthInterpreter(List("1", "2", "3", "dup"))
        forth.execute()
        assert(forth.getStackState() == List(3, 3, 2, 1))
    }

    it should "drop the top of the stack" in {
        val forth = new ForthInterpreter(List("1", "2", "3", "drop"))
        forth.execute()
        assert(forth.getStackState() == List(2, 1))
    }

    it should "correctly reorder with the swap command" in {
        val forth = new ForthInterpreter(List("1", "2", "3", "swap"))
        forth.execute()
        assert(forth.getStackState() == List(2, 3, 1))
    }

    it should "correctly duplicate the 2nd with the over command" in {
        val forth = new ForthInterpreter(List("1", "2", "3", "over"))
        forth.execute()
        assert(forth.getStackState() == List(2, 3, 2, 1))
    }

    it should "correctly reorder the top 3 with the swap command" in {
        val forth = new ForthInterpreter(List("99", "1", "2", "3", "rot"))
        forth.execute()
        assert(forth.getStackState() == List(1, 3, 2, 99))
    }

}