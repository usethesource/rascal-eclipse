package org.rascalmpl.eclipse.actions;

import java.io.OutputStream;

import org.eclipse.core.internal.jobs.JobStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.commands.AbstractEditorAction;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.eclipse.util.ResourcesToModules;
import org.rascalmpl.library.experiments.Compiler.Commands.Rascal;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.RVMExecutable;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.ApiGen;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.java2rascal.Java2Rascal;
import org.rascalmpl.library.lang.rascal.boot.IKernel;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;

public class GenerateInterface extends AbstractEditorAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate, IViewActionDelegate {
    public GenerateInterface() {
        super(null, "Generate Interface");
    }

    public GenerateInterface(UniversalEditor editor) {
        super(editor, "Generate Interface");
    }

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public boolean isEnabled() {
        return file != null && IRascalResources.RASCAL_EXT.equals(file.getFileExtension());
    }

    @Override
    public void run() {
        new Job("Compiling module and Generating Interface") {
            @Override
            protected IStatus run(IProgressMonitor arg0) {
                String moduleName = ResourcesToModules.moduleFromFile(file);
                IValueFactory vf = ValueFactoryFactory.getValueFactory();
                PathConfig pcfg = new ProjectConfig(vf).getPathConfig(file.getProject());

                try {
                    IKernel kernel = Java2Rascal.Builder.bridge(vf, new PathConfig(), IKernel.class)
                            .trace(false)
                            .profile(false)
                            .verbose(false)
                            .build();

                    kernel.compileAndLink(vf.string(moduleName), pcfg.asConstructor(kernel), kernel.kw_compileAndLink());
                    ISourceLocation binary = Rascal.findBinary(pcfg.getBin(), moduleName);
                    RVMExecutable exec = RVMExecutable.read(binary);

                    String modulePath = getModulePath(moduleName);
                    String pkg = getPackageName(modulePath); 
                    String api = ApiGen.generate(exec, moduleName, pkg);
                    String path = pkg + "/" + modulePath + ".java";
                    ISourceLocation apiLoc = URIUtil.getChildLocation((ISourceLocation) pcfg.getSrcs().get(0), path); 
                    OutputStream apiOut = URIResolverRegistry.getInstance().getOutputStream(apiLoc, false);
                    apiOut.write(api.getBytes());
                    apiOut.close();
                    return new Status(IStatus.OK, Activator.PLUGIN_ID, "API generated");           
                } catch (Exception e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "could not generate API", e);
                }
            }
        }.schedule();
    }

    private String getPackageName(String modulePath) {
        int i = modulePath.lastIndexOf(".");
        if (i == -1) {
            return file.getProject().getName();
        }
        
        return modulePath.replaceAll("/", ".").substring(0, i);
    }

    private String getModulePath(String moduleName) {
        String modulePath;
        int i = moduleName.lastIndexOf("::");
        if (i >= 0) {
            modulePath = moduleName.substring(0, i+2) + "I" + moduleName.substring(i+2);
            modulePath = modulePath.replaceAll("::",  "/");
        } else {
            modulePath = "I" + moduleName;
        }
        return modulePath;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof StructuredSelection) {
            StructuredSelection ssel = (StructuredSelection) selection;

            Object elem = ssel.getFirstElement();
            if (elem instanceof IFile) {
                file = (IFile) elem;
            }
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        if (targetPart instanceof UniversalEditor) {
            UniversalEditor editor = (UniversalEditor) targetPart;
            IFile file = initFile(editor, editor.getParseController().getProject());

            if (file != null) {
                project = file.getProject();
                this.file = file;
            }
        }
    }

    @Override
    public void init(IViewPart view) {
    }
}
