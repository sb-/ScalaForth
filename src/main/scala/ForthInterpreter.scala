class ForthInterpreter(prog: List[String]) {
    val program = prog
    var stack = new scala.collection.mutable.Stack[Int]
    var conditional_stack = new scala.collection.mutable.Stack[(String, Int)]
    var do_loop_stack = new scala.collection.mutable.Stack[(Int, Int)]
    // Map from function to starting index of function body
    var functions = scala.collection.mutable.Map[String,Int]()
    var constants = scala.collection.mutable.Map[String, Int]()

    val MEM_CELLS = 1000000
    var memory = new Array[Int](MEM_CELLS)
    val CELL_SIZE = 1
    var free_index = 1000
    var variables_to_addr = scala.collection.mutable.Map[String, Int]()
    
    def execute() {
        var pc = 0
        while (pc < program.length) {
            val current_token = program(pc);
            pc = executeToken(program(pc), pc);
        }
    }

    def executeToken(token: String, pc: Int): Int = {
        if (conditional_stack.length > 1 && token != "THEN") {
            val (condtype, condval) = conditional_stack.top
            if ((condtype == "iff" && condval == 0 && token != "elsef") ||
                (condtype == "elsef" && condval == 0)) {
                return pc + 1
            }
        }
        val is_num = token.forall(_.isDigit)
        if (is_num) {
            stack.push(token.toInt)
            return pc + 1
        }
        return pc + 1
    }

    def getStackState(): List[Int] = {
        stack.toList
    }
}