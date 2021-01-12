package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.custom.{GraphProviderImpl, GraphProviderWithBufferImpl}

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = new GraphProviderWithBufferImpl()

  def get: GraphProvider = graphProvider

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}
