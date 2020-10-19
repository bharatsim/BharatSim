package com.bharatsim.engine.testModels

case class SerdeExampleModel(
                              isSmart: Boolean,
                              singleByte: Byte,
                              gender: Char,
                              name: String,
                              age: Int,
                              height: Float,
                              prob: Double,
                              bankBalance: Long,
                              l: List[Int],
                              m: Map[String, Char]
                            )
