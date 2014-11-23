package graphs

import scala.collection.mutable.Set
import scala.collection.mutable.HashSet
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.sys.process._
import java.io._

object Graphs {

  type Node     = Int
  type Edge     = (Node,Node)
  type Morphism = Map[Node,Node]
  
  class Graph( var id: Option[Long] = None ) { self =>
    
    val nodes: Set[Node] = new HashSet[Node]
    val edges: Set[Edge] = new HashSet[Edge]

    def nNodes(): Int = nodes.size
    def nEdges(): Int = edges.size
    
    def isEmpty: Boolean = (nNodes == 0)
    
    def addNode( node: Node ): Graph = {
      nodes += node
      clearCache
    }
    
    def addEdge( n1: Node, n2: Node ): Graph = {
      require( n1 != n2 )
      nodes += n1 += n2
      edges += (if (n1<n2) (n1,n2) else (n2,n1))
      clearCache
    }
      
    // memoization cache for degree array
    private var dhisto: Option[Array[Int]] = None
    private def clearCache: Graph = { dhisto = None; this }
    
    def histogram(): Array[Int] =
      dhisto match {
        case Some(darr) => darr
        case None => { dhisto = Some(calculateHistogram); histogram }
      }
    
    def histogramCode(): String = histogram.mkString("")

    private def nodeDegrees(): Map[Node,Int] = {
      new HashMap[Node,Int] { 
        for (n <- nodes) put( n, 0 ) 
        for ( (n1,n2) <- edges ) {  
          put( n1, this(n1) + 1 )
          put( n2, this(n2) + 1 )
        }
      }
    }
    
    private def calculateHistogram(): Array[Int] = 
      nodeDegrees.foldLeft( new Array[Int](nNodes-1) ){ 
        case (arr,(node,d)) => arr(d-1) += 1; arr 
      }
    
    private def degrees() = {
      nodeDegrees.foldLeft( List[Int]() ){ 
        case (ds,(node,d)) => d :: ds
      }.sorted
    }
    
    def degreesCode(): String = degrees.mkString( "", ",", "" )
    
    private def sameCounts( g: Graph ) = 
      (nNodes == g.nNodes) && 
      (nEdges == g.nEdges) && 
      histogram.sameElements( g.histogram )
    
    def morph( m: Morphism ): Graph = 
      new Graph { 
        for ( node   <- self.nodes) addNode( m(node) )
        for ((n1,n2) <- self.edges) addEdge( m(n1), m(n2) ) 
      }
    
    def isIsomorphism( m: Morphism, g: Graph ) = 
      if (!sameCounts(g)) false else morph( m ).equals( g )
      
    def isIsomorphic( gs: List[Graph] ): Boolean = 
      gs.exists( g => isIsomorphic(g) )
      
    def isIsomorphic( g: Graph ): Boolean = 
      if (!sameCounts(g)) false
      else if (equals(g)) true
      else {
        val nodelist = nodes.toList
        for {
          perm <- nodelist.permutations.toList
	                if !perm.equals( nodelist )
		              if isIsomorphism( createMorphism(nodelist,perm), g )
	      } return true 
	     false
      }
    
    def isConnected() = 
      if (nEdges < nNodes-1) false else (nComponents == 1)
      
    def nComponents(): Int =
      edges.foldLeft( new MergeSets(nodes) )( (ms,e) => ms.merge(e) ).count
    
    def generateId(): String = {
      "g" + nNodes + "-" + histogramCode + 
      (this.id match { case Some(i) => "-"+i; case None => ""})
    }
    
    override def equals( any: Any ): Boolean = 
      any match {
        case g: Graph => sameCounts(g) && nodes.equals(g.nodes) && edges.equals(g.edges)
        case _ => false
      }
    
    override def toString(): String = s"Graph $nodes $edges" 
  }
  
  private class MergeSets( val nodes: Set[Node] ) {
    
    private var sets = new HashSet[Set[Node]] { for (node <- nodes) add( HashSet(node) ) }
    
    def count(): Int = sets.size
    
    def merge( edge: Edge ): MergeSets = {
      // assumes edge is in graph (i.e. both nodes are present)
      val s1 = sets.find( s => s.contains(edge._1) ) match { case Some(s) => s }
      val s2 = sets.find( s => s.contains(edge._2) ) match { case Some(s) => s }
      if (s1 != s2) sets -= s1 -= s2 += (s1 ++ s2)
      this
    }
  }
  
