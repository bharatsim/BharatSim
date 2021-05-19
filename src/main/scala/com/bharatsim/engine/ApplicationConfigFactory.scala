package com.bharatsim.engine

object ApplicationConfigFactory {
  private lazy val appConfig = new ApplicationConfig()
  private var testConfig: Option[ApplicationConfig] = None
  def config: ApplicationConfig = if (testConfig.isDefined) testConfig.get else appConfig

  private[engine] def testOverride(config: ApplicationConfig): Unit = {
    testConfig = Some(config)
  }
}
