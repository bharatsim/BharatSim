package com.bharatsim.engine.graph.neo4j

class VariableGenerator(letter: Char, number: Int) {
  def generate: String = s"$letter$number"

  def next: VariableGenerator = {
    val nextLetter = if(letter == 'z') 'a' else (letter.toInt + 1).toChar
    val nextNumber = if(letter == 'z') number + 1 else number

    new VariableGenerator(nextLetter, nextNumber)
  }
}

object VariableGenerator {
  def apply(): VariableGenerator = new VariableGenerator('a', 0)
}