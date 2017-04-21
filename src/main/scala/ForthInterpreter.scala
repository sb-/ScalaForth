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



class ForthInterpreter(prog: List[String]) {



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

    val GRAPHICS_DIM = 40
    val graphics_size = GRAPHICS_DIM * GRAPHICS_DIM
    val GRAPHICS_CHUNKS = 20
    val scaleFactor = GRAPHICS_DIM / GRAPHICS_CHUNKS
    val GRAPHICS_START = MEM_CELLS / 2
    constants.put("graphics", GRAPHICS_START)

    new JFXPanel()
    val canvas = new Canvas(200, 200)

    Platform.runLater {
        val dialogStage = new Stage {
            title = "TestStage"
            val rootPane = new Group
            rootPane.children = List(canvas)
            scene = new Scene(400, 400) {
                root = rootPane
            }
        }
        dialogStage.showAndWait()
        Platform.exit()
    }

    // canvas.translateX = 100
    // canvas.translateY = 100

    val gc = canvas.graphicsContext2D

    
    
    def updateGraphics() {
        val r = 0 to graphics_size
        r.foreach(i => {
            val x = i / GRAPHICS_CHUNKS
            val y = i % GRAPHICS_CHUNKS
            var color = Color.WHITE
            if (memory(GRAPHICS_START + i) > 0) {
                color = Color.BLACK
            }
            gc.fill = color
            gc.fillRect(x * scaleFactor, y * scaleFactor, x+scaleFactor, y+scaleFactor)
        })
    }
    def execute() {
        var pc = 0
        while (pc < program.length) {
            val current_token = program(pc)
            pc = executeToken(program(pc), pc)
            updateGraphics()
        }
    }

    def executeToken(token: String, pc: Int): Int = {
        if (conditional_stack.length > 0 && token != "then") {
            val (condtype, condval) = conditional_stack.top
            if ((condtype == "iff" && condval == 0 && token != "elsef") ||
                (condtype == "elsef" && condval == 0)) {
                return pc + 1
            }
        }
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
            case "emitstr" => {
                // TODO: print next token
            }
            case "iff" => {
                val x = stack.pop
                conditional_stack.push(("iff", x))
            }
            case "elsef" => {
                val (condtype, condval) = conditional_stack.top
                if (condtype != "iff") {
                    // TODO: throw some sort of exception
                }
                conditional_stack.push(("elsef", condval < 1))
            }
            case "then" => {
                val (condtype, condval) = conditional_stack.pop
                if (condtype == "elsef") {
                    val (condtype, condval) = conditional_stack.pop
                    // TODO: Throw an exception
                } else if (condtype != "iff") {
                    // TODO: Throw an exception
                }
            }
            case "key" => stack.push(scala.io.StdIn.readChar())
            case "enddef" => {
                val (jumptype, newpc) = conditional_stack.pop
                if (jumptype != "funccall") {
                    // TODO: throw an exception
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
                val (loop_count, end) = do_loop_stack.top
                stack.push(loop_count)
            }
            case "LOOP" => {
                val (condtype, condval) = conditional_stack.pop
                val (loop_count, end) = do_loop_stack.pop
                if (loop_count != end) {
                    conditional_stack.push((condtype, condval))
                    do_loop_stack.push((loop_count + 1, end))
                    return condval
                } 
            }
            case "begin" => conditional_stack.push(("begin", pc + 1))
            case "until" => {
                val x = stack.pop
                val (condtype, condval) = conditional_stack.pop
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
                // TODO: have a better way of indicating this
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
                if (funcdef.isDefined) {
                    conditional_stack.push(("funccall", pc + 1))
                    return funcdef.get
                }

                val const = constants.get(token)
                if (const.isDefined) {
                    stack.push(const.get)
                    return pc + 1
                }

                val varaddr = variables_to_addr.get(token)
                if (varaddr.isDefined) {
                    stack.push(varaddr.get)
                    return pc + 1
                }
            }
        }
        pc + 1
    }

    def getStackState(): List[Int] = {
        stack.toList
    }
}