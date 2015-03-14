Old model
=========

Creating models
---------------

@Submodel
Factor

Creating factors
----------------

@FactorArgument
@FactorArgument(makeStochastic=true)
Factor.getLogDensity()
@FactorComponent

Creating moves
--------------

@RandomVariable(mcmcMoves = {..})
@SampledVariable
@ConnectedFactor

Creating variables
------------------

Main things to do is creating move


Next changes
============

- Better Factor hierarchy (still keep root)
- Introduce @Factor, automatically recurse
- Create moves via constructors, 
- @RandomVariable -> @DefaultSamplers(..); [Other mechanism : later]
