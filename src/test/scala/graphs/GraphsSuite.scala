package graphs

//--------------------------------------------------------
// Copyright (c) 2013 Mike French
// This software is released under the MIT license
// http://www.opensource.org/licenses/mit-license.php
//--------------------------------------------------------

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GraphsSuite extends FunSuite {

  import Graphs._
  
  test("star 3") {
    assert( createStar(3).toString === "Graph "+Set(1,2,3)+" "+Set((1,2), (1,3)) )
  }
  
  test("line 3") {
    assert( createLine(3).toString === "Graph "+Set(1,2,3)+" "+Set((1,2), (2,3)) )
  }
  
  test("clique 3") {
    assert( createClique(3).toString === "Graph "+Set(1,2,3)+" "+Set((1,2), (1,3), (2,3)) )
  }
  
  test("edge order") {
    val g1 = new Graph().addEdge( 1, 2 ).addEdge( 2, 3 ).addEdge( 3, 4 )
    val g2 = new Graph().addEdge( 4, 3 ).addEdge( 3, 2 ).addEdge( 2, 1 )
    assert( g1.equals(g2) )
  }
  
  test("degree code") {
    assert( createLine(3).degreesCode   === "1,1,2" )
    assert( createClique(3).degreesCode === "2,2,2" )
    assert( createStar(4).degreesCode   === "1,1,1,3" )
  }
  
  test("identifier") {
    assert( createLine(3).generateId === "g3-21" )
    assert( createClique(3).generateId === "g3-03" )
  }
  
  test("morphism, 3") {
    
    val line = createLine( 3 )
    val star = createStar( 3 )
    
    // different in detail
    assert( !line.equals(star) )
    
    // the same abstract form
    val swap12 = createMorphism( List(1,2,3), List(2,1,3) )
    assert( line.isIsomorphism( swap12, star ) )
					  
  }
  
  test("connected") {
    
    var g = new Graph().addNode( 1 )
    assert( g.isConnected )
    g.addNode( 2 )
    assert( !g.isConnected )
    g.addEdge( 1, 2 )
    assert( g.isConnected )
    
    assert( createLine(3).isConnected )
    assert( createStar(4).isConnected )
    assert( createClique(5).isConnected )
  }
  
  test("not connected") {
    val g: Graph = new Graph
    g.addEdge( 1, 2 )
    assert( g.isConnected )
    g.addEdge( 3, 4 )
    assert( !g.isConnected )
    g.addEdge( 5, 6 )
    assert( !g.isConnected )
    g.addEdge( 2, 4 )
    assert( !g.isConnected )
    g.addEdge( 6, 4 )
    assert( g.isConnected )
  }
  
  test("degrees") {
    
    assert( new Graph().addEdge(1,2).histogram === Array[Int]( 2 ) )
    
    assert( createLine(3).histogram   === Array[Int]( 2, 1 ) )
    assert( createStar(3).histogram   === Array[Int]( 2, 1 ) )
    assert( createClique(3).histogram === Array[Int]( 0, 3 ) )
    
    assert( createLine(3).histogram.sameElements( createStar(3).histogram ) )
 
    assert( createLine(4).histogram   === Array[Int]( 2, 2, 0 ) )
    assert( createStar(4).histogram   === Array[Int]( 3, 0, 1 ) )
    assert( createClique(4).histogram === Array[Int]( 0, 0, 4 ) )
  }
  
  test("general graph") {
    assert( createGraph(3,3L) === createStar(3) )
    assert( createGraph(3,5L) === createLine(3) )
    assert( createGraph(3,7L) === createClique(3) )

    assert( createGraph(4,7L)  === createStar(4) )
    assert( createGraph(4,41L) === createLine(4) )
    assert( createGraph(4,63L) === createClique(4) )
  }
  
  test("all graphs") {
    //println( createAllGraphs(3) )
    //println( createConnectedGraphs(3) )
    //println( createUniqueGraphs(3) )
    
    assert( createAllGraphs(1).length === 1 )
    assert( createConnectedGraphs(1).length === 1 )
    assert( createUniqueGraphs(1).length === 1 )
    
    assert( createAllGraphs(2).length === 2 )
    assert( createConnectedGraphs(2).length === 1 )
    assert( createUniqueGraphs(2).length === 1 )
    
    assert( createAllGraphs(3).length === 8 )
    assert( createConnectedGraphs(3).length === 4 )
    assert( createUniqueGraphs(3).length === 2 )
    
    assert( createAllGraphs(4).length === 64 )
    assert( createConnectedGraphs(4).length === 38 )
    assert( createUniqueGraphs(4).length === 6 )
    
    assert( createAllGraphs(5).length === 1024 )
    assert( createUniqueGraphs(5).length === 21 )
  }
  
  test("dot") { 
    
    generateTable( "g2table", createUniqueGraphs(2) ) 
    generateTable( "g3table", createUniqueGraphs(3) ) 
    generateTable( "g4table", createUniqueGraphs(4) )
  //generateTable( "g5table", createUniqueGraphs(5) )
  //generateTable( "g6table", createUniqueGraphs(6) )
  }

}
