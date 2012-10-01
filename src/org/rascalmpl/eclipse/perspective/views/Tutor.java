package org.rascalmpl.eclipse.perspective.views;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.tutor.RascalTutor;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class Tutor extends ViewPart {
	public static final String ID = "rascal-eclipse.tutorBrowser";
	
	private Browser browser;
    private RascalTutor tutor;
	private Object lock = new Object();
    
	public Tutor() { }

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setText("<html><body>The Rascal tutor is now loading: <progress max=\"100\"></progress></body></html>");
		new StarterJob().schedule();
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}
	
	@Override
	public void dispose() {
		stop();
	}
	
	private void stop() {
		if (tutor != null) {
			try {
				tutor.stop();
				tutor = null;
			} catch (Exception e) {
				Activator.log("could not stop tutor", e);
			}
		}
	}
	
	private class StarterJob extends Job {
		public StarterJob() {
			super("Starting tutor");
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (lock) {
				int port = 9000;
				try {
					stop();

					if (tutor == null) {
						monitor.beginTask("Loading Tutor server", 2);
						tutor = new RascalTutor();
						URIResolverRegistry registry = tutor.getResolverRegistry();
						BundleURIResolver resolver = new BundleURIResolver(registry);
						registry.registerInput(resolver);
						registry.registerOutput(resolver);

						Evaluator eval = tutor.getRascalEvaluator();
						ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(eval.getResolverRegistry(), "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
						eval.getResolverRegistry().registerInput(eclipseResolver);
						eval.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
						eval.addClassLoader(getClass().getClassLoader());

						String rascalPlugin = jarForPlugin("rascal");
						String rascalEclipsePlugin = jarForPlugin("rascal_eclipse");
						String PDBValuesPlugin = jarForPlugin("org.eclipse.imp.pdb.values");

						Configuration.setRascalJavaClassPathProperty(
								rascalPlugin 
								+ File.pathSeparator 
								+ PDBValuesPlugin 
								+ File.pathSeparator 
								+ rascalPlugin 
								+ File.separator + "src" 
								+ File.pathSeparator 
								+ rascalPlugin + File.separator + "bin" 
								+ File.pathSeparator 
								+ PDBValuesPlugin + File.separator + "bin"
								+ File.pathSeparator
								+ rascalEclipsePlugin
								+ File.pathSeparator
								+ rascalEclipsePlugin + File.separator + "bin"
								);

						for (int i = 0; i < 100; i++) {
							try {
								tutor.start(port, new RascalMonitor(monitor));
								break;
							}
							catch (BindException e) {
								port += 1;
							}
						}
					}
					
					monitor.worked(1);
					
					final int foundPort = port;
					new WorkbenchJob("Loading tutor start page") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							browser.setUrl("http://127.0.0.1:" + foundPort);
							return Status.OK_STATUS;
						}
					}.schedule();
					
				}
				catch (Throwable e) {
					Activator.getInstance().logException("Could not start tutor server", e);
				}
			}
			
			return Status.OK_STATUS;
		}
		
		private String jarForPlugin(String pluginName) throws IOException {
			URL rascalURI = FileLocator.resolve(Platform.getBundle(pluginName).getEntry("/"));
			
			try {
				if (rascalURI.getProtocol().equals("jar")) {
					String path = rascalURI.toURI().toASCIIString();
					return path.substring(path.indexOf("/"), path.indexOf('!'));
				}
				else {
					// TODO this is a monumental workaround, apparently the Rascal plugin gets unpacked and in 
					// it is a rascal.jar file that we should lookup...
					String path = rascalURI.getPath();
					File folder = new File(path);
					if (folder.isDirectory()) {
						File[] list = folder.listFiles();
						for (File f : list) {
							if (f.getName().startsWith(pluginName) && f.getName().endsWith(".jar")) {
								return f.getAbsolutePath();
							}
						}
					}
					
					return path;
				}
			}
			catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
	}
}
