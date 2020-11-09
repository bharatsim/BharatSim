package com.bharatsim.engine.intervention

import scala.collection.mutable

private[engine] class Interventions {
  private val inactiveInterventions: mutable.HashSet[Intervention] = mutable.HashSet.empty
  private val activeInterventions: mutable.HashSet[Intervention] = mutable.HashSet.empty

  private[engine] def add(i: Intervention): Unit = inactiveInterventions.addOne(i)

  private[engine] def markActive(i: Intervention): Unit = {
    inactiveInterventions.remove(i)
    activeInterventions.addOne(i)
  }

  private[engine] def markInactive(i: Intervention): Unit = {
    activeInterventions.remove(i)
    inactiveInterventions.add(i)
  }

  private[engine] def active: List[Intervention] = activeInterventions.toList

  private[engine] def inactive: List[Intervention] = inactiveInterventions.toList

  def activeNames: Set[String] = active.map(_.name).toSet
}
