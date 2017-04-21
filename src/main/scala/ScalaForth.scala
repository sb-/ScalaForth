package runtime

import interpreter.ForthInterpreter
object ScalaForth extends App {
    override def main(args: Array[String]) {
        println("before ForthInterpreter")
        val forth = new ForthInterpreter(List("defn", "loop-color-test", "1600", "1", "DO", "2", "rand", "i", "graphics", "add",
            "vwrite", "LOOP", "enddef", "begin", "loop-color-test", "1", "until"))
        println("executing forth interpreter")
        forth.execute()
    }
}
