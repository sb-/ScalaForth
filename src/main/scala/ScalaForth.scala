package runtime

import interpreter.ForthInterpreter
object ScalaForth extends App {
    override def main(args: Array[String]) {
        println("before ForthInterpreter")
        val forth = new ForthInterpreter(List(
            "defn", "convert-x-y", "24", "cells", "mul", "add", "enddef",
            "defn", "draw", "convert-x-y", "graphics", "add", "vwrite", "enddef",
            "defn", "draw-white", "1", "rot", "rot", "draw", "enddef",
            "defn", "draw-black", "0", "rot", "rot", "draw", "enddef",
            "variable", "snake-x-head", "500", "cells", "allot",
            "variable", "snake-y-head", "500", "cells", "allot",
            "variable", "apple-x",
            "variable", "apple-y",
            "0", "constant", "left",
            "1", "constant", "up",
            "2", "constant", "right",
            "3", "constant", "down",
            "24", "constant", "width",
            "24", "constant", "height",

            "variable", "direction",
            "variable", "length",

            "defn", "snake-x", "cells", "snake-x-head", "add", "enddef",

            "defn", "snake-y", "cells", "snake-y-head", "add", "enddef",
            
            "defn", "draw-walls",
            "width", "0", "DO",
            "i", "0", "draw-black",
            "i", "height", "1", "sub", "draw-black",
            "LOOP",
            "height", "0", "DO",
            "0", "i", "draw-black",
            "width", "1", "sub", "i", "draw-black",
            "LOOP", "enddef",

            "defn", "initialize-snake",
            "4", "length", "vwrite",
            "length", "vread", "1", "add", "0", "DO",
            "12", "i", "sub", "i", "snake-x", "vwrite",
            "12", "i", "snake-y", "vwrite",
            "LOOP",
            "left", "direction", "vwrite", "enddef",

            "defn", "set-apple-position", "apple-x", "vwrite", "apple-y", "vwrite", "enddef",
            "defn", "initialize-apple", "4", "4", "set-apple-position", "enddef",

            "defn", "initialize",
            "width", "0", "DO",
            "height", "0", "DO",
                "j", "i", "draw-white",
            "LOOP",
            "LOOP",
            "draw-walls",
            "initialize-snake",
            "initialize-apple", "enddef",

            // "defn", "move-up", "-1", "snake-y-head", "add", "vwrite", "enddef",
            // "defn", "move-left", "-1", "snake-x-head", "add", "vwrite", "enddef",
            // "defn", "move-down", "1", "snake-y-head", "add", "vwrite", "enddef",
            // "defn", "move-right", "1", "snake-x-head", "add", "vwrite", "enddef",

            "defn", "move-up", "-1", "snake-x-head", "vread",  "add", "snake-x-head", "vwrite", "enddef",
            "defn", "move-left", "-1", "snake-y-head", "vread", "add", "snake-y-head", "vwrite", "enddef",
            "defn", "move-down", "1", "snake-x-head", "vread", "add", "snake-x-head", "vwrite", "enddef",
            "defn", "move-right", "1", "snake-y-head",  "vread", "add", "snake-y-head", "vwrite", "enddef",

            "defn", "move-snake-head", "direction", "vread",
            "left", "over", "eql", "iff", "move-left", "elsef",
            "up", "over", "eql", "iff", "move-up", "elsef",
            "right", "over", "eql", "iff", "move-right", "elsef",
            "down", "over", "eql", "iff", "move-down",
            "then", "then", "then", "then", "drop", "enddef",

            "defn", "move-snake-tail", "0", "length", "vread", "DO",
            "i", "snake-x", "vread", "i", "1", "add", "snake-x", "vwrite",
            "i", "snake-y", "vread", "i", "1", "add", "snake-y", "vwrite",
            "-1", "LOOPADD", "enddef",

            "defn", "is-horizontal", "direction", "vread", "dup",
            "left", "eql", "swap",
            "right", "eql", "or", "enddef",

            "defn", "is-vertical", "direction", "vread", "dup",
            "up", "eql", "swap",
            "down", "eql", "or", "enddef",

            "defn", "turn-up", "is-horizontal", "iff", "up", "direction", "vwrite", "then", "enddef",
            "defn", "turn-left", "is-vertical", "iff", "left", "direction", "vwrite", "then", "enddef",
            "defn", "turn-down", "is-horizontal", "iff", "down", "direction", "vwrite", "then", "enddef",
            "defn", "turn-right", "is-vertical", "iff", "right", "direction", "vwrite", "then", "enddef",

            "defn", "change-direction",
            "37", "over", "eql", "iff", "turn-left", "elsef",
            "38", "over", "eql", "iff", "turn-up", "elsef",
            "39", "over", "eql", "iff", "turn-right", "elsef",
            "40", "over", "eql", "iff", "turn-down",
            "then", "then", "then", "then", "drop", "enddef",

            "defn", "check-input",
            "last-key", "vread", "change-direction",
            "0", "last-key", "vwrite", "enddef",

            "defn", "random-position",
            "width", "4", "sub", "rand", "2", "add", "enddef",

            "defn", "move-apple",
            "apple-x", "vread", "apple-y", "vread", "draw-white",
            "random-position", "random-position",
            "set-apple-position", "enddef",

            // "defn", "grow-snake", "1", "length", "add", "vwrite", "enddef",
            "defn", "grow-snake", "1", "length", "vread", "add", "length", "vwrite", "enddef",

            "defn", "check-apple",
            "snake-x-head", "vread", "apple-x", "vread", "eql",
            "snake-y-head", "vread", "apple-y", "vread", "eql",
            "and", "iff",
            "move-apple",
            "grow-snake",
            "then", "enddef",

            "defn", "check-collision", "snake-x-head", "vread", "snake-y-head", "vread",
            "convert-x-y", "graphics", "add", "vread", "1", "eql", "enddef",

            "defn", "draw-snake",
            "length", "vread", "0", "DO",
            "i", "snake-x", "vread", "i", "snake-y", "vread", "draw-black",
            "LOOP",
            "length", "vread", "snake-x", "vread",
            "length", "vread", "snake-y", "vread",
            "draw-white", "enddef",

            "defn", "draw-apple",
            "apple-x", "vread", "apple-y", "vread", "draw-black", "enddef",

            "defn", "game-loop",
            "begin",
            "printstack",
            "draw-snake",
            "draw-apple",
            "100", "sleep",
            "check-input",
            "move-snake-tail",
            "move-snake-head",
            "check-apple",
            "check-collision",
            "until",
            "emitstr", " Game, Over", "enddef",

            "defn", "start", "initialize", "game-loop", "enddef",
            "start"
            ))
        println("executing forth interpreter")
        forth.execute()
        println(forth.functions)
        while (true) {

        }
    }
}
