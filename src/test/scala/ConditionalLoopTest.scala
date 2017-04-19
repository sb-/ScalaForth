import collection.mutable.Stack
import org.scalatest._

class ConditionalLoopSpec extends FlatSpec with Matchers {
    "Conditionals" should "support begin-until loops" in {
        val forth = new ForthInterpreter(List("10", "begin", "printstack", "1", "sub", "dup", "dup", "dot", "until", "6"))
        forth.execute()
        // TODO: This behavior might be incorrect.
        assert(forth.getStackState() == List(6, 0))
    }
    it should "support do-loops" in {
        // TODO: Possibly check for the number of times print was called
        val forth = new ForthInterpreter(List("10", "0", "DO", "i", "dot", "LOOP"))
        forth.execute()
        assert(forth.getStackState() == List())
    }

    "Variables/Constants" should "support variables" in {
        var forth = new ForthInterpreter(List("variable", "test", "test"))
        forth.execute()
        assert(forth.getStackState().length == 1)
    }
    it should "allow variables to be modified/read" in {
        var forth = new ForthInterpreter(List("variable", "test", "123", "test", "vwrite", "test", "vread"))
        forth.execute()
        assert(forth.getStackState() == List(123))
    }
    it should "support constants" in {
        var forth = new ForthInterpreter(List("42", "constant", "test", "test"))
        forth.execute()
        assert(forth.getStackState() == List(42))
    }
}