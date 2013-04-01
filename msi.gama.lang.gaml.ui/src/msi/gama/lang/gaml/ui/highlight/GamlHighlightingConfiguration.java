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
package msi.gama.lang.gaml.ui.highlight;

/**
 * Written by drogoul
 * Modified on 16 nov. 2011
 * 
 * @todo Description
 * 
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.*;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

public class GamlHighlightingConfiguration extends DefaultHighlightingConfiguration {

	public static final String OPERATOR_ID = "binary";
	public static final String RESERVED_ID = "reserved";
	public static final String FACET_ID = "facet";
	public static final String FIELD_ID = "field";
	public static final String GLOBAL_ID = "global";
	public static final String VARIABLE_ID = "variable";
	public static final String VARDEF_ID = "varDef";
	public static final String TYPE_ID = "typeDef";
	public static final String ASSIGN_ID = "assignment";
	public static final String UNIT_ID = "unit";

	@Override
	public void configure(final IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(KEYWORD_ID, "Statement keywords", keywordTextStyle());
		acceptor.acceptDefaultHighlighting(PUNCTUATION_ID, "Punctuation characters",
			punctuationTextStyle());
		acceptor.acceptDefaultHighlighting(OPERATOR_ID, "Operators & action calls",
			operatorTextStyle());
		acceptor.acceptDefaultHighlighting(RESERVED_ID, "Reserved symbols", reservedTextStyle());
		acceptor.acceptDefaultHighlighting(COMMENT_ID, "Comments", commentTextStyle());
		acceptor.acceptDefaultHighlighting(STRING_ID, "Strings", stringTextStyle());
		acceptor.acceptDefaultHighlighting(NUMBER_ID, "Literal constants", numberTextStyle());
		acceptor.acceptDefaultHighlighting(DEFAULT_ID, "Default", defaultTextStyle());
		acceptor.acceptDefaultHighlighting(FACET_ID, "Facet keys", facetTextStyle());
		acceptor.acceptDefaultHighlighting(VARIABLE_ID, "Variables used in expressions",
			variableTextStyle());
		acceptor.acceptDefaultHighlighting(VARDEF_ID, "Variables definitions", varDefTextStyle());
		acceptor.acceptDefaultHighlighting(TYPE_ID, "Type", typeTextStyle());
		acceptor.acceptDefaultHighlighting(ASSIGN_ID, "Assignment signs", assignTextStyle());
		acceptor.acceptDefaultHighlighting(UNIT_ID, "Unit names", unitTextStyle());
	}

	public TextStyle facetTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(150, 0, 60));
		return textStyle;
	}

	public TextStyle typeTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(0, 79, 116));
		return textStyle;
	}

	public TextStyle assignTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(50, 50, 50));
		return textStyle;
	}

	public TextStyle variableTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(28, 125, 180));
		return textStyle;
	}

	public TextStyle fieldTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(125, 125, 0));
		return textStyle;
	}

	public TextStyle operatorTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(46, 93, 78));
		return textStyle;
	}

	public TextStyle reservedTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(0, 0, 0));
		return textStyle;
	}

	@Override
	public TextStyle errorTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		return textStyle;
	}

	public TextStyle unitTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.ITALIC);
		return textStyle;
	}

	@Override
	public TextStyle numberTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(125, 125, 125));
		return textStyle;
	}

	@Override
	public TextStyle keywordTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setColor(new RGB(127, 0, 85));
		textStyle.setStyle(SWT.BOLD);
		return textStyle;
	}

	@Override
	public TextStyle punctuationTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		return textStyle;
	}

	public TextStyle varDefTextStyle() {
		TextStyle textStyle = defaultTextStyle().copy();
		textStyle.setStyle(SWT.BOLD);
		textStyle.setColor(new RGB(0, 0, 153));
		return textStyle;
	}

}
