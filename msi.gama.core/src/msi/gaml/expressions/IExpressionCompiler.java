/*******************************************************************************************************
 *
 * msi.gaml.expressions.IExpressionCompiler.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.expressions;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import msi.gama.common.interfaces.IDisposable;
import msi.gama.runtime.IExecutionContext;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.IExpressionDescription;
import msi.gaml.descriptions.OperatorProto;
import msi.gaml.statements.Arguments;
import msi.gaml.types.Signature;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 * 
 * @todo Description
 * 
 */
public interface IExpressionCompiler<T> extends IDisposable {

	public static final THashMap<String, THashMap<Signature, OperatorProto>> OPERATORS = new THashMap<>();
	public static final Set<String> ITERATORS = new THashSet<>();

	public abstract IExpression compile(final IExpressionDescription s, final IDescription parsingContext);

	public IExpression compile(final String expression, final IDescription parsingContext,
			IExecutionContext tempContext);

	Arguments parseArguments(ActionDescription action, EObject eObject, IDescription context, boolean compileArgValues);

	/**
	 * @param context
	 * @param facet
	 * @return
	 */
	public abstract EObject getFacetExpression(IDescription context, EObject facet);

	public List<IDescription> compileBlock(final String string, final IDescription actionContext,
			IExecutionContext tempContext);

}