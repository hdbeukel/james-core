JAMES Core Module Changes
=========================

Version 1.2 (under development)
-------------------------------

 - ...

Version 1.1 (11/07/2015)
------------------------

 - Replaced abstract class `AbstractProblem` with concrete class `GenericProblem`.
   The former class had a single abstract method, responsible for generating random
   solutions. This has been replaced with delegation through a newly defined
   `RandomSolutionGenerator` interface. It is now possible (and preferred) to
   model custom problems by plugging in the data, objective, constraints (if any)
   and random solution generator in a `GenericProblem`, avoiding the need to extend
   and abstract problem class. The predefined `SubsetProblem` includes a default,
   but now easily customizable, random subset solution generator.
 - Added a constructor to `SubsetProblem` that allows to easily create a subset
   problem without subset size limits.
 - Reordered arguments in `SubsetProblem` constructors.
 - Various improvements.

Version 1.0 (17/06/2015)
------------------------

 - Dedicated customizable random generator per search.
 - Reduced overhead of checking stop criteria.
 - Various improvements and minor bug fixes.
 - Improved test coverage.
 - Cleaned up log messages.
 - Moved SearchFactory interface from extensions to core module. Grouped various
   factory interfaces in package org.jamesframework.core.factory.
 - Moved to SLF4J API 1.7.12.

Version 0.2 (12/11/2014)
------------------------

 - Added support for efficient delta evaluations and validations.
 - Provided additional subset neighbourhoods.
 - Added option to sort IDs of selected items in a subset solution using a
   custom comparator.
 - Renamed SubsetData to IntegerIdentifiedData.
 - Renamed ProblemWithData to AbstractProblem.
 - Refactored abstract Solution class.
 - Reorganized package structure.
 - Removed SubsetProblem interface and renamed SubsetProblemWithData to SubsetProblem.
 - Removed MinMaxObjective.
 - Removed EmptySearchListener, LocalSearchListener and EmptyLocalSearchListener.
   Default empty implementations of all callbacks are now directly provided in the
   SearchListener interface.
 - Various code simplifications and optimizations (e.g. using functional operations).
 - Minor bug fixes and improvements.
 - Moved to SLF4J API 1.7.7.
 - Moved to Java 8.


Version 0.1 (25/06/2014)
------------------------

 - First stable release of the JAMES Core Module.
