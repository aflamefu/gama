/*
 * generated by Xtext
 */
package msi.gama.lang.gaml.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import msi.gama.lang.gaml.GamlRuntimeModule;
import msi.gama.lang.gaml.GamlStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
public class GamlIdeSetup extends GamlStandaloneSetup {

	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(new GamlRuntimeModule(), new GamlIdeModule()));
	}
	
}
