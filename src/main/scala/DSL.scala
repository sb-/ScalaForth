package dslrun
import scala.language.dynamics
import interpreter.ForthInterpreter

object ForthDSL {
    val debug_enabled = true    
    def DEBUG(s: Any) {
        if (debug_enabled) {
            println(s)
        }
    }

    var prog = scala.collection.mutable.ListBuffer[String]()

    class general_op extends Dynamic {
        var optype = "";
        def selectDynamic(name: String) = {
            DEBUG(s"selectDynamic CURRENT OPTYPE: $optype applied: $name")
            // stack = stack.push(optype)
            // stack = stack.push(name)
            prog += optype
            prog += name
            new general_op
            //println("In Dynamic GENERALOP: " + name)
        }
        def applyDynamic(name: String)(args: Any*): general_op = {
            DEBUG(s"object $optype - You called '$name' method with " +
            s"following arguments: ${args mkString ", "}")
            if (name == "num") {
                val nums = args.map(x => x.asInstanceOf[Int])
                // stack.push(optype)
                // stack = stack.push(nums(0).toString)
                prog += optype 
                prog += nums(0).toString
                return new general_op
            }
            try {
                val names = args.map(x => x.asInstanceOf[general_op].optype)
                DEBUG(s"object $optype - You called '$name' method with " +
                s"following arguments: ${names mkString ", "}")
                // stack = stack.push(optype)
                // stack = stack.push(name)
                // stack = stack.push(names(0))
                prog += optype
                prog += name
                prog += names(0)
            } catch {
                case ex: java.lang.ClassCastException => {
                    DEBUG("caught exception! " + ex)
                    //stack = stack.push(optype)
                    //stack = stack.push(name)
                    prog += optype 
                    prog += name
                    if (name != "end") {
                        val strings = args.map(x => x.asInstanceOf[String])
                        prog += strings(0)
                    }
                 }
            }
            new general_op
        }

        def updateDynamic(name: String)(value: Any) {
            DEBUG("in updateDynamic")
            optype = value.asInstanceOf[String]
        }

        def apply(other: Int) {
            DEBUG("did we get here?");
            //optype = other.toString;
            new general_op
        }

        def apply(other: Symbol) {
            println("pls work...");
            //optype = other.toString;
            new general_op
        }
    }

    object printstack extends general_op {
        this.optype = "printstack"
    }
    object dup extends general_op {
        this.optype = "dup"
    }
    object swap extends general_op {
        this.optype = "swap"
    }
    object drop extends general_op {
        this.optype = "drop"
    }
    object over extends general_op {
        this.optype = "over"
    }
    object dot extends general_op {
        this.optype = "dot"
    }
    object emit extends general_op {
        this.optype = "emit"
    }
    object cr extends general_op {
        this.optype = "cr"
    }
    object rot extends general_op {
        this.optype = "rot"
    }

    object add extends general_op {
        this.optype = "add"
    }

    object sub extends general_op {
        this.optype = "sub"
    }
    object mul extends general_op {
        this.optype = "mul"
    }
    object div extends general_op {
        this.optype = "div"
    }

    object mod extends general_op {
        this.optype = "mod"
    }

    object lt extends general_op {
        this.optype = "lt"
    }

    object gt extends general_op {
        this.optype = "gt"
    }

    object eql extends general_op {
        this.optype = "eql"
    }

    object and extends general_op {
        this.optype = "and"
    }

    object or extends general_op {
        this.optype = "or"
    }

    object invert extends general_op {
        this.optype = "invert"
    }

    object iff extends general_op {
        this.optype = "iff"
    }

    object then extends general_op {
        this.optype = "then"
    }

    object elsef extends general_op {
        this.optype = "elsef"
    }

    object DO extends general_op {
        this.optype = "DO"
    }

    object LOOP extends general_op {
        this.optype = "LOOP"
    }

    object LOOPADD extends general_op {
        this.optype = "LOOP"
    }

    object i extends general_op {
        this.optype = "i"
    }

    object j extends general_op {
        this.optype = "j"
    }

    object begin extends general_op {
        this.optype = "begin"
    }

    object until extends general_op {
        this.optype = "until"
    }

    object variable extends general_op {
        this.optype = "variable"
    }

    object constant extends general_op {
        this.optype = "constant"
    }

    object allot extends general_op {
        this.optype = "allot"
    }
    
    object key extends general_op {
        this.optype = "key"
    }

    object defn extends general_op {
        this.optype = "defn"
    }

    object enddef extends general_op {
        this.optype = "enddef"
    }

    object end extends general_op {
        this.optype = "end"
    }

    object lastkey extends general_op {
        this.optype = "lastkey"
    }

    object sleep extends general_op {
        this.optype = "sleep"
    }

    object rand extends general_op {
        this.optype = "rand"
    }

    object emitstr extends general_op {
        this.optype = "emitstr"
    }

    object vwrite extends general_op {
        this.optype = "vwrite"
    }
    
    object vread extends general_op {
        this.optype = "vread"
    }

    def num(num: Int): general_op = {
        var x = new general_op
        x.optype = num.toString
        x
    }

    def s(st: String): general_op = {
        var x = new general_op
        x.optype = st
        x
    }

    def run() {
        (new ForthInterpreter(prog.toList.filter(x => (x != "" && x != "s")))).execute()
    }

