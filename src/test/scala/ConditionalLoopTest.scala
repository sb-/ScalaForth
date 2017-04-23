import collection.mutable.Stack
import org.scalatest._
import interpreter.ForthInterpreter
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
    it should "support nested do-loops" in {
        /*val forth = new ForthInterpreter(List("10", "0", "DO", "5", "0", "DO", "j", "i", "add", "dot", "LOOP", "LOOP"))
        forth.execute()
        assert(forth.getStackState() == List())*/
    }
    it should "support iff..[elsef]..then" in {
        var forth = new ForthInterpreter(List("0", "iff", "66", "then", 
            "1", "iff", "99", "then"))
        forth.execute()
        assert(forth.getStackState() == List(99))

        forth = new ForthInterpreter(List("0", "iff", "66", "elsef", "77", "then", 
            "1", "iff", "99", "then"))
        forth.execute()
        assert(forth.getStackState() == List(99, 77))
    }
    it should "support nested iff/elsef" in {
        var forth = new ForthInterpreter(List("3",
            "1", "over", "eql", "iff", "11", "elsef",
            "2", "over", "eql", "iff", "22", "elsef",
            "3", "over", "eql", "iff", "33", "elsef",
            "4", "over", "eql", "iff", "44",
            "then", "then", "then", "then"))
        forth.execute()
        assert(forth.getStackState() == List(33, 3))
    }



    "Comparison/boolean operators" should "support less/greater than, equals" in {
        val forth = new ForthInterpreter(List("10", "5", "lt", "10", "5", "gt",
            "10", "5", "eql", "10", "10", "eql"))
        forth.execute()
        assert(forth.getStackState() == List(1, 0, 1, 0))
    }
    it should "support boolean operators and/or/invert" in {
        var forth = new ForthInterpreter(List("0", "1", "and", "1", "0", "and", "1", "1", "and", "0", "0", "and"))
        forth.execute()
        assert(forth.getStackState() == List(0, 1, 0, 0))

        forth = new ForthInterpreter(List("0", "1", "or", "1", "0", "or", "1", "1", "or", "0", "0", "or"))
        forth.execute()
        assert(forth.getStackState() == List(0, 1, 1, 1))

        forth = new ForthInterpreter(List("0", "invert", "1", "invert"))
        forth.execute()
        assert(forth.getStackState() == List(0, 1))
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