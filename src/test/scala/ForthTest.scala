import collection.mutable.Stack
import org.scalatest._

class ForthSpec extends FlatSpec with Matchers {

    "A ForthInterpreter" should "initialize without crashing" in {
        val forth = new ForthInterpreter(List("dup", "drop"))
    }

    it should "add three numbers to the stack" in {
        val forth = new ForthInterpreter(List("1", "2", "3"))
        forth.execute()
        assert(forth.getStackState() == List(3, 2, 1))
    }


}