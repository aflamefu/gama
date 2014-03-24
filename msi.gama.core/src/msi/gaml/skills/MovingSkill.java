/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.skills;

import java.util.Map;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.*;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.graph.*;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.IGraph;
import msi.gama.util.path.*;
import msi.gaml.operators.*;
import msi.gaml.operators.Spatial.Punctal;
import msi.gaml.types.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * MovingSkill : This class is intended to define the minimal set of behaviours required from an
 * agent that is able to move. Each member that has a meaning in GAML is annotated with the
 * respective tags (vars, getter, setter, init, action & args)
 * 
 * @author drogoul 4 juil. 07
 */

@doc("The moving skill is intended to define the minimal set of behaviours required for agents that are able to move on different topologies")
@vars({
	@var(name = IKeyword.SPEED, type = IType.FLOAT, init = "1.0", doc = @doc("the speed of the agent (in meter/second)")),
	@var(name = IKeyword.HEADING, type = IType.INT, init = "rnd(359)", doc = @doc("the absolute heading of the agent in degrees (in the range 0-359)")),
	@var(name = IKeyword.DESTINATION, type = IType.POINT, depends_on = { IKeyword.SPEED, IKeyword.HEADING,
		IKeyword.LOCATION }, doc = @doc("continuously updated destination of the agent with respect to its speed and heading (read-only)")) })
@skill(name = IKeyword.MOVING_SKILL)
public class MovingSkill extends Skill {

	/**
	 * @throws GamaRuntimeException
	 * @throws GamaRuntimeException Gets the destination of the agent. The destination is the next
	 *             absolute coordinates the agent could reach if it keeps the current speed and the
	 *             current heading
	 */
	@getter(IKeyword.DESTINATION)
	public ILocation getDestination(final IAgent agent) {
		final ILocation actualLocation = agent.getLocation();
		// if ( actualLocation == null ) { return null; }
		final double dist = getSpeed(agent) /* agent.getSimulation().getScheduler().getStep() */;
		final ITopology topology = getTopology(agent);
		return topology.getDestination(actualLocation, agent.getHeading(), dist, false);
	}

	@setter(IKeyword.DESTINATION)
	public void setDestination(final IAgent agent, final ILocation p) {
		// READ_ONLY
	}

	@getter(IKeyword.SPEED)
	public double getSpeed(final IAgent agent) {
		return (Double) agent.getAttribute(IKeyword.SPEED);
	}

	@setter(IKeyword.SPEED)
	public void setSpeed(final IAgent agent, final double s) {
		agent.setAttribute(IKeyword.SPEED, s);
		// scope.setAgentVarValue(agent, IKeyword.SPEED, s);
	}

	@getter(IKeyword.HEADING)
	public int getHeading(final IAgent agent) {
		return agent.getHeading();
	}

	@setter(IKeyword.HEADING)
	public void setHeading(final IAgent agent, final int heading) {
		agent.setHeading(heading);
	}

	/**
	 * @throws GamaRuntimeException
	 * @throws GamaRuntimeException Prim: move randomly. Has to be redefined for every class that
	 *             implements this interface.
	 * 
	 * @param args the args speed (meter/sec) : the speed with which the agent wants to move
	 *            distance (meter) : the distance the agent want to cover in one step amplitude (in
	 *            degrees) : 360 or 0 means completely random move, while other values, combined
	 *            with the heading of the agent, define the angle in which the agent will choose a
	 *            new place. A bounds (geometry, agent, list of agents, list of geometries, species)
	 *            can be specified
	 * @return the path followed
	 */

	protected int computeHeadingFromAmplitude(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final int ampl = scope.hasArg("amplitude") ? scope.getIntArg("amplitude") : 359;
		agent.setHeading(agent.getHeading() + GAMA.getRandom().between(-ampl / 2, ampl / 2));
		return agent.getHeading();
	}

	protected int computeHeading(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Integer heading = scope.hasArg(IKeyword.HEADING) ? scope.getIntArg(IKeyword.HEADING) : null;
		if ( heading != null ) {
			agent.setHeading(heading);
		}
		return agent.getHeading();
	}

	protected double computeDistance(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		// We do not change the speed of the agent anymore. Only the current primitive is affected
		final Double s = scope.hasArg(IKeyword.SPEED) ? scope.getFloatArg(IKeyword.SPEED) : getSpeed(agent);
		// 20/1/2012 Change : The speed of the agent is multiplied by the timestep in order to
		// obtain the maximal distance it can cover in one step.
		return s * scope.getClock().getStep();
	}

	protected ILocation computeTarget(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object target = scope.getArg("target", IType.NONE);
		ILocation result = null;
		if ( target != null && target instanceof ILocated ) {
			result = ((ILocated) target).getLocation();
		}
		// if ( result == null ) {
		// scope.setStatus(ExecutionStatus.failure);
		// }
		return result;
	}

	protected ITopology computeTopology(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object on = scope.getArg("on", IType.NONE);
		final ITopology topo = Cast.asTopology(scope, on);
		if ( topo == null ) { return scope.getTopology(); }
		return topo;
	}

	protected Map computeMoveWeights(final IScope scope) throws GamaRuntimeException {
		return scope.hasArg("move_weights") ? (Map) scope.getArg("move_weights", IType.MAP) : null;
	}

	@action(name = "wander", args = {
		@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
		@arg(name = "amplitude", type = IType.INT, optional = true, doc = @doc("a restriction placed on the random heading choice. The new heading is chosen in the range (heading - amplitude/2, heading+amplitude/2)")),
		@arg(name = IKeyword.BOUNDS, type = { IType.AGENT, IType.GEOMETRY }, optional = true, doc = @doc("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")) }, doc = @doc(examples = { @example("do wander speed: speed - 10 amplitude: 120 bounds: agentA;") }, value = "Moves the agent towards a random location at the maximum distance (with respect to its speed). The heading of the agent is chosen randomly if no amplitude is specified. This action changes the value of heading."))
	public void primMoveRandomly(final IScope scope) throws GamaRuntimeException {

		final IAgent agent = getCurrentAgent(scope);
		final ILocation location = agent.getLocation();
		final int heading = computeHeadingFromAmplitude(scope, agent);
		final double dist = computeDistance(scope, agent);

		ILocation loc = scope.getTopology().getDestination(location, heading, dist, true);

		if ( loc == null ) {
			agent.setHeading(heading - 180);
			// pathFollowed = null;
		} else {
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if ( bounds != null ) {
				final IShape geom = GamaGeometryType.staticCast(scope, bounds, null);
				if ( geom != null && geom.getInnerGeometry() != null ) {
					loc = computeLocationForward(scope, dist, loc, geom.getInnerGeometry());
				}
			}
			// pathFollowed = new GamaPath(this.getTopology(agent), GamaList.with(location, loc));

			// Enable to use wander in 3D space. An agent will wander in the plan define by its z value.
			((GamaPoint) loc).z = agent.getLocation().getZ();
			agent.setLocation(loc);
		}
		// scope.setStatus(loc == null ? ExecutionStatus.failure : ExecutionStatus.success);
		// return null;
	}

	@action(name = "wander_3D", args = {
		@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
		@arg(name = "amplitude", type = IType.INT, optional = true, doc = @doc("a restriction placed on the random heading choice. The new heading is chosen in the range (heading - amplitude/2, heading+amplitude/2)")),
		@arg(name = "z_max", type = IType.INT, optional = true, doc = @doc("the maximum alltitude (z) the geometry can reach")),
		@arg(name = IKeyword.BOUNDS, type = { IType.AGENT, IType.GEOMETRY }, optional = true, doc = @doc("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")) }, doc = @doc(examples = { @example("do wander_3D speed: speed - 10 amplitude: 120 bounds: agentA;") }, value = "Moves the agent towards a random location (3D point) at the maximum distance (with respect to its speed). The heading of the agent is chosen randomly if no amplitude is specified. This action changes the value of heading."))
	public IPath primMoveRandomly3D(final IScope scope) throws GamaRuntimeException {

		final IAgent agent = getCurrentAgent(scope);
		final ILocation location = agent.getLocation();
		final int heading = computeHeadingFromAmplitude(scope, agent);
		final double dist = computeDistance(scope, agent);
		//By default the max z value is 100
		double w= scope.getSimulationScope().getEnvelope().getWidth();
		double h= scope.getSimulationScope().getEnvelope().getHeight();
		final int z_max = (int) (scope.hasArg("z_max") ? scope.getIntArg("z_max") : w > h ? w: h);
		ILocation loc = scope.getTopology().getDestination(location, heading, dist, true);

		if ( loc == null ) {
			agent.setHeading(heading - 180);
			// pathFollowed = null;
		} else {
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if ( bounds != null ) {
				final IShape geom = GamaGeometryType.staticCast(scope, bounds, null);
				//java.lang.System.out.println("define bound w:" + geom.getEnvelope().getWidth() + "h:" + geom.getEnvelope().getHeight() + "d:" + geom.getEnvelope().getDepth());
				if ( geom != null && geom.getInnerGeometry() != null ) {
					loc = computeLocationForward(scope, dist, loc, geom.getInnerGeometry());
				}
			}
			else{
				final IShape geom = scope.getSimulationScope().getGeometry();
				//java.lang.System.out.println("world bound w:" + geom.getEnvelope().getWidth() + "h:" + geom.getEnvelope().getHeight() + "d:" + geom.getEnvelope().getDepth());
				if ( geom != null && geom.getInnerGeometry() != null ) {
					loc = computeLocationForward(scope, dist, loc, geom.getInnerGeometry());
				}
			}
			((GamaPoint) loc).z =
			Math.max(0, ((GamaPoint) location).z + dist * (2 * RandomUtils.getDefault().next() - 1));
			((GamaPoint) loc).z = Math.min(((GamaPoint) loc).z, z_max);
			agent.setLocation(loc);
		}

		return null;
	}

	@action(name = "move", args = {
		@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
		@arg(name = IKeyword.HEADING, type = IType.INT, optional = true, doc = @doc("a restriction placed on the random heading choice. The new heading is chosen in the range (heading - amplitude/2, heading+amplitude/2)")),
		@arg(name = IKeyword.BOUNDS, type = { IType.GEOMETRY, IType.AGENT }, optional = true, doc = @doc("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")) }, doc = @doc(examples = { @example("do move speed: speed - 10 heading: heading + rnd (30) bounds: agentA;") }, value = "moves the agent forward, the distance being computed with respect to its speed and heading. The value of the corresponding variables are used unless arguments are passed."))
	public IPath primMoveForward(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final ILocation location = agent.getLocation();
		final double dist = computeDistance(scope, agent);
		final int heading = computeHeading(scope, agent);

		ILocation loc = scope.getTopology().getDestination(location, heading, dist, true);
		if ( loc == null ) {
			agent.setHeading(heading - 180);
			// pathFollowed = null;
		} else {
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if ( bounds != null ) {
				final IShape geom = GamaGeometryType.staticCast(scope, bounds, null);
				if ( geom != null && geom.getInnerGeometry() != null ) {
					loc = computeLocationForward(scope, dist, loc, geom.getInnerGeometry());
				}
			}
			// pathFollowed = new GamaPath(this.getTopology(agent), GamaList.with(location, loc));
			agent.setLocation(loc);
		}
		// scope.setStatus(loc == null ? ExecutionStatus.failure : ExecutionStatus.success);
		return null;
	}

	@action(name = "follow", args = {
		@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
		@arg(name = "path", type = IType.PATH, optional = false, doc = @doc("a path to be followed.")),
		@arg(name = "move_weights", type = IType.MAP, optional = true, doc = @doc("Weights used for the moving.")),
		@arg(name = "return_path", type = IType.BOOL, optional = true, doc = @doc("if true, return the path followed (by default: false)")) }, doc = @doc(value = "moves the agent along a given path passed in the arguments.", returns = "optional: the path followed by the agent.", examples = { @example("do follow speed: speed * 2 path: road_path;") }))
	public IPath primFollow(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final double dist = computeDistance(scope, agent);
		final Boolean returnPath = (Boolean) scope.getArg("return_path", IType.BOOL);
		final GamaMap weigths = (GamaMap) computeMoveWeights(scope);
		final GamaPath path = scope.hasArg("path") ? (GamaPath) scope.getArg("path", IType.PATH) : null;
		if ( path != null && !path.getEdgeList().isEmpty() ) {
			if ( returnPath != null && returnPath ) {
				final IPath pathFollowed = moveToNextLocAlongPath(scope, agent, path, dist, weigths);
				if ( pathFollowed == null ) {
					// scope.setStatus(ExecutionStatus.failure);
					return null;
				}
				// scope.setStatus(ExecutionStatus.success);
				return pathFollowed;
			}
			moveToNextLocAlongPathSimplified(scope, agent, path, dist, weigths);
			// scope.setStatus(ExecutionStatus.success);
			return null;
		}
		// scope.setStatus(ExecutionStatus.failure);
		return null;
	}

	@action(name = "goto", args = {
		@arg(name = "target", type = { IType.AGENT, IType.POINT, IType.GEOMETRY }, optional = false, doc = @doc("the location or entity towards which to move.")),
		@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
		@arg(name = "on", type = { IType.LIST, IType.AGENT, IType.GRAPH, IType.GEOMETRY }, optional = true, doc = @doc("list, agent, graph, geometry that restrains this move (the agent moves inside this geometry)")),
		@arg(name = "recompute_path", type = IType.BOOL, optional = true, doc = @doc("if false, the path is not recompute even if the graph is modified (by default: true)")),
		@arg(name = "return_path", type = IType.BOOL, optional = true, doc = @doc("if true, return the path followed (by default: false)")),
		@arg(name = "move_weights", type = IType.MAP, optional = true, doc = @doc("Weights used for the moving.")) }, doc = @doc(value = "moves the agent towards the target passed in the arguments.", returns = "optional: the path followed by the agent.", examples = { @example("do goto target: (one_of road).location speed: speed * 2 on: road_network;") }))
	public IPath primGoto(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final ILocation source = agent.getLocation().copy(scope);
		final double maxDist = computeDistance(scope, agent);
		final ILocation goal = computeTarget(scope, agent);
		if ( goal == null ) {
			// scope.setStatus(ExecutionStatus.failure);
			return null;
		}
		final ITopology topo = computeTopology(scope, agent);
		if ( topo == null ) {
			// scope.setStatus(ExecutionStatus.failure);
			return null;
		}
		Boolean recomputePath = (Boolean) scope.getArg("recompute_path", IType.NONE);
		if ( recomputePath == null ) {
			recomputePath = true;
		}
		IPath path = (GamaPath) agent.getAttribute("current_path");
		if ( path == null || path.getTopology() != null && !path.getTopology().equals(topo) ||
			!path.getEndVertex().equals(goal) || !path.getStartVertex().equals(source) ) {
			path = topo.pathBetween(scope, source, goal);

		} else {

			if ( topo instanceof GraphTopology ) {
				if ( ((GraphTopology) topo).getPlaces() != path.getGraph() || recomputePath &&
					((GraphTopology) topo).getPlaces().getVersion() != path.getGraphVersion() ) {
					path = topo.pathBetween(scope, source, goal);
				}
			}
		}
		if ( path == null ) {
			// scope.setStatus(ExecutionStatus.failure);
			return null;
		}

		final Boolean returnPath = (Boolean) scope.getArg("return_path", IType.NONE);
		final GamaMap weigths = (GamaMap) computeMoveWeights(scope);
		if ( returnPath != null && returnPath ) {
			final IPath pathFollowed = moveToNextLocAlongPath(scope, agent, path, maxDist, weigths);
			if ( pathFollowed == null ) {
				// scope.setStatus(ExecutionStatus.failure);
				return null;
			}
			// scope.setStatus(ExecutionStatus.success);
			return pathFollowed;
		}
		moveToNextLocAlongPathSimplified(scope, agent, path, maxDist, weigths);
		// scope.setStatus(ExecutionStatus.success);
		return null;
	}

	/**
	 * @throws GamaRuntimeException
	 *             Return the next location toward a target on a line
	 * 
	 * @param coords coordinates of the line
	 * @param source current location
	 * @param target location to reach
	 * @param distance max displacement distance
	 * @return the next location
	 */

	protected GamaList initMoveAlongPath(final IAgent agent, final IPath path, GamaPoint currentLocation) {
		final GamaList initVals = new GamaList();
		Integer index = 0;
		Integer indexSegment = 1;
		Integer endIndexSegment = 0;
		GamaPoint falseTarget = null;
		final IList<IShape> edges = path.getEdgeGeometry();
		if ( edges.isEmpty() ) { return null; }
		final int nb = edges.size();
		if ( path.getGraph() == null && nb == 1 && edges.get(0).getInnerGeometry().getNumPoints() == 2 ) {
			index = 0;
			indexSegment = 0;
			endIndexSegment = 0;
			falseTarget = (GamaPoint) path.getEndVertex();

		} else {
			if ( path.isVisitor(agent) ) {
				index = path.indexOf(agent);
				indexSegment = path.indexSegmentOf(agent);

			} else {
				path.acceptVisitor(agent);
				double distanceS = Double.MAX_VALUE;
				IShape line = null;
				for ( int i = 0; i < nb; i++ ) {
					line = edges.get(i);
					final double distS = line.euclidianDistanceTo(currentLocation);
					if ( distS < distanceS ) {
						distanceS = distS;
						index = i;
					}
				}
				line = edges.get(index);

				currentLocation = (GamaPoint) Punctal._closest_point_to(currentLocation, line);
				final Point pointGeom = (Point) currentLocation.getInnerGeometry();
				if ( line.getInnerGeometry().getNumPoints() >= 3 ) {
					distanceS = Double.MAX_VALUE;
					final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
					final int nbSp = coords.length;
					final Coordinate[] temp = new Coordinate[2];
					for ( int i = 0; i < nbSp - 1; i++ ) {
						temp[0] = coords[i];
						temp[1] = coords[i + 1];
						final LineString segment = GeometryUtils.FACTORY.createLineString(temp);
						final double distS = segment.distance(pointGeom);
						if ( distS < distanceS ) {
							distanceS = distS;
							indexSegment = i + 1;
						}
					}
				}
			}
			final IShape lineEnd = edges.get(nb - 1);
			falseTarget = (GamaPoint) Punctal._closest_point_to(((IShape) path.getEndVertex()).getLocation(), lineEnd);
			endIndexSegment = 1;
			final Point pointGeom = (Point) falseTarget.getInnerGeometry();
			if ( lineEnd.getInnerGeometry().getNumPoints() >= 3 ) {
				double distanceT = Double.MAX_VALUE;
				final Coordinate coords[] = lineEnd.getInnerGeometry().getCoordinates();
				final int nbSp = coords.length;
				final Coordinate[] temp = new Coordinate[2];
				for ( int i = 0; i < nbSp - 1; i++ ) {
					temp[0] = coords[i];
					temp[1] = coords[i + 1];
					final LineString segment = GeometryUtils.FACTORY.createLineString(temp);
					final double distT = segment.distance(pointGeom);
					if ( distT < distanceT ) {
						distanceT = distT;
						endIndexSegment = i + 1;
					}
				}
			}
		}
		initVals.add(index);
		initVals.add(indexSegment);
		initVals.add(endIndexSegment);
		initVals.add(currentLocation);
		initVals.add(falseTarget);
		return initVals;
	}

	private void moveToNextLocAlongPathSimplified(final IScope scope, final IAgent agent, final IPath path,
		final double d, final GamaMap weigths) {
		GamaPoint currentLocation = (GamaPoint) agent.getLocation().copy(scope);
		final GamaList indexVals = initMoveAlongPath(agent, path, currentLocation);
		if ( indexVals == null ) { return; }
		int index = (Integer) indexVals.get(0);
		int indexSegment = (Integer) indexVals.get(1);
		final int endIndexSegment = (Integer) indexVals.get(2);
		currentLocation = (GamaPoint) indexVals.get(3);
		final GamaPoint falseTarget = (GamaPoint) indexVals.get(4);
		final IList<IShape> edges = path.getEdgeGeometry();
		final int nb = edges.size();
		double distance = d;
		final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();
		for ( int i = index; i < nb; i++ ) {
			final IShape line = edges.get(i);
			final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
			double weight;
			if ( weigths == null ) {
				weight = computeWeigth(graph, path, line);
			} else {
				IShape realShape = path.getRealObject(line);
				final Double w =
					realShape == null ? null : (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
				weight = w == null ? computeWeigth(graph, path, line) : w;
			}

			for ( int j = indexSegment; j < coords.length; j++ ) {
				GamaPoint pt = null;
				if ( i == nb - 1 && j == endIndexSegment ) {
					pt = falseTarget;
				} else {
					pt = new GamaPoint(coords[j]);
				}
				double dist = pt.distance(currentLocation);
				dist = weight * dist;

				if ( distance < dist ) {
					final double ratio = distance / dist;
					final double newX = currentLocation.x + ratio * (pt.x - currentLocation.x);
					final double newY = currentLocation.y + ratio * (pt.y - currentLocation.y);
					currentLocation.setLocation(newX, newY);
					distance = 0;
					break;
				} else if ( distance > dist ) {
					currentLocation = pt;
					distance = distance - dist;
					if ( i == nb - 1 && j == endIndexSegment ) {
						break;
					}
					indexSegment++;
				} else {
					currentLocation = pt;
					distance = 0;
					if ( indexSegment < coords.length - 1 ) {
						indexSegment++;
					} else {
						if ( index < nb - 1 ) {
							index++;
						}
						indexSegment = 1;
					}
					break;
				}
			}
			if ( distance == 0 ) {
				break;
			}
			indexSegment = 1;
			if ( index < nb - 1 ) {
				index++;
			}
		}
		if ( currentLocation.equals(falseTarget) ) {
			currentLocation = (GamaPoint) path.getEndVertex();
			index++;
		}
		path.setIndexSegementOf(agent, indexSegment);
		path.setIndexOf(agent, index);
		agent.setLocation(currentLocation);
		path.setSource(currentLocation.copy(scope));
	}

	protected double computeWeigth(final IGraph graph, final IPath path, final IShape line) {
		if ( graph == null ) { return 1.0; }
		IShape realShape = path.getRealObject(line);
		return realShape == null ? 1 : graph.getEdgeWeight(realShape) / realShape.getGeometry().getPerimeter();
	}

	private IPath moveToNextLocAlongPath(final IScope scope, final IAgent agent, final IPath path, final double d,
		final GamaMap weigths) {
		GamaPoint currentLocation = (GamaPoint) agent.getLocation().copy(scope);
		final GamaList indexVals = initMoveAlongPath(agent, path, currentLocation);
		if ( indexVals == null ) { return null; }
		int index = (Integer) indexVals.get(0);
		int indexSegment = (Integer) indexVals.get(1);
		final int endIndexSegment = (Integer) indexVals.get(2);
		currentLocation = (GamaPoint) indexVals.get(3);
		final GamaPoint falseTarget = (GamaPoint) indexVals.get(4);
		final IList<IShape> edges = path.getEdgeGeometry();
		final int nb = edges.size();
		double distance = d;
		final GamaList<IShape> segments = new GamaList();
		final GamaPoint startLocation = (GamaPoint) agent.getLocation().copy(scope);
		final GamaMap agents = new GamaMap();
		for ( int i = index; i < nb; i++ ) {
			final IShape line = edges.get(i);
			final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();

			double weight;
			if ( weigths == null ) {
				weight = computeWeigth(graph, path, line);
			} else {
				IShape realShape = path.getRealObject(line);
				final Double w =
					realShape == null ? null : (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
				weight = w == null ? computeWeigth(graph, path, line) : w;
			}
			final Coordinate coords[] = line.getInnerGeometry().getCoordinates();

			for ( int j = indexSegment; j < coords.length; j++ ) {
				GamaPoint pt = null;
				if ( i == nb - 1 && j == endIndexSegment ) {
					pt = falseTarget;
				} else {
					pt = new GamaPoint(coords[j]);
				}
				double dist = scope.getTopology().distanceBetween(scope, pt, currentLocation);
				dist = weight * dist;
				if ( distance < dist ) {
					final GamaPoint pto = currentLocation.copy(scope);
					final double ratio = distance / dist;
					final double newX = pto.x + ratio * (pt.x - pto.x);
					final double newY = pto.y + ratio * (pt.y - pto.y);
					currentLocation.setLocation(newX, newY);
					final IShape gl = GamaGeometryType.buildLine(pto, currentLocation);
					final IShape sh = path.getRealObject(line);
					if ( sh != null ) {
						final IAgent a = sh.getAgent();
						if ( a != null ) {
							agents.put(gl, a);
						}
					}
					segments.add(gl);
					distance = 0;
					break;
				} else if ( distance > dist ) {
					final IShape gl = GamaGeometryType.buildLine(currentLocation, pt);
					final IShape sh = path.getRealObject(line);
					if ( sh != null ) {
						final IAgent a = sh.getAgent();
						if ( a != null ) {
							agents.put(gl, a);
						}
					}
					segments.add(gl);
					currentLocation = pt.copy(scope);
					distance = distance - dist;
					if ( i == nb - 1 && j == endIndexSegment ) {
						break;
					}
					indexSegment++;
				} else {
					final IShape gl = GamaGeometryType.buildLine(currentLocation, pt);
					if ( path.getRealObject(line) != null ) {
						final IAgent a = path.getRealObject(line).getAgent();

						if ( a != null ) {
							agents.put(gl, a);
						}
					}

					segments.add(gl);
					currentLocation = pt.copy(scope);;
					distance = 0;
					if ( indexSegment < coords.length - 1 ) {
						indexSegment++;
					} else {
						index++;
					}
					break;
				}
			}
			if ( distance == 0 ) {
				break;
			}
			indexSegment = 1;
			index++;
		}
		if ( currentLocation.equals(falseTarget) ) {
			currentLocation = (GamaPoint) path.getEndVertex();
		}
		path.setIndexSegementOf(agent, indexSegment);
		path.setIndexOf(agent, index);
		path.setSource(currentLocation.copy(scope));
		if ( segments.isEmpty() ) { return null; }
		final IPath followedPath =
			PathFactory.newInstance(agent.getTopology(), startLocation, currentLocation, segments, false);
		// new GamaPath(agent.getTopology(), startLocation, currentLocation, segments, false);
		followedPath.setRealObjects(agents);

		agent.setLocation(currentLocation);
		return followedPath;
	}

	private ILocation computeLocationForward(final IScope scope, final double dist, final ILocation loc,
		final Geometry geom) {

		final Point locPt = GeometryUtils.FACTORY.createPoint(getCurrentAgent(scope).getLocation().toCoordinate());
		final Geometry buff = locPt.buffer(dist);
		final Geometry test = locPt.buffer(dist / 100, 4);
		Geometry frontier = null;
		try {
			frontier = buff.intersection(geom);
		} catch (final Exception e) {
			// frontier = buff.intersection(geom.buffer(0.0));
			final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
			frontier =
				GeometryPrecisionReducer.reducePointwise(geom, pm).intersection(
					GeometryPrecisionReducer.reducePointwise(buff, pm));

		}

		Geometry geomsSimp = null;
		if ( frontier instanceof GeometryCollection ) {
			final GeometryCollection gc = (GeometryCollection) frontier;
			final int nb = gc.getNumGeometries();
			for ( int i = 0; i < nb; i++ ) {
				if ( !gc.getGeometryN(i).disjoint(test) ) {
					geomsSimp = gc.getGeometryN(i);
					break;
				}
			}
			if ( geomsSimp == null || geomsSimp.isEmpty() ) { return getCurrentAgent(scope).getLocation(); }
			frontier = geomsSimp;
		}
		final ILocation computedPt = Punctal._closest_point_to(loc, new GamaShape(frontier));
		if ( computedPt != null ) { return computedPt; }
		return getCurrentAgent(scope).getLocation();
	}
}
