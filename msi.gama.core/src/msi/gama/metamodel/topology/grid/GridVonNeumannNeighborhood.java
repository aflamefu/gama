/*******************************************************************************************************
 *
 * msi.gama.metamodel.topology.grid.GridVonNeumannNeighborhood.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.metamodel.topology.grid;

import gnu.trove.set.hash.TIntHashSet;
import msi.gaml.operators.fastmaths.CmnFastMath;

/**
 * Written by drogoul Modified on 8 mars 2011
 *
 * @todo Description
 *
 */
public class GridVonNeumannNeighborhood extends GridNeighborhood {


	/**
	 * @param gamaSpatialMatrix
	 */
	GridVonNeumannNeighborhood(final GamaSpatialMatrix matrix) {
		super(matrix);
	}

	@Override
	protected TIntHashSet getNeighborsAtRadius(final int placeIndex, final int radius) {
		final int y = placeIndex / matrix.numCols;
		final int x = placeIndex - y * matrix.numCols;

		final TIntHashSet v = new TIntHashSet(radius << 2);
		int p;
		for ( int i = -radius; i < radius; i++ ) {
			p = matrix.getPlaceIndexAt(x - i, y - CmnFastMath.abs(i) + radius);
			if ( p != -1 ) {
				v.add(p);
			}
			p = matrix.getPlaceIndexAt(x + i, y + CmnFastMath.abs(i) - radius);
			if ( p != -1 ) {
				v.add(p);
			}
		}
		return v;
	}

	@Override
	public boolean isVN() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.metamodel.topology.grid.INeighborhood#clear()
	 */
	@Override
	public void clear() {}
}