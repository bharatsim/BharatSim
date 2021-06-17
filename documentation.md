Welcome to the BharatSim Documentation!

## About BharatSim

To analyze and predict behaviour of complex events such as COVID19 pandemic, one needs an ability to create hypothetical scenarios and simulate them iteratively. Agent based modeling is a technique which is used to create such models. As the domain itself is complex, building & implementing these agent based models is even more challenging.
Good models would help us predict the timing of potential surges in health care requirements, making it easier to allocate resources in a timely manner. They would also enable us to compare the merits of different non-pharmaceutical interventions as the epidemic proceeds, while helping us understand, say, the role of underlying asymptomatic infections in spreading the disease.
Models developed for other countries will simply not take into account the many complexities such as its unique demographics, the patchy state of health care, stratified social structures and complex ecological gradients of India. The inputs that are required for such modelling must be collated from multiple sources, including imperfect and not easily usable government data, requiring both local knowledge and considerable experience in how to gather and interpret them. Hence the need for an epidemic modelling framework tailored to Indian needs, with potential applications beyond its immediate use for COVID-19 modelling.

BharatSim is a framework which helps model developers to build large scale agent based models considering the above challenges in the Indian context. BharatSim's vision is to build an India scale agent based framework that would enable modellers from various fields of study like epidemiology, disaster management, economics, etc to advise policy makers and institutions. BharatSim will allow researchers and modelers to build a distributed, multi-scale agent based simulation framework to help envision the disease spread of COVID19 in India, devise and visualize different interventions strategies in a way to advice policy makers for micro level decision making in controlling the epidemic. No other framework in India is generic enough and scalable enough in this context.

The framework has two parts:
1. Simulation engine - This component allows modeller to specify and simulate the model & is responsible for running the simulations.
2. Visualization engine - This component would accept Simulation engine output and would help users to visualize it by means of creating different graphs and charts.

#### Key features of BharatSim are:
BharatSim is network based simulation framework developed with three goals in mind which together makes it unique:
1. Flexibility: For being a social simulation framework, a researcher can develop models across disciplines such as epidemiology, economics, climate change, and so on,
2. Scalability: It can scale to millions of agents,
3. Localization: It is developed to suit India's needs, especially via its support for synthetic population.
    
## Background
Simulations help an intervention by giving it foresight — it allows administrators and policy-makers to test multiple scenarios without risk of consequences. Simulation as a technique has existed for decades. Based on the domain, the simulation model has to account for unique factors, and complex dynamics.<br/>
While differential equation-based models can accommodate the exponential nature of variables, they cannot explain the underlying dynamics and the impact of strategies. Also it’s difficult to model what-if and if-what scenarios using differential equations. <br/>
With an agent-based model, however, a policy maker can model a virtual world. A world where agents, representing entities like people, live out daily lives. Also called microsimulations, they allow researchers to specify richer details, thus ensuring extremely useful outcomes.<br/> 
For example, In an epidemic model, an individual could catch an infection while traveling in a bus, and a policy maker could subsequently propose decongesting public transport. A simulation model ought to capture this fact. But, equation-based models cannot process a diverse demography exhibiting a wide range of behaviors.

## Setup Requirements and Installation
1. Prerequisites
   * Setup scala and sbt
       https://docs.scala-lang.org/getting-started/index.html#install-scala
   * [Optional] Configure IntelliJ https://docs.scala-lang.org/getting-started/intellij-track/getting-started-with-scala-in-intellij.html#installation  
2. Code setup
    1. Clone the repository 
        ``git clone https://github.com/debayanLab/BharatSim``
    2. [Optional] Change the model as per requirement ``src/main/scala/com/bharatsim/model/`` 
    3. Compile the code ``sbt compile``   
    4. Run the code ``sbt run`` 
3. API Documentation
    1. Run ``sbt doc``
    2. Generated documentation should be in ``target/scala-2.13/api/index.html``
4. Package
    1. Run ``sbt assembly`` to create independent jar. 
    2. Generated jar should be at ``target/scala-2.13/engine-assembly-0.1.jar``
    3. The jar should be run as follows
        ``java -DSIMULATION_STEPS=100 -cp target/scala-2.13/engine-assembly-0.1.jar <the_main_class> ``.
        For ex. The main class can be com.bharatsim.examples.epidemiology.sir.Main

## Distributed Setup
There are two modes an application has to run in distributed setup

1. EngineMain
2. Worker

#### EngineMain
 The EngineMain plays coordinator role in distributed setup. hence, there must be only one instance of application running  as EngineMain role in the setup.

EngineMain performs following tasks:
   1. Distribution of work and coordination between of workers
   2. Ingestion of data
   3. Execution of Interventions
   4. Execution of Simulation Listeners

