import collection.mutable.Stack
import org.scalatest._
import interpreter.ForthInterpreter

class FunctionSpec extends FlatSpec with Matchers {
    "Functions" should "define and execute correctly" in {
        var forth = new ForthInterpreter(List("defn", "addone", "1", "add", "enddef", "6"))
        forth.execute()
        // Function shouldn't be called
        assert(forth.getStackState() == List(6))
        
        forth = new ForthInterpreter(List("defn", "addone", "1", "add", "enddef", "6", "addone"))
        forth.execute()
        assert(forth.getStackState() == List(7))

    }
}