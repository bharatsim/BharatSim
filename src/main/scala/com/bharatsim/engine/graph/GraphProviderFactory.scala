package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.custom.GraphProviderImpl

private[engine] object GraphProviderFactory {
  private val graphProvider = new GraphProviderImpl()
  def get: GraphProvider = graphProvider
}