  def createMorphism( src: List[Node], dst: List[Node] ): Morphism =
    new HashMap[Node,Node]() { for ( arrow <- (src zip dst) ) this += arrow }
  
  def createStar( n: Int ) = 
    new Graph { for (i <- 2 to n) addEdge( 1, i ) }
  
  def createLine( n: Int ) = 
    new Graph { for (i <- 1 until n) addEdge(i,i+1) }
  
  def createClique( n: Int ): Graph = 
    new Graph { for (i <- 1 until n; j <- (i+1) to n) addEdge(i,j) }
  
  def createGraph( n: Int, mask: Long ): Graph = 
    new Graph( Some(mask) ) {
      for (i <- 1 to n) addNode( i )
      var k = 0
      for (i <- 1 until n; j <- (i+1) to n) {
        if (((mask >> k) & 0x1) == 0x1) addEdge(i,j) 
        k += 1
      }
    }
  
  def createAllGraphs( n: Int ): List[Graph] =
    for (mask <- (0L until (1L << (n*(n-1)/2))).toList) yield createGraph( n, mask )
  
  def createConnectedGraphs( n: Int ): List[Graph] =
    createAllGraphs( n ).filter( g => g.isConnected )
  
  def createUniqueGraphs( n: Int ): List[Graph] = {
    val ghead :: gtail = createConnectedGraphs( n )
    gtail.foldLeft( List(ghead) ){ 
      (gs,g) => if (g.isIsomorphic(gs)) gs else g::gs 
    }.sortBy( _.histogramCode ).reverse
  }
  
  def generateTable( title: String, graphs: List[Graph] ) = {
    for (g <- graphs) generateImage( g )
    generateHTML( title, graphs )
  }
    
  def generateImage( g: Graph ) = {
    val dir  = "dot"
    val base = g.generateId
    val png  = new File( dir, base + ".png" )
    val dot  = new File( dir, base + ".dot" )
    if (png.exists) png.delete
    if (dot.exists) dot.delete
    val out = new PrintWriter( dot )
    toDot( out, g )
    out.close()
    val exitCode = Process( s"neato -Tpng -o${png} ${dot}" ).!
    if (exitCode != 0) println( "Process exit code: " + exitCode )
  }
  
  def toDot( out: PrintWriter, g: Graph ) {
    out.write( "graph G {\n" )
    out.write( "  size=\"0.8,0.8\";\n" )
    out.write( "  ratio=1;\n" )
    out.write( "  node [label=\"\",height=0.3,width=0.3,fixedsize=true,\n" )
    out.write( "        shape=circle,style=filled,\n" )
    out.write( "        fillcolor=\"#DCE6F2\",color=\"#142335\"];\n" )
    out.write( "  edge [style=solid,color=\"#142335\"];\n" )
    out.write( "  " )
    for (node <- g.nodes) out.write( s"n${node}; " )
    out.write( "\n" )
    for (edge <- g.edges) out.write( s"  n${edge._1} -- n${edge._2};\n" )
    out.write( "}" )
  }
  
  def generateHTML( filename: String, graphs: List[Graph] ) = {
    val html = new File( "dot", filename + ".html" )
    if (html.exists) html.delete
    val out = new PrintWriter( html )
    out.write( "<html>\n<head>\n" )
    out.write( "<link rel=\"stylesheet\" type=\"text/css\" href=\"graphs.css\">" )
    out.write( "</head>\n<body>\n<table border=\"0\">\n" )
    out.write( "<tr><th>Id</th><th>Graph</th><th>Degrees</th><th>Histogram</th></tr>\n" )
    var i: Int = 1
    for ( g <- graphs ) { toHTML( out, g, i ); i += 1 }
    out.write( "</table>\n</body>\n</html>" )
    out.close()
  }
  
  def toHTML( out: PrintWriter, g: Graph, i: Int ) {
    out.write( "<tr>\n" )
    out.write( "  <td>" + i + "</td>" )
    out.write( "  <td><img src=\"" + g.generateId + ".png\"/></td>\n" )
    out.write( "  <td>" + g.degreesCode + "</td>\n" )
    out.write( "  <td>" + g.histogramCode + "</td>\n" )
    out.write( "</tr>\n" )
  }
}
