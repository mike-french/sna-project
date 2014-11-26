#### Degree Distributions for Small Graphs

Final Project - Social Network Analysis - Coursera    
_Mike French_     
_December 2013_

**Abstract**

We enumerate all non-isomorphic simple undirected graphs on _n_ nodes. We calculate their degree distributions and find sets of graphs that have the same histogram (equivalence classes). We show that histograms uniquely determine graphs for _n=2,3,4_. We find the first ambiguous cases of non-isomorphic graphs with the same histogram for _n=5_, and show they are homeomorphic (topologically equivalent). We then find many graphs for _n=6_ that have the same histogram, but are topologically distinct.

**Download**

_Degree Distributions for Small Graphs_, Mike French, December 2013 \[[pdf](../../raw/master/SNA-Project-DegreeDistributions.pdf)\]

**Dependencies**

You must install [GraphViz](http://www.graphviz.org/) and update your `PATH` to include the `bin` directory.

**Build & Run**

If you are building with the [Scala Build Tool](http://www.scala-sbt.org/), just type: `sbt test`   
The output DOT, PNG, HTML files will be written to the `dot` subdirectory.      
The tables for _n=1,2,3,4,5_ will be written quite quickly, _n=6_ case will take a bit longer.     
Click on one of the HTML files, such as `g6table.html`, to see the result in your browser.

**Permissions**

The Scala code is open source, released under the [MIT License](LICENSE).     
The code and the paper are  _Copyright © 2013 Mike French_
