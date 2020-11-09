package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context
import com.bharatsim.engine.intervention.InterventionsTest.dummyIntervention
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InterventionsTest extends AnyWordSpec with Matchers {
  "add" should {
    "insert provided intervention in inactive interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)

      interventions.inactive.size shouldBe 1
      interventions.inactive.head.name shouldBe "DummyIntervention"
      interventions.active.size shouldBe 0
    }
  }

  "markActive" should {
    "insert the intervention in active interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)
      interventions.markActive(dummyIntervention)

      interventions.active.size shouldBe 1
      interventions.active.head.name shouldBe "DummyIntervention"
    }

    "remove the intervention from the inactive interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)

      interventions.inactive.size shouldBe 1

      interventions.markActive(dummyIntervention)

      interventions.inactive.size shouldBe 0
    }
  }

  "markInactive" should {
    "insert the intervention in inactive interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)
      interventions.markActive(dummyIntervention)

      interventions.inactive.size shouldBe 0
      interventions.markInactive(dummyIntervention)
      interventions.inactive.size shouldBe 1
      interventions.inactive.head.name shouldBe "DummyIntervention"
    }

    "remove the interventions from the active interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)
      interventions.markActive(dummyIntervention)

      interventions.active.size shouldBe 1
      interventions.markInactive(dummyIntervention)
      interventions.active.size shouldBe 0
    }
  }

  "activeNames" should {
    "return set containing names of the active interventions" in {
      val interventions = new Interventions
      interventions.add(dummyIntervention)
      interventions.markActive(dummyIntervention)

      val names = interventions.activeNames
      names.size shouldBe 1
      names.head shouldBe "DummyIntervention"
    }
  }
}

object InterventionsTest {
  private[InterventionsTest] val dummyIntervention = new Intervention {
    override def name: String = "DummyIntervention"

    override def shouldActivate(context: Context): Boolean = ???

    override def shouldDeactivate(context: Context): Boolean = ???

    override private[engine] def firstTimeAction(context: Context): Unit = ???

    override private[engine] def whenActiveAction(context: Context): Unit = ???
  }
}