package interpreter

import javafx.embed.swing.JFXPanel
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.stage.Stage
import scalafx.beans.property.DoubleProperty.sfxDoubleProperty2jfx
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Stop.sfxStop2jfx
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.Rectangle
import scalafx.scene.{Group, Scene}
import scalafx.application.Platform
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}



class ForthInterpreter(prog: List[String]) {
    val debug_enabled = true    
    def DEBUG(s: Any) {
        if (debug_enabled) {
            println(s)
        }
    }
    implicit def bool2int(b:Boolean) = if (b) 1 else 0

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

    val GRAPHICS_DIM = 192
    val graphics_size = GRAPHICS_DIM * GRAPHICS_DIM
    val GRAPHICS_CHUNKS = 24
    val scaleFactor = GRAPHICS_DIM / GRAPHICS_CHUNKS
    val GRAPHICS_START = MEM_CELLS / 2

    var lastKeyPressed = 0
    var lastKeyAddr = GRAPHICS_START - CELL_SIZE
    constants.put("graphics", GRAPHICS_START)
    variables_to_addr.put("last-key", lastKeyAddr)


    new JFXPanel()
    val canvas = new Canvas(200, 200)

    Platform.runLater {
        val dialogStage = new Stage {
            title = "TestStage"
            val rootPane = new Group
            rootPane.children = List(canvas)
            rootPane.setFocusTraversable(true)
            scene = new Scene(400, 400) {
                root = rootPane
                onKeyPressed = (k: KeyEvent) => {
                    k.code match {
                        case KeyCode.A => memory(lastKeyAddr) = 37
                        case KeyCode.W => memory(lastKeyAddr) = 38
                        case KeyCode.D => memory(lastKeyAddr) = 39
                        case KeyCode.S => memory(lastKeyAddr) = 40
                        case _ => 
                    }
                    println("lastKeyPressed in handler: " + memory(lastKeyAddr))
                }
            }
        }
        dialogStage.showAndWait()
        Platform.exit()
    }

    val gc = canvas.graphicsContext2D

