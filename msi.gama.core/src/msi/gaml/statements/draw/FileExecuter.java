/**
 * Created by drogoul, 28 janv. 2016
 *
 */
package msi.gaml.statements.draw;

import java.awt.geom.Rectangle2D;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.file.*;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;

class FileExecuter extends DrawExecuter {

	private final GamaFile constImg;

	FileExecuter(final IExpression item) throws GamaRuntimeException {
		super(item);
		constImg = item.isConst() ? Cast.as(item, GamaFile.class, false) : null;
	}

	@Override
		Rectangle2D executeOn(final IScope scope, final IGraphics g, final DrawingData data)
			throws GamaRuntimeException {
		final GamaFile file = constImg == null ? (GamaFile) item.value(scope) : constImg;
		if ( file == null ) { return null; }
		FileDrawingAttributes attributes = computeAttributes(scope, data, file instanceof GamaImageFile);
		return g.drawFile(file, attributes);
	}

	FileDrawingAttributes computeAttributes(final IScope scope, final DrawingData data, final boolean noBorder) {
		FileDrawingAttributes attributes = new FileDrawingAttributes(data.currentSize, data.currentRotation,
			data.currentLocation, data.currentColor, noBorder ? null : data.currentBorder, scope.getAgentScope());
		// We push the location of the agent if none has been provided
		attributes.setLocationIfAbsent(new GamaPoint(attributes.agent.getLocation()));
		// If a size is provided, we automatically center the file
		// if ( attributes.size != null ) {
		// final GamaPoint location = attributes.location;
		// final double displayWidth = attributes.size.x;
		// final double displayHeight = attributes.size.y;
		// final double displayDepth = attributes.size.z;
		// final double x = location.x - displayWidth / 2;
		// final double y = location.y - displayHeight / 2;
		// final double z = location.z - displayDepth / 2;
		// // New location
		// attributes.location = new GamaPoint(x, y, z);
		// }
		return attributes;
	}
}