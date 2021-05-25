package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import com.bharatsim.engine.Dynamics
import com.bharatsim.engine.distributions.LogNormal

import scala.collection.immutable.HashMap

object Disease extends Dynamics {

  final val beta: Double = 0.5

  final val ageStratifiedBetaMultiplier = HashMap(
    9 -> 0.34,
    19 -> 0.67,
    29 -> 1.0,
    39 -> 1.0,
    49 -> 1.0,
    59 -> 1.0,
    69 -> 1.0,
    79 -> 1.24,
    89 -> 1.47,
    99 -> 1.47
  )
  final val ageStratifiedOneMinusGamma = HashMap(
    9 -> 0.5,
    19 -> 0.55,
    29 -> 0.6,
    39 -> 0.65,
    49 -> 0.7,
    59 -> 0.75,
    69 -> 0.8,
    79 -> 0.85,
    89 -> 0.9,
    99 -> 0.9
  )
  final val ageStratifiedOneMinusDelta = HashMap(
    9 -> 0.0005,
    19 -> 0.00165,
    29 -> 0.00720,
    39 -> 0.02080,
    49 -> 0.03430,
    59 -> 0.07650,
    69 -> 0.13280,
    79 -> 0.20655,
    89 -> 0.24570,
    99 -> 0.24570
  )
  final val ageStratifiedSigma = HashMap(
    9 -> 0.00002,
    19 -> 0.00002,
    29 -> 0.0001,
    39 -> 0.00032,
    49 -> 0.00098,
    59 -> 0.00265,
    69 -> 0.00766,
    79 -> 0.02439,
    89 -> 0.08292,
    99 -> 0.16190
  )

//  All the samples drawn from these distributions represent days
  final val exposedDurationProbabilityDistribution = LogNormal(4.5, 1.5)
  final val presymptomaticDurationProbabilityDistribution = LogNormal(1.1, 0.9)
  final val asymptomaticDurationProbabilityDistribution = LogNormal(8, 2)
  final val mildSymptomaticDurationProbabilityDistribution = LogNormal(8, 2)
  final val severeSymptomaticDurationProbabilityDistribution = LogNormal(1.5, 2.0)

  final val criticalSymptomaticDurationProbabilityDistribution = LogNormal(18.1, 6.3) // For hospitalized individuals

  final val dt = 1.toDouble / 2.toDouble
  final val inverse_dt = 2.toDouble / 1.toDouble

  //    TODO: Add the change to master - Jayanta / Philip
  final val vaccinationRate = 0.05
  final val vaccinatedGammaFractionalIncrease = 0.8
  final val fractionalTransmissionReduction = 0.2

  //  Does not have any effect, can be used to model reduced chances of catching an infection due to vaccination, but can be extended to masking or such other interventions
  final val vaccinatedBetaMultiplier = 1.0

}
