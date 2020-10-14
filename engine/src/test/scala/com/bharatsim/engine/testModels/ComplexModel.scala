package com.bharatsim.engine.testModels

trait OccupationType

object Engineer extends OccupationType

object Teacher extends OccupationType

case class ComplexModel(name: String, occupation: OccupationType)
