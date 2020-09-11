package com.bharatsim.engine.graph

private[engine] object GraphProviderFactory {
  private val graphProvider = new GraphProviderImpl()
  def get: GraphProvider = graphProvider
}
