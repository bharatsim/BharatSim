package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.custom.GraphProviderWithBufferImpl

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = GraphProviderWithBufferImpl()

  def get: GraphProvider = graphProvider

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}
