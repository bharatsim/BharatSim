package com.bharatsim.engine.distributed.engineMain
import java.util

import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import com.bharatsim.engine.distributed.DBBookmark
import com.bharatsim.engine.distributed.engineMain.Barrier.{
  BarrierAborted,
  BarrierFinished,
  NotifyOnBarrierFinished,
  Stop,
  WorkErrored,
  WorkFinished
}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class BarrierTest extends AnyFunSuite with BeforeAndAfterAll with Matchers {

  test("should finish barrier with results when on all the work is done ") {

    val inbox = TestInbox[Barrier.Reply]()
    val testKit = BehaviorTestKit(Barrier(0, 2, inbox.ref))

    val res1 = DBBookmark(util.Set.of("1"))
    testKit.run(WorkFinished(Some(res1)))
    testKit.isAlive shouldBe true
    inbox.hasMessages shouldBe false

    val res2 = DBBookmark(util.Set.of("2"))

    testKit.run(WorkFinished(Some(res2)))
    testKit.isAlive shouldBe false
    val msg = inbox.receiveMessage().asInstanceOf[BarrierFinished]
    msg.bookmarks should contain only (res1, res2)
  }

  test("should finish without results when on all the work is done ") {
    val inbox = TestInbox[Barrier.Reply]()
    val testKit = BehaviorTestKit(Barrier(0, 2, inbox.ref))

    testKit.run(WorkFinished())
    testKit.isAlive shouldBe true
    inbox.hasMessages shouldBe false

    testKit.run(WorkFinished())
    testKit.isAlive shouldBe false
    val msg = inbox.receiveMessage().asInstanceOf[BarrierFinished]
    msg.bookmarks shouldBe empty
  }

  test("should notify barrier finish to all the additional listeners") {
    val inbox = TestInbox[Barrier.Reply]()
    val additionalListener1 = TestInbox[Barrier.BarrierFinished]()
    val additionalListener2 = TestInbox[Barrier.BarrierFinished]()
    val testKit = BehaviorTestKit(Barrier(0, 1, inbox.ref))

    testKit.run(NotifyOnBarrierFinished(additionalListener1.ref))
    testKit.run(NotifyOnBarrierFinished(additionalListener2.ref))

    testKit.run(WorkFinished())

    additionalListener1.expectMessage(Barrier.BarrierFinished(List.empty))
    additionalListener2.expectMessage(Barrier.BarrierFinished(List.empty))
  }

  test("should stop on Stop message") {
    val inbox = TestInbox[Barrier.Reply]()
    val testKit = BehaviorTestKit(Barrier(0, 10, inbox.ref))
    testKit.run(Stop())
    testKit.isAlive shouldBe false
  }

  test("should Abort the barrier when WorkErrored is reported") {
    val inbox = TestInbox[Barrier.Reply]()
    val testKit = BehaviorTestKit(Barrier(0, 10, inbox.ref))
    val error = "Error"
    testKit.run(WorkErrored(error, testKit.ref))
    inbox.expectMessage(BarrierAborted(error, testKit.ref))
    testKit.isAlive shouldBe false
  }

}