#### Workers
   Worker perform the majority of work needed for simulation execution. There can be multiple instances of worker in the setup.
   The worker executes all the Agents Behaviours and State transitions.


#### Database
   The Distributed setup uses [Neo4j](https://neo4j.com/) as database. The [Clustering](https://neo4j.com/docs/operations-manual/current/clustering/)  available in Enterprise version of Neo4j should be used for better performance. Though the Community and Desktop/developer version of Neo4j is also supported. 

#### Configurations
   The following configurations need to be set for application to run in distributed mode.
   * bharatsim.engine.execution="distributed"
   * ROLES.0="EngineMain" or ROLES.0="Worker"
   * HOSTNAME=<host_name_or_ip_address>
   * PORT=<port_for_application>
   * SEED_NODES.0="akka://Cluster@<host_name_or_ip_address_for_seed>:<port>" (Ip address of first node to start in the cluster)
   * bharatsim.engine.db.neo4j.uri="neo4j://<ip_address_of_neo4j>:<port_of_neo4j>
   * bharatsim.engine.db.neo4j.username=<neo4j_username>
   * bharatsim.engine.db.neo4j.password=<neo4j_password>

   Please take a look at `reference.conf` for additional configurations. These configurations are set as system property. For example,
   `Java -Dbharatsim.engine.execution="distributed" -DROLES.0="EngineMain -DHOSTNAME="localhost" -DPORT="25250" `
    

## Framework Key Concepts
#### Agent 
In agent-based modeling (ABM), a system is modeled as a collection of autonomous decision-making entities called **Agents**.

Agents in BharatSim Framework has two components.
* Agent **Attributes**:
    * **State** is set of value associated with an agent which will change over the course of simulation.
For ex. In Epidemiological Model infection status of an Agent (Susceptible, Infected, Recovered)

    * **Properties** is set of values associated with an agent which are constant over the course of simulation.
For ex. Name, Age of an Agent.

* Agent **Behaviour**:
       This is the behaviour that agent exhibit during simulation. For ex. let's say, Modeller is modelling a person who is student, then student would have behaviour patterns like going to school, playing with friend. etc.
Behaviours are executed on every step of the simulation.

#### Stateful Agent
Stateful Agents are agents but they follow Finite State Machine (FSM). There can be multiple states, with defined state transition. An agent can be in only one state at a time.
   * **enterAction**: This code would be executed only once, when an agent enters a state.
   * **perTickAction**: This code would be executed for all the ticks the agent is in this particular state.
   * **addTransition**: Defines what should be next state and then when the transition to the next step happen. In case of multiple transitions, the first one to evaluate to true is executed.

#### Network
Network is formed when two or more agents are connected with each other. For example, persons who stays at same place or goes to same office they will form a network.
Network has three component Agents, Network And Relation between an Agent and Network.
 
#### Schedule
   Schedules are the activities an Agent would perform at give point in time in the simulation.
   Using schedule, modeller can specify which Network that agent belongs to at any given point of time in the simulation.
#### Interventions
  Interventions are events which get activated when the provided condition is satisfied. 
- Each intervention is identified uniquely by the **name** of an intervention.
- Each intervention needs to have an **activation condition** and a **condition to deactivate**.
> activation condition and deactivation condition are `boolean` decisions
- Additionally modeller can define a **activation action** and **per tick action**; both of which are optional.
> activation action: This action is invoked only once, i.e, at the activation of the intervention.  
> per tick action: This action is invoked per tick for which an intervention is active.
    
#### Input
   The BharatSim Framework provides ways to ingest initial data (synthetic population) needed for simulation.
#### Output
   For generating output in CSV file format the BharatSim framework provides interface `CSVSpecs`.
    The Modeller has to implement this interface in order to specify **Headers** and **Rows** data.

## Order of Per tick executions

  1. `onStepStart` of all the `SimulationListener` from `SimulationListenerRegistry` in order of registration. 
  2. Intervention registered using `registerIntervention` in an order of intervention invocation.
  3. Agent behaviours which are registered using `addBehaviour` construct. In case of multiple behaviours, these would be executed in the order of declaration. In case of `StatefulAgent` it would be agents behaviours followed by state transition defined using `addTransition`.
  4. Actions which are registered in Main using `registerAction`. If there are multiple such actions registered, these would be executed in the order of declaration.
  5. `onStepEnd` of all the `SimulationListener` from `SimulationListenerRegistry` in order of registration.
 
## Quick Start Guide
1. Create Simulation instance : Simulation instance would be created with max number of ticks as specified in `bharatsim.engine.executionsimulation-steps` in `application.conf` file
    
    ```scala
       val simulation = Simulation()
    ```
2. Ingest csv data : CSV Import feature allows modeller to import data from CSVs. Data in CSV could be information about Agent, Networks and their Relations. API to load csv data is `ingestData` which invokes used defined mapping function with path to csv file and mapper function.
    ```scala
         simulation.ingestData(implicit context => {
             ingestCSVData("dummyData.csv", csvDataMapper)
        })
    ```
    Where `csvDataMapper` is defined as follows.
   
   ```scala
       def csvDataMapper(rowData: Map[String, String]): GraphData = {
   
            val agentId = rowData("id").toInt
            val age = rowData("age").toInt
    
            val person: Person = Person(agentId, age)
    
            val homeId = rowData("house_id").toInt
            val officeId = rowData("office_id").toInt
    
            val home = House(homeId)
            val office = Office(officeId)
    
            val staysAt = Relation[Person, House](agentId, "STAYS_AT", homeId)
            val worksAt = Relation[Person, Office](agentId, "WORKS_AT", officeId)
    
            val memberOf = Relation[House, Person](homeId, "HOUSES", agentId)
            val employeeOf = Relation[Office, Person](officeId, "EMPLOYEE_OF", agentId)
    
            val graphData = new GraphData()
            graphData.addNode(agentId, person)
            graphData.addNode(homeId, home)
            graphData.addNode(officeId, office)
    
            graphData.addRelations(staysAt, worksAt, memberOf, employeeOf)
    
            graphData
        }
   ```
3. Define Simulation : defineSimulation method helps to register specific actions like schedule creation, agent registration, defining interventions, simulation output, etc.
    ```scala
       simulation.defineSimulation(implicit context => {
         createSchedules()
         createIntervention()
       
         registerAction(
           StopSimulation,
           (c: Context) => {
             getInfectedCount(c) == 0
           }
         )

         registerAgent[Person]
   
         SimulationListenerRegistry.register(
           new CsvOutputGenerator("src/main/resources/output_" + currentTime + ".csv", new PersonCSVSpec(context))
         )
     })
    ``` 
   
4. Define and register schedule : Schedules are the activities an Agent would perform at give point in time in the simulation.
    1. Define and register Schedules : Schedule is defined as 8 Hours Home - Office - Home and is registered for all those agents which are of type `Person` and whose age is above 30 years. 
    ```scala
   private def createSchedules()(implicit context: Context): Unit = {
        val employeeSchedule = (Day, Hour)
                 .add[House](0, 8)
                 .add[Office](9, 18)
                 .add[House](19, 23)
        registerSchedules((employeeSchedule, (agent: Agent, context: Context) => agent.asInstanceOf[Person].age > 30))
   }
    ```

5. Define Simulation specific actions : The api accepts the action and the condition if to execute the action. The action would be invoked at each simulation tick for which the condition is true.
    ```scala
       registerAction(
           StopSimulation,
           (c: Context) => {
             getCurrentTick(c) == 100
           }
       )
    ```
      
6. Define and Register agent
    1. Define Agent :
             An agent can be defined by extending `Agent` class provided by framework.
             
        ```scala
           case class Person(id: Int, age: Int) extends Agent
        ```
        Behaviours of an agent can be defined using `addBehaviour` API.
            
        ```scala
           def work(context: Context): Unit ={
             updateParam("is_working", true)
           }
        
           addBehaviour(work)
        ```
    2. Register Agent
        ```scala
           registerAgent[Person]
        ```
       
7. Register simulation listeners : For generating output in CSV file format the BharatSim framework provides interface `CSVSpecs`.
    1. Define listener
    ```scala
        class PersonCSVSpec(context: Context) extends CSVSpecs {
    
          override def getHeaders(): List[String] = List("Step", "CountOfWorkingAgents")
    
          override def getValue(fieldName: String): Any = {
            fieldName match {
              case "Step" => context.getCurrentStep
              case "Count" => context.graphProvider.fetchNodes("Person", ("is_working", true)).size
             }
          }
        }
    ```
    2. Register listener
    ```scala
           SimulationListenerRegistry.register( new CsvOutputGenerator("output.csv", new StudentCSVSpec(context)) )
    ```
   
8. Define interventions : Interventions are events which get activated when the provided condition is satisfied.

    1. Create and register intervention
        ```scala
        import com.bharatsim.engine.intervention.Intervention
        
       private def createIntervention(implicit context: Context): Unit = {
            val shouldActivate = (context: Context) => context.getCurrentStep == 5
            val shouldDeactivate = (context: Context) => context.getCurrentStep == 10
            
            val intervention = Intervention("exampleIntervention", shouldActivate, shouldDeactivate)
       
            registerIntervention(intervention)
       }
        ```
       
9. Run the simulation

    ```scala
       Simulation.run()
    ```
10. Register Simulation termination actions like Clean up 
    ```scala 
        simulation.onCompleteSimulation { implicit context =>
              printStats(beforeCount)
              teardown()
            }
    ```

## Using examples
   Examples are listed in `com.bharatsim.examples` package. Each example can be executed by running `main` method from `Main.scala` class in each of the example. Details about each example are as follows:
   1.  `com.bharatsim.examples.epidemiology.sir` : Basic compartmental SIR model. It has two types of agents; employees and students. Each of the agent spends 12 hours at home and then 12 hours at school or office based on their type. The population is seeded with 1% infection and simulation continues until infected count becomes 0 or maximum number of steps as defined by application config are reached. The output is written to output file specified while instantiating `CsvOutputGenerator`.  
    

## Synthetic population

Synthetic population represents a set of synthetic agents that share a common geographic, social or biological characteristic. For example, we can have a synthetic population for a district and they would share Census measurements of that district.
Data and attributes of the synthetic agents are synthesised by integrating a diverse set of data sources(e.g., Census, IHDS, NSS etc.) and using models for interpolation and extrapolation of data.
These agents are “similar” to real individuals but not identical to any individual in the population. Thus individual attributes are based on real-world collected data. The correlations between the synthetic data sets “agree” with the measured correlations of the real world data.
Source: https://nssac.bii.virginia.edu/~swarup/synthetic_population_tutorial/AAMAS_2016_generating_synthetic_populations_for_social_modeling_full_tutorial.pdf

## Visualizing model output

Visualization engine/tool would accept Simulation engine output(csv) and would help users to visualize it by means of creating different charts and graphs. The tool allows user to visualize data using Line chart, bar chart and histogram. Along with these basic charts, the tool supports GIS data in the geoJson format and can plot heat map and choropleth. Further more, user can visualize these heat maps and choropleths across time dimension.

More details about visualization engine can be found here: https://github.com/debayanLab/BharatSim-Visualisation

## Examples - User testimonials

## Help & FAQs
1. What is PerTickCache and how to use it?
    
    PerTickCache is a Map based temporary data store, which is used as cache. Calls on data store are typically time and resource consuming and within a tick there's a chance that some of the calls would become redundent. For such calls, model developer can store the results in the PerTickCache and access it from there. If the key is not present then call to data store would get through and then result would be stored back in the cache. Model developer needs to make sure the keys are unique to avoid overriding the results. The PerTickCache is auto cleaned at the begining of the tick. Example: In epidemiology, if we are trying to compute who all might get infected based on their area(Home, office, etc.) the total number of infected in the area remains constant for a particular tick and same can be used for all susceptible agents.

2. Can I perform multiple updates on the node's property in a single tick?
    
    To create an effect of simultaneous updates and to remove path dependence, the framework uses double buffering in which an update on a Node is reflected at the tick end, and would become accessible on the next tick. Hence, if there are multiple updates happening on a same node property, last update would be synced to data store. Example: In epidemiology, if an agent of type Citizen has a property "infection_status" and within a tick i, if we change it to Exposed and later to Infected, the change would reflect as Infected on next tick which is i+1 and agent would never be in Exposed state.

## Troubleshooting
1. Compile time error with `Could not find implicit value for parameter [encoder],[decoder], [serializer], [deserializer]`: Import for default encoders and decoders are missing which needs to be added.
    ```scala
        import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
        import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
    ```
2. Getting `java.util.NoSuchElementException: None.get` exception while running the simulation model: There can be multiple reasons for the same but one such potential reason being the way relations are specified. e.x. if we are modeling `Person stays in House` use case, then we need to add two relations while csv ingestion, and one each in Person and House class.
    E.x. In data ingestion
    ```scala
        val staysAt = Relation[Person, House](citizenId, "STAYS_AT", homeId)
        val memberOf = Relation[House, Person](homeId, "HOUSES", citizenId)
        ...
        graphData.addRelations(staysAt, memberOf)
    ```
    In Person class:
    ```scala
        addRelation[House]("STAYS_AT")
    ```
    And similarly in House class:
    ```scala
        addRelation[Person]("HOUSES")
    ```

3. Getting `java.util.NoSuchElementException: key not found: [int]` exception: Please check the schedule definition. This exception can potentially be caused when schedule is not defined continuously and if there are empty slots in the schedule. e.x. 24 Hours schedule definition where there's slot 9 to 10 is missing.
      
   ```scala
          val employeeScheduleOnWeekDays = (Day, Hour)
             .add[House](0, 8)
             .add[Office](10, 18)
             .add[House](19, 23)
   ```
## Contributing to BharatSim

## Disclaimer