    implicit class symbolconv(s:Symbol) {
        var x = new general_op
        x.optype = s.name
        x
    }

    def main(args: Array[String]): Unit = {

            defn s("convert-x-y") num(24) s("cells") mul add enddef
            defn s("draw") s("convert-x-y") s("graphics") add vwrite enddef
            defn s("draw-white") num(1) rot rot s("draw") enddef
            defn s("draw-black") num(0) rot rot s("draw") enddef
            variable s("snake-x-head") num(500) cells "allot"
            variable s("snake-y-head") num(500) cells "allot"
            variable s("apple-x")
            variable s("apple-y")
            num(0) constant s("left")
            num(1) constant s("up")
            num(2) constant s("right")
            num(3) constant s("down")
            num(24) constant s("width")
            num(24) constant s("height")

            variable s("direction")
            variable s("length")

            defn s("snake-x") cells s("snake-x-head") add enddef

            defn s("snake-y") cells s("snake-y-head") add enddef
            
            defn s("draw-walls")
            s("width") num(0) DO
            i num(0) s("draw-black")
            i s("height") num(1) sub s("draw-black") LOOP
            s("height") num(0) DO
            num(0) i s("draw-black")
            s("width") num(1) sub i s("draw-black")
            LOOP enddef



            defn s("initialize-snake")
            num(4) s("length") vwrite
            s("length") vread num(1) add num(0) DO
            num(12) i sub i s("snake-x") vwrite
            num(12) i s("snake-y") vwrite LOOP
            s("left") s("direction") vwrite enddef

            defn s("set-apple-position") s("apple-x") vwrite s("apple-y") vwrite enddef
            defn s("initialize-apple") num(4) num(4) s("set-apple-position") enddef

            defn s("initialize")
            s("width") num(0) DO
            s("height") num(0) DO
                j i s("draw-white")
            LOOP LOOP
            s("draw-walls") s("initialize-snake")
            s("initialize-apple") enddef

            defn s("move-up") num(-1) s("snake-x-head") vread  add s("snake-x-head") vwrite enddef
            defn s("move-left") num(-1) s("snake-y-head") vread add s("snake-y-head") vwrite enddef
            defn s("move-down") num(1) s("snake-x-head") vread add s("snake-x-head") vwrite enddef
            defn s("move-right") num(1) s("snake-y-head")  vread add s("snake-y-head") vwrite enddef

            defn s("move-snake-head") s("direction") vread
            s("left") over eql iff s("move-left") elsef
            s("up") over eql iff s("move-up") elsef
            s("right") over eql iff s("move-right") elsef
            s("down") over eql iff s("move-down")
            then then then then drop enddef

            defn s("move-snake-tail") num(0) s("length") vread DO
            i s("snake-x") vread i num(1) add s("snake-x") vwrite
            i s("snake-y") vread i num(1) add s("snake-y") vwrite
            num(-1) LOOPADD enddef

            defn s("is-horizontal") s("direction") vread dup
            s("left") eql swap
            s("right") eql or enddef

            defn s("is-vertical") s("direction") vread dup
            s("up") eql swap
            s("down") eql or enddef

            defn s("turn-up") s("is-horizontal") iff s("up") s("direction") vwrite then enddef
            defn s("turn-left") s("is-vertical") iff s("left") s("direction") vwrite then enddef
            defn s("turn-down") s("is-horizontal") iff s("down") s("direction") vwrite then enddef
            defn s("turn-right") s("is-vertical") iff s("right") s("direction") vwrite then enddef

            defn s("change-direction")
            num(37) over eql iff s("turn-left") elsef
            num(38) over eql iff s("turn-up") elsef
            num(39) over eql iff s("turn-right") elsef
            num(40) over eql iff s("turn-down")
            then then then then drop enddef

            defn s("check-input")
            s("last-key") vread s("change-direction")
            num(0) s("last-key") vwrite enddef

            defn s("random-position")
            s("width") num(4) sub rand num(2) add enddef

            defn s("move-apple")
            s("apple-x") vread s("apple-y") vread s("draw-white")
            s("random-position") s("random-position")
            s("set-apple-position") enddef

            defn s("grow-snake") num(1) s("length") vread add s("length") vwrite enddef

            defn s("check-apple")
            s("snake-x-head") vread s("apple-x") vread eql
            s("snake-y-head") vread s("apple-y") vread eql
            and iff
            s("move-apple") s("grow-snake")
            then enddef

            defn s("check-collision") s("snake-x-head") vread s("snake-y-head") vread
            s("convert-x-y") s("graphics") add vread num(1) eql enddef

            defn s("draw-snake")
            s("length") vread num(0) DO
            i s("snake-x") vread i s("snake-y") vread s("draw-black") LOOP
            s("length") vread s("snake-x") vread
            s("length") vread s("snake-y") vread
            s("draw-white") enddef

            defn s("draw-apple")
            s("apple-x") vread s("apple-y") vread s("draw-black") enddef

            defn s("game-loop")
            begin printstack
            s("draw-snake") s("draw-apple")
            num(100) sleep
            s("check-input") s("move-snake-tail")
            s("move-snake-head") s("check-apple")
            s("check-collision") until
            emitstr s(" Game, Over") enddef

            defn s("start") s("initialize") s("game-loop") enddef s("start")

        run()
    }
}