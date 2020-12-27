package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.custom.GraphProviderImpl

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = new GraphProviderImpl()

  def get: GraphProvider = graphProvider

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}
