/*********************************************************************************************
 *
 *
 * 'IOverlayProvider.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package msi.gama.common.interfaces;

import msi.gama.outputs.layers.OverlayStatement.OverlayInfo;

/**
 * Class IOverlay.
 *
 * @author drogoul
 * @since 9 mars 2014
 *
 */
public interface IOverlayProvider<C extends OverlayInfo> {

	public void setTarget(IUpdaterTarget<C> overlay, IDisplaySurface surface);
}
