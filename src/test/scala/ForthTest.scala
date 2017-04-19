import collection.mutable.Stack
import org.scalatest._

class ForthSpec extends FlatSpec with Matchers {

    "A ForthInterpreter" should "initialize without crashing" in {
        val forth = new ForthInterpreter(List("dup", "drop"));
    }
}