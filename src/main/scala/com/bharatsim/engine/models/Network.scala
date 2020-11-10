package com.bharatsim.engine.models

/** Network is formed when two or more agent are connected with each other.
  * Network is a Hub Node that represents the connection between these agents.
  */
abstract class Network extends Node {

  /**
    * Contact probability of the Network defines how likely a given agent is going to come in contact with other agents.
    * @return a value Ranging 0 to 1.
    *        - 0 being Agent never comes in contact with each other.
    *        - 1 being Agent are always in contact with each other.
    */
  def getContactProbability(): Double
}