    canvas.translateX = 150
    canvas.translateY = 150

    
    def updateGraphics(addr: Int) {
        val i = addr - GRAPHICS_START

        val x = i / GRAPHICS_CHUNKS
        val y = i % GRAPHICS_CHUNKS

        var color = Color.BLACK
        if (memory(addr) > 0) {
            color = Color.WHITE
        }
        gc.fill = color
        gc.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor, scaleFactor)
    }
    def execute() {
        var pc = 0
        while (pc < program.length) {
            val current_token = program(pc)
            pc = executeToken(program(pc), pc)
        }
    }

    def dumpVariables() {
        for ((k,v) <- variables_to_addr) {
            printf("var: %s $k val: %d \n", k, memory(v))
        }
    }

    def executeToken(token: String, pc: Int): Int = {
        if (conditional_stack.length > 0 && token != "then" && token != "iff" && token != "elsef") {
            val (condtype, condval) = conditional_stack.top
            if ((condtype == "iff" && condval < 1) || (condtype == "elsef" && condval < 1)) {
                return pc + 1
            }
        }
        val current_token = program(pc)

        DEBUG(s"Executing token $current_token at $pc.")

        val is_num = token.forall(_.isDigit) ||
            (token.substring(1, token.length).forall(_.isDigit) && token(0) == '-')
        if (is_num) {
            stack.push(token.toInt)
            return pc + 1
        }
        token match {
            case "printstack" =>  println(stack)
            case "dup" => stack.push(stack.top)
            case "drop" => stack.pop
            case "over" => {
                val x = stack.pop
                val y = stack.top
                stack = stack.push(x)
                stack = stack.push(y)
            }
            case "dot" => print(stack.pop)
            case "emit" => print(stack.pop.toChar)
            case "cr" => println("")
            case "swap" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(x)
                stack.push(y)
            }
            case "rot" => {
                val x = stack.pop
                val y = stack.pop
                val z = stack.pop
                stack.push(y)
                stack.push(x)
                stack.push(z)
            }
            case "add" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(x + y)
            }
            case "sub" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(y - x)
            }
            case "mul" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(x * y)
            }
            case "div" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(y / x)
            }
            case "mod" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(y % x)
            }
            case "lt" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(y < x)
            }
            case "gt" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(y > x)
            }
            case "eql" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(x == y)
            }

            case "and" => {
                val x = stack.pop
                val y = stack.pop
                stack.push((x ==1) && (y == 1))
            }

            case "or" => {
                val x = stack.pop
                val y = stack.pop
                stack.push(x > 0 || y > 0)
            }
            case "invert" => {
                val x = stack.pop
                stack.push(!(x>0))
            }
            case "defn" => {
                val function_name = program(pc + 1)
                val function_end = program.indexOf("enddef", pc + 1)
                functions.put(function_name, pc + 2)
                return function_end + 1
            }
            case "end" => {}
            case "lol" => {}
            case "emitstr" => {
                print(program(pc + 1))
                return pc + 2
            }
            case "iff" => {
                DEBUG("before iff: " + conditional_stack)
                if (conditional_stack.length > 0) {
                    val (condtype, condval) = conditional_stack.top
                    if (condval < 1) {
                        conditional_stack.push(("iff", -1))
                        return pc + 1
                    }
                }
                val x = stack.pop
                conditional_stack.push(("iff", x))
            }
            case "elsef" => {
                DEBUG("before elsef: " + conditional_stack)
                val (condtype, condval) = conditional_stack.top
                if (condtype != "iff") {
                    throw new Exception("reached elsef with no if!")
                }
                if (condval == -1) {
                    conditional_stack.push(("elsef", -1))
                } else {
                    conditional_stack.push(("elsef", condval < 1))
                }
            }
            case "then" => {
                DEBUG("before then: " + conditional_stack)
                val (condtype, condval) = conditional_stack.pop
                if (condtype == "elsef") {
                    val (condtype, condval) = conditional_stack.top
                    if (condtype == "iff") {
                        conditional_stack.pop
                    } else {
                        throw new Exception("Invalid order of iff/elsef!")
                    }
                } else if (condtype != "iff") {
                    throw new Exception("then without a preceding iff!")
                }
            }
            case "key" => stack.push(scala.io.StdIn.readChar())
            case "enddef" => {
                DEBUG(conditional_stack)
                val (jumptype, newpc) = conditional_stack.pop
                if (jumptype != "funccall") {
                    DEBUG(program(pc-2), program(pc-1), program(pc), program(pc + 1), program(pc + 2))
                    throw new Exception("enddef when not in a function call!")
                }
                return newpc
            }
            case "DO" => {
                val start = stack.pop
                val end = stack.pop
                // TODO: look into named tuples or something similar
                do_loop_stack.push((start, end))
                conditional_stack.push(("DO", pc + 1))
            }
            case "i" => {
                if (do_loop_stack.length > 1){
                    var (loop_count, end) = do_loop_stack.pop
                    var (loop_count2, end2) = do_loop_stack.pop
                    do_loop_stack.push((loop_count2, end2))
                    do_loop_stack.push((loop_count, end))
                    stack.push(loop_count2)
                    return pc + 1
                }
                val (loop_count, end) = do_loop_stack.top
                stack.push(loop_count)
            }
            case "j" => {
                val (loop_count, end) = do_loop_stack.top
                stack.push(loop_count)
            }
            case "LOOP" => {
                DEBUG(conditional_stack)
                val (condtype, condval) = conditional_stack.pop
                val (loop_count, end) = do_loop_stack.pop
                if (loop_count != end - 1) {
                    conditional_stack.push((condtype, condval))
                    do_loop_stack.push((loop_count + 1, end))
                    return condval
                } 
            }
            case "LOOPADD" => {
                DEBUG(conditional_stack)
                val (condtype, condval) = conditional_stack.pop
                val (loop_count, end) = do_loop_stack.pop
                val amt = stack.pop
                if (math.abs(loop_count - end) > 0){
                    conditional_stack.push((condtype, condval))
                    do_loop_stack.push((loop_count + amt, end))
                    return condval
                } 
            }

            case "begin" => conditional_stack.push(("begin", pc + 1))
            case "until" => {
                val x = stack.pop
                val (condtype, condval) = conditional_stack.pop
                if (condtype != "begin") {
                    throw new Exception("until with no begin!")
                }
                if (x != 0) {
                    conditional_stack.push((condtype, condval))
                    return condval
                }
            }
            case "constant" => {
                val x = stack.pop
                val name = program(pc + 1)
                // TODO: have a better way of indicating this
                constants.put(name, x)
                return pc + 2
            }
            case "variable" => {
                val name = program(pc + 1)
                val addr = free_index
                free_index += CELL_SIZE
                // TODO: dumpVariables()funchave a better way of indicating this
                variables_to_addr.put(name, addr)
                return pc + 2
            }
            case "vread" => {
                val addr = stack.pop
                stack.push(memory(addr))
            }

            case "vwrite" => {
                val addr = stack.pop
                val write_value = stack.pop
                memory(addr) = write_value
                if (addr >= GRAPHICS_START && addr <= GRAPHICS_START + graphics_size) {
                    updateGraphics(addr);
                }
            }
            case "allot" => {
                var length = stack.pop
                free_index += length * CELL_SIZE
            }
            // TODO: NOT SURE IF THIS IS CORRECT
            case "cells" => {
                var x = stack.pop
                stack = stack.push(x * CELL_SIZE)
            }

            case "sleep" => {
                Thread sleep stack.pop
            }

            case "rand" => {
                val r = scala.util.Random
                stack.push(r.nextInt(stack.pop))
            }
            case _ => {
                val funcdef = functions.get(token)
                val const = constants.get(token)
                val varaddr = variables_to_addr.get(token)

                if (funcdef.isDefined) {
                    conditional_stack.push(("funccall", pc + 1))
                    println("Calling function: " + token)
                    dumpVariables()
                    return funcdef.get
                }
                else if (const.isDefined) {
                    stack.push(const.get)
                    return pc + 1
                }
                else if (varaddr.isDefined) {
                    stack.push(varaddr.get)
                    return pc + 1
                } else {
                    throw new Exception("unrecognized token: " + token)
                }
            }
        }
        pc + 1
    }

    def getStackState(): List[Int] = {
        stack.toList
    }
}