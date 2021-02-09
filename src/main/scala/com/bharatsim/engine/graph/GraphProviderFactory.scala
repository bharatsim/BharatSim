package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.custom.BufferedGraphWithAutoSync

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = BufferedGraphWithAutoSync()

  def get: GraphProvider = graphProvider

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}
