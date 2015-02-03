JAMES Core Module
=================

[![Build Status](https://img.shields.io/travis/hdbeukel/james-core.svg?style=flat)](https://travis-ci.org/hdbeukel/james-core)
[![Coverage Status](http://img.shields.io/coveralls/hdbeukel/james-core.svg?style=flat)](https://coveralls.io/r/hdbeukel/james-core)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jamesframework/james-core/badge.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22james-core%22)

The JAMES core module is part of the [JAMES framework][james-github].

The core module includes many general components for both problem specification and search application.

A wide range of local search algorithms are provided out-of-the-box, including

1. Random descent (basic local search)
2. Steepest descent
3. Tabu search
4. Variable neighbourhood search
5. Metropolis search
6. Parallel tempering
7. ...

Exhaustive search is also available, which is of course only feasible for problems with a reasonably small search space.

In addition, the core contains specific components for subset selection such as a predefined solution type, a generic problem specification and various subset neighbourhoods, as well as a greedy subset sampling heuristic (LR subset search).

  
Documentation
=============

More information, user documentation and examples of how to use the framework are provided at the [website][james-website].
Additional developer documentation is posted on the [wiki][james-wiki].

License and copyright
=====================

The JAMES core module is licensed under the Apache License, Version 2.0, see LICENSE file or http://www.apache.org/licenses/LICENSE-2.0.
Copyright information is stated in the NOTICE file.

User forum
==========

Users may post questions on the [forum][james-forum]. Instructions for participating without a Google account are available at the [website][james-contact].

Developers
==========

The JAMES framework is developed and maintained by

 - Herman De Beukelaer (Herman.DeBeukelaer@UGent.be)
 
Please use the forum instead of directly mailing the developers whenever possible, so that others may benefit from or contribute to the discussion as well.
 
Changes
=======

A list of changes is provided in the CHANGES file.


[james-github]:   https://github.com/hdbeukel/james
[james-website]:  http://www.jamesframework.org
[james-wiki]:     http://github.com/hdbeukel/james/wiki
[james-forum]:    https://groups.google.com/forum/#!forum/james-users
[james-contact]:  http://www.jamesframework.org/contact/
