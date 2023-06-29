# BharatSim

This repository houses the source code for [BharatSim](https://bharatsim.ashoka.edu.in). 

BharatSim is a distributed, multi-scale, simulation framework for agent-based models. It was originally designed to model the epidemiological dynamics of the COVID-19 pandemic in India, including the social determinants of disease, but is designed to easily describe other communicable as well as non-communicable diseases. In addition to disease modelling, it may also be used to study a wide range of social phenomena.

BharatSim can be used to explore the potential impact of a range of different interventions on disease dynamics, including masking, social distancing, school closures, testing, contact tracing, different quarantining strategies, and even vaccination drives.

BharatSim was initially developed through a collaboration between [Ashoka University](https://www.ashoka.edu.in) and [Thoughtworks](https://www.thoughtworks.com/), funded by the [Bill & Melinda Gates Foundation](https://www.gatesfoundation.org/). The ongoing development of BharatSim at Ashoka University is funded by the [Mphasis F1 Foundation](https://www.mphasis.com/).

The original paper describing BharatSim is available [here](https://doi.org/10.1101/2023.06.15.23291292). The citation is:

> Cherian, P., Kshirsagar, J., Neekhra, B., Deshkar, G., Hayatnagarkar, H., Kapoor, K., Kaski, C., Kathar, G., Khandekar, S., Mookherje, S., Ninawe, P., Noronha, R. F., Ranka, P., Sinha, V., Vinod, T., Yadav, C., Gupta, D., & Menon, G. I. (2023). **BharatSim: An agent-based modelling framework for India**. MedRxiv. https://doi.org/10.1101/2023.06.15.23291292.

If you are using BharatSim in your research, consider starring this repository. This gives us an accurate lower bound of the number of people this project has helped.

Questions or comments can be directed to bharatsim@ashoka.edu.in, or on this project's [GitHub page](.). More information about BharatSim can be found in its [documentation](https://bharatsim.readthedocs.io).


## Setup and installation

The BharatSim framework is written in [Scala 2](https://www.scala-lang.org). Once the source code is obtained from this repository, a development environment needs to be set up. You can find instructions about doing this in the [Getting Started](https://bharatsim.readthedocs.io/en/latest/setup.html#) section of the [documentation](https://bharatsim.readthedocs.io).


## Learning to use BharatSim

The documentation comes with a short tutorial on [Writing your First Program](https://bharatsim.readthedocs.io/en/latest/firstprogram.html) which introduces the basic concepts of the simulation framework using instructions on how to write a simple SIR model in BharatSim. The documentation also contains a very basic introduction to modelling epidemics using agent-based models, explained in [A Basic Introduction to Epidemic Modelling](https://bharatsim.readthedocs.io/en/latest/epidemiology.html).


## Contributing to BharatSim

- Write to us at bharatsim@ashoka.edu.in.

#### **Have you managed to fix a bug or add a new feature?**
- Open a new GitHub pull request in this repository if you think your addition is worth being included.
- Ensure that the pull request description clearly describes the problem and solution or the new addition. Include the relevant issue number if applicable.

 
## Support

#### **Do you think you've found a bug in BharatSim?**

* **Ensure that this bug was not already reported** by searching on GitHub in [BharatSim issues](https://github.com/bharatsim/bharatSim-public/issues).
* If you're unable to find an open issue addressing your problem, open a new one in the corresponding repository. Be sure to include a **title and clear description**, as much relevant information as possible, and a **code sample** or a **test case** demonstrating the expected behavior that is not occurring.

#### **Do you have questions about your models?**

* If you have any about how to use BharatSim, first go through the documentation on the [website](http://bharatsim.ashoka.edu.in).
* If you need more assistance, write to us at bharatsim@ashoka.edu.in.


## License

[![CC BY-SA 4.0][cc-by-sa-shield]][cc-by-sa]

This work is licensed under a
[Creative Commons Attribution-ShareAlike 4.0 International License][cc-by-sa].

[![CC BY-SA 4.0][cc-by-sa-image]][cc-by-sa]

[cc-by-sa]: http://creativecommons.org/licenses/by-sa/4.0/
[cc-by-sa-image]: https://licensebuttons.net/l/by-sa/4.0/88x31.png
[cc-by-sa-shield]: https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg
