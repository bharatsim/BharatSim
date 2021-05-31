Welcome to the BharatSim Documentation!

## About BharatSim

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

#### Network
 
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
         creaetIntervention()
       
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
        
       private def creaetIntervention(implicit context: Context): Unit = {
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
   1.  `com.bharatsim.examples.epidemiology.sir` : Basic compartmental SIR model. It has two types of agents, employees and students. Each of the agent spends 12 hours at home and then 12 hours at school or office based on their type. The population is seeded with 1% infection and simulation continues until infected count becomes 0 or maximum number of steps as defined by application config, are reached. The output is written to output file specified while instantiating `CsvOutputGenerator`.  
    

## Synthetic population

## Visualizing model output

## Examples - User testimonials

## Help & FAQs

## Contributing to BharatSim

## Disclaimer

