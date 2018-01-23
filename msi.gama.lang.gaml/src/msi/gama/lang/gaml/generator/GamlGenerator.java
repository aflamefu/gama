/*********************************************************************************************
 *
 * 'GamlGenerator.java, in plugin msi.gama.lang.gaml, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.lang.gaml.generator;

import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.generator.IGeneratorContext;

import msi.gama.lang.gaml.resource.GamlResource;
import msi.gama.lang.gaml.resource.GamlResourceServices;

/**
 * Class GamlGenerator.
 *
 * @author drogoul
 * @since 4 janv. 2016
 *
 */
public class GamlGenerator implements IGenerator2 {

	/**
	 *
	 */
	public GamlGenerator() {
	}

	/**
	 * Method doGenerate()
	 * 
	 * @see org.eclipse.xtext.generator.IGenerator#doGenerate(org.eclipse.emf.ecore.resource.Resource,
	 *      org.eclipse.xtext.generator.IFileSystemAccess)
	 */
//	@Override
//	public void doGenerate(final Resource input, final IFileSystemAccess fsa) {
		// TODO: Deactivated for the moment [DEACTIVATION OF
		// META_INFORMATION]
		// final GamlResource resource = (GamlResource) input;
		// final String fileName = getFilenameFor(resource);
		// final String contents = getContentsFor(resource);
		// fsa.generateFile(fileName, GamlOutputConfigurationProvider.META,
		// contents);
//	}

	/**
	 * @param input
	 * @return
	 */
	private String getContentsFor(final GamlResource input) {
		// final GamlProperties requires = input.getRequires();
		final StringWriter sw = new StringWriter();

		// requires.store(sw);

		return sw.toString();
	}

	/**
	 * @param input
	 * @return
	 */
	private String getFilenameFor(final GamlResource input) {
		IPath path = GamlResourceServices.getPathOf(input);
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		path = file.getProjectRelativePath();
		final String s = path.toString();
		return s + ".meta";
	}

	@Override
	public void doGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
		// TODO Auto-generated method stub
		
	}

}
