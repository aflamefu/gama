/*******************************************************************************************************
 *
 * msi.gama.util.graph.IGraph.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.util.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;

import msi.gama.metamodel.topology.graph.FloydWarshallShortestPathsGAMA;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaPair;
import msi.gama.util.IAddressableContainer;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.IModifiableContainer;
import msi.gama.util.path.IPath;
import msi.gaml.operators.Graphs;
import msi.gaml.operators.Graphs.GraphObjectToAdd;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;

/**
 * Written by drogoul Modified on 24 nov. 2011
 *
 * An interface for the different kinds of graphs encountered in GAML. Vertices are the keys (actually, pairs of nodes),
 * while edges are the values
 *
 */
@vars ({ @variable (
		name = "spanning_tree",
		type = IType.LIST,
		of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the list of edges that compose the minimal spanning tree of this graph") }),
		@variable (
				name = "circuit",
				type = IType.PATH,
				doc = { @doc ("Returns a polynomial approximation of the Hamiltonian cycle (the optimal tour passing through each vertex) of this graph") }),
		@variable (
				name = "connected",
				type = IType.BOOL,
				doc = { @doc ("Returns whether this graph is connected or not") }),
		@variable (
				name = "edges",
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of edges of the receiver graph") }),
		@variable (
				name = "vertices",
				type = IType.LIST,
				of = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of vertices of the receiver graph") }) })
@SuppressWarnings ({ "rawtypes" })
public interface IGraph<Node, Edge> extends IModifiableContainer<Node, Edge, GamaPair<Node, Node>, Graphs.GraphObjectToAdd>,
		IAddressableContainer<Node, Edge, GamaPair<Node, Node>, List<Edge>>, WeightedGraph<Node, Edge>,
		DirectedGraph<Node, Edge>, UndirectedGraph<Node, Edge>, IGraphEventProvider {

	public abstract double getVertexWeight(final Object v);

	public abstract Double getWeightOf(final Object v);

	public abstract void setVertexWeight(final Object v, final double weight);

	void setWeights(Map<?, Double> weights);

	public Collection _internalEdgeSet();

	public Collection _internalNodesSet();

	public Map<Edge, _Edge<Node, Edge>> _internalEdgeMap();

	public Map<Node, _Vertex<Node, Edge>> _internalVertexMap();

	@getter ("edges")
	public abstract IList<Edge> getEdges();

	@getter ("vertices")
	public abstract IList<Node> getVertices();

	@getter ("spanning_tree")
	public abstract IList<Edge> getSpanningTree(IScope scope);

	@getter ("circuit")
	public abstract IPath<Node, Edge, IGraph<Node, Edge>> getCircuit(IScope scope);

	@getter ("connected")
	public abstract Boolean getConnected();

	@getter ("has_cycle")
	public abstract Boolean hasCycle();

	public abstract boolean isDirected();

	public abstract void setDirected(final boolean b);

	public abstract Object addEdge(Object p);

	public abstract void setOptimizerType(String optiType);

	public FloydWarshallShortestPathsGAMA<Node, Edge> getOptimizer();

	public void setOptimizer(FloydWarshallShortestPathsGAMA<Node, Edge> optimizer);

	public int getVersion();

	public void setVersion(int version);

	public void incVersion();

	// FIXME Patrick: To check
	// public abstract IPath<V,E> computeShortestPathBetween(final Object
	// source, final Object target);
	// public abstract IList<IShape> computeBestRouteBetween(final Object
	// source, final Object target);

	public abstract IPath<Node, Edge, IGraph<Node, Edge>> computeShortestPathBetween(IScope scope, final Node source,
			final Node target);

	public abstract IList<Edge> computeBestRouteBetween(IScope scope, final Node source, final Node target);

	public double computeWeight(final IPath<Node, Edge, ? extends IGraph<Node, Edge>> gamaPath);

	public double computeTotalWeight();

	public boolean isSaveComputedShortestPaths();

	public void setSaveComputedShortestPaths(boolean saveComputedShortestPaths);

	public abstract IList<IPath<Node, Edge, IGraph<Node, Edge>>> computeKShortestPathsBetween(IScope scope, Node source,
			Node target, int k);

	public abstract IList<IList<Edge>> computeKBestRoutesBetween(IScope scope, final Node source, final Node target,
			int k);

	public Graphs.GraphObjectToAdd buildValue(IScope scope, Object object);

	public IContainer buildValues(IScope scope, IContainer objects);

	public GamaPair<Node, Node> buildIndex(IScope scope, Object object);

	public IContainer<?, GamaPair<Node, Node>> buildIndexes(IScope scope, IContainer value);

	public ISpecies getVertexSpecies();

	public ISpecies getEdgeSpecies();

}