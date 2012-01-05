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
package msi.gama.metamodel.shape;

import msi.gama.common.util.GeometryUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.util.*;
import msi.gaml.types.*;
import com.vividsolutions.jts.algorithm.PointLocator;
import com.vividsolutions.jts.algorithm.distance.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.operation.distance.IndexedFacetDistance;

/**
 * Written by drogoul Modified on 25 ao�t 2010
 * 
 * 
 * 
 */
@vars({ @var(name = "area", type = IType.FLOAT_STR), @var(name = "width", type = IType.FLOAT_STR),
	@var(name = "height", type = IType.FLOAT_STR),
	@var(name = "points", type = IType.LIST_STR, of = IType.POINT_STR),
	@var(name = "envelope", type = IType.GEOM_STR),
	@var(name = "geometries", type = IType.LIST_STR, of = IType.GEOM_STR),
	@var(name = "multiple", type = IType.BOOL_STR),
	@var(name = "holes", type = IType.LIST_STR, of = IType.GEOM_STR),
	@var(name = "contour", type = IType.GEOM_STR) })
public class GamaShape implements IShape {

	private Geometry geometry;
	private ILocation location;
	private boolean isPoint;
	private Operations optimizedOperations;
	private IAgent agent;
	private static double dx = 0, dy = 0;

	public GamaShape(final Geometry geom) {
		setInnerGeometry(geom);
	}

	public GamaShape(final IShape geom) {
		this(geom.getInnerGeometry());
	}

	public GamaShape(final ILocation point) {
		this(GeometryUtils.getFactory().createPoint(point.toCoordinate()));
	}

	public GamaShape() {

	}

	@Override
	public IType type() {
		return Types.get(IType.GEOMETRY);
	}

	@getter(var = "multiple")
	public boolean isMultiple() {
		return getInnerGeometry() instanceof GeometryCollection;
	}

	@getter(var = "geometries")
	public GamaList<GamaShape> getGeometries() {
		GamaList<GamaShape> result = new GamaList();
		if ( isMultiple() ) {
			for ( int i = 0, n = getInnerGeometry().getNumGeometries(); i < n; i++ ) {
				result.add(new GamaShape(getInnerGeometry().getGeometryN(i)));
			}
		} else {
			result.add(this);
		}
		return result;
	}

	@Override
	public boolean isPoint() {
		return isPoint;
	}

	@Override
	public String stringValue() {
		return getInnerGeometry().getGeometryType();
	}

	@Override
	public String toGaml() {
		if ( isPoint ) { return getLocation().toGaml() + " as geometry"; }
		if ( isMultiple() ) { return getGeometries().toGaml() + " as geometry"; }
		GamaList<GamaShape> holes = getHoles();
		String result = "polygon (" + new GamaList(getPoints()).toGaml() + ")";
		if ( holes.isEmpty() ) { return result; }
		for ( GamaShape g : holes ) {
			result = "(" + result + ") - (" + g.toGaml() + ")";
		}
		return result;
	}

	//
	// @Override
	// public String toJava() {
	// // TODO False now
	// return "new " + getClass().getCanonicalName() + "(" +
	// GeometricFunctions.class.getCanonicalName() + ".buildGeometryJTS(" +
	// Cast.toJava(getGeometries()) + "))";
	// }

	@Override
	public String toString() {
		return getInnerGeometry().toText() + " at " + getLocation();
	}

	@Override
	public ILocation getLocation() {
		return location;
	}

	@Override
	public void setLocation(final ILocation l) {
		final ILocation previous = location;
		location = l;
		if ( previous != null ) {
			// if ( isPoint ) {
			dx = isPoint ? location.getX() : location.getX() - previous.getX();
			dy = isPoint ? location.getY() : location.getY() - previous.getY();
			geometry.apply(isPoint ? modification : translation);
			geometry.geometryChanged();
			// } else {
			// Geometry g =
			// GeometryUtils.translation(getInnerGeometry(),
			// location.getX() - previous.getX(), location.getY() - previous.getY());
			// setGeometry(g, false);
			// }
		}
	}

	// @Override
	// public boolean equals(final Object o) {
	// if ( !(o instanceof GamaGeometry) ) { return false; }
	// GamaGeometry g = (GamaGeometry) o;
	// if ( isPoint && g.isPoint ) { return location.equals(g.location); }
	// return geometry.equals(((GamaGeometry) o).geometry);
	// }

	private static Translation2 translation = new Translation2();
	private static PointModification modification = new PointModification();

	private static class Translation2 implements CoordinateFilter {

		/**
		 * @see com.vividsolutions.jts.geom.CoordinateFilter#filter(com.vividsolutions.jts.geom.Coordinate)
		 */
		@Override
		public void filter(final Coordinate coord) {
			coord.x = coord.x + dx;
			coord.y = coord.y + dy;
		}

	}

	private static class PointModification implements CoordinateFilter {

		@Override
		public void filter(final Coordinate c) {
			c.x = dx;
			c.y = dy;
		}

	}

	public static class Operations {

		final static GamaPoint currentPoint = new GamaPoint(0d, 0d);
		final static Point point = GeometryUtils.getFactory().createPoint(new Coordinate(0, 0));
		final static PointPairDistance ppd = new PointPairDistance();
		final static PointLocator pl = new PointLocator();

		final Geometry cached;
		IndexedFacetDistance distance;
		PreparedGeometry prepared;

		public Operations(final GamaShape g1) {
			cached = g1.getInnerGeometry();
		}

		private IndexedFacetDistance distanceOp() {
			if ( distance == null ) {
				distance = new IndexedFacetDistance(cached);
			}
			return distance;
		}

		private PreparedGeometry preparedOp() {
			if ( prepared == null ) {
				prepared = PreparedGeometryFactory.prepare(cached);
			}
			return prepared;
		}

		public double getDistance(final IShape g2) {
			if ( g2.isPoint() ) {
				// if (isPoint()) {return getLocation().distance(g2.getLocation());
				ppd.initialize();
				DistanceToPoint.computeDistance(cached, g2.getLocation().toCoordinate(), ppd);
				return ppd.getDistance();
			}
			return distanceOp().getDistance(g2.getInnerGeometry());
		}

		public boolean covers(final IShape g) {
			return g.isPoint() ? pl.intersects(g.getLocation().toCoordinate(), cached)
				: preparedOp().covers(g.getInnerGeometry());
		}

		public boolean intersects(final IShape g) {
			return g.isPoint() ? pl.intersects(g.getLocation().toCoordinate(), cached)
				: preparedOp().intersects(g.getInnerGeometry());
		}
	}

	@Override
	public IShape getGeometry() {
		return this;
	}

	@getter(var = "area")
	public Double getArea() {
		return getInnerGeometry().getArea();
	}

	@Override
	public double getPerimeter() {
		return getInnerGeometry().getLength();
	}

	@getter(var = "holes")
	public GamaList<GamaShape> getHoles() {
		GamaList<GamaShape> holes = new GamaList();
		if ( getInnerGeometry() instanceof Polygon ) {
			Polygon p = (Polygon) getInnerGeometry();
			int n = p.getNumInteriorRing();
			for ( int i = 0; i < n; i++ ) {
				holes.add(new GamaShape(p.getInteriorRingN(i)));
			}
		}
		return holes;
	}

	@getter(var = "contour")
	public GamaShape getExteriorRing() {
		Geometry result = getInnerGeometry();
		if ( result instanceof Polygon ) {
			result = ((Polygon) result).getExteriorRing();
		} else

		if ( result instanceof MultiPolygon ) {
			MultiPolygon mp = (MultiPolygon) result;
			LineString lines[] = new LineString[mp.getNumGeometries()];
			for ( int i = 0; i < mp.getNumGeometries(); i++ ) {
				lines[i] = ((Polygon) mp.getGeometryN(i)).getExteriorRing();
			}
			result = GeometryUtils.getFactory().createMultiLineString(lines);

		}
		return new GamaShape(result);
	}

	@getter(var = "width")
	public Double getWidth() {
		return getInnerGeometry().getEnvelopeInternal().getWidth();
	}

	@getter(var = "height")
	public Double getHeight() {
		return getInnerGeometry().getEnvelopeInternal().getHeight();
	}

	@getter(var = "envelope")
	public GamaShape getGeometricEnvelope() {
		return new GamaShape(getInnerGeometry().getEnvelope());
	}

	@getter(var = "points")
	public IList<GamaPoint> getPoints() {
		GamaList<GamaPoint> result = new GamaList();
		Coordinate[] points = getInnerGeometry().getCoordinates();
		for ( Coordinate c : points ) {
			result.add(new GamaPoint(c));
		}
		return result;
	}

	@Override
	public Envelope getEnvelope() {
		return getInnerGeometry().getEnvelopeInternal();
	}

	@Override
	public IAgent getAgent() {
		return agent;
	}

	@Override
	public void setAgent(final IAgent a) {
		agent = a;
	}

	@Override
	public void setInnerGeometry(final Geometry geom) {
		setGeometry(geom, true);
	}

	@Override
	public void setGeometry(final IShape geom) {
		if ( geom == null || geom == this ) { return; }
		location = geom.getLocation();
		setGeometry(geom.getInnerGeometry(), false);
	}

	protected void setGeometry(final Geometry geom, final boolean computeLoc) {
		geometry = geom;
		if ( geom == null ) { return; }
		isPoint = geom.getNumPoints() == 1;
		if ( computeLoc ) {
			computeLocation();
		}
		optimizedOperations = null;
	}

	private void computeLocation() {
		Coordinate c = getInnerGeometry().getCentroid().getCoordinate();
		if ( location == null ) {
			location = new GamaPoint(c);
		} else {
			location.setLocation(c.x, c.y);
		}
	}

	@Override
	public void dispose() {
		// if ( getInnerGeometry() != null ) {
		// setInnerGeometry((Geometry) null);
		// }
		// IMPORTANT We now leave the geometry of the agent intact in case it is used elsewhere
		// in topologies, etc.
		optimizedOperations = null;
		agent = null;
	}

	public boolean contains(final Object o) {
		if ( o == null ) { return false; }
		if ( o instanceof Geometry ) { return getInnerGeometry().covers((Geometry) o); }
		if ( o instanceof GamaShape ) { return getInnerGeometry().covers(
			((GamaShape) o).getInnerGeometry()); }
		if ( o instanceof ILocation ) { return contains(GeometryUtils.getFactory().createPoint(
			((ILocation) o).toCoordinate())); }
		return false;

	}

	@Override
	public Geometry getInnerGeometry() {
		return geometry;
	}

	@Override
	public IShape copy() {

		// TODO Attention : in case of points and lines, buffer(0,0) returns an empty polygon !!!!!
		return new GamaShape((Geometry) geometry.clone());
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#covers(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean covers(final IShape g) {
		return operations().covers(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#euclidianDistanceTo(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public double euclidianDistanceTo(final IShape g) {
		if ( isPoint() ) { return g.euclidianDistanceTo(this.getLocation()); }
		return operations().getDistance(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#intersects(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean intersects(final IShape g) {
		return operations().intersects(g);
	}

	private GamaShape.Operations operations() {
		if ( optimizedOperations == null ) {
			optimizedOperations = new GamaShape.Operations(this);
		}
		return optimizedOperations;
	}

}
