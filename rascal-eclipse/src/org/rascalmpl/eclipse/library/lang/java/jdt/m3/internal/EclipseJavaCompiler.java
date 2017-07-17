package org.rascalmpl.eclipse.library.lang.java.jdt.m3.internal;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.library.lang.java.m3.internal.LimitedTypeStore;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.vallang.IBool;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class EclipseJavaCompiler extends org.rascalmpl.library.lang.java.m3.internal.EclipseJavaCompiler {

    public EclipseJavaCompiler(IValueFactory vf) {
        super(vf);
    }
    
    public ISet createAstsFromEclipseProject(ISourceLocation root, IBool collectBindings, IBool errorRecovery, IEvaluatorContext ctx) {
        LimitedTypeStore store = getM3Store(ctx);

        ISetWriter result = VF.setWriter();
        Map<String, ISourceLocation> cache = new HashMap<>();
        compileAllFiles(root, collectBindings.getValue(), errorRecovery.getValue(), (loc, ast) -> {
            result.insert(convertToAST(collectBindings, cache, loc, ast, store));
        });
        return result.done();
    }
    
    public ISet createM3sFromEclipseProject(ISourceLocation root, IBool errorRecovery, IEvaluatorContext ctx) {
        LimitedTypeStore store = getM3Store(ctx);


        ISetWriter result = VF.setWriter();
        Map<String, ISourceLocation> cache = new HashMap<>();
        compileAllFiles(root, true, errorRecovery.getValue(), (loc, ast) -> {
            result.insert(convertToM3(store, cache, loc, ast));
        });
        return result.done();
        
    }

    public IValue createAstFromEclipseFile(ISourceLocation file, IBool collectBindings, IBool errorRecovery, IEvaluatorContext ctx) {
        LimitedTypeStore store = getM3Store(ctx);


        CompilationUnit cu = compileOneFile(file, collectBindings.getValue(), errorRecovery.getValue());
        Map<String, ISourceLocation> cache = new HashMap<>();
        return convertToAST(collectBindings, cache, file, cu, store);
    }
    
    public IValue createM3FromEclipseFile(ISourceLocation file, IBool errorRecovery, IEvaluatorContext ctx) {
        LimitedTypeStore store = getM3Store(ctx);


        CompilationUnit cu = compileOneFile(file, true, errorRecovery.getValue());
        Map<String, ISourceLocation> cache = new HashMap<>();
        return convertToM3(store, cache, file, cu);
    }
    
    private void compileAllFiles(ISourceLocation root, boolean collectBindings, boolean errorRecovery, BiConsumer<ISourceLocation, CompilationUnit> consumeCompiled) {
        IJavaProject project = getProject(root);
        ASTParser parser = constructASTParser(collectBindings, project, errorRecovery); 
        parser.createASTs(getFiles(project), new String[0], new ASTRequestor() {
            @Override
            public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
                consumeCompiled.accept(getLocation(root, project, source), ast);
            }
        }, null);
    }

    private CompilationUnit compileOneFile(ISourceLocation file, boolean collectBindings, boolean errorRecovery) {
        IJavaProject project = getProject(file);
        IJavaElement path;
        try {
             path = project.findElement(new Path(file.getPath()));
             if (path == null) {
                 throw RuntimeExceptionFactory.io(VF.string("Could not find" + file), null, null);
             }
             if (path.getElementType() != IJavaElement.COMPILATION_UNIT) {
                 throw RuntimeExceptionFactory.io(VF.string("" + file + "is not a compilation unit"), null, null);
             }
        }
        catch (JavaModelException e) {
             throw RuntimeExceptionFactory.io(VF.string("Could not find " + file), null, null);
        }
        ASTParser parser = constructASTParser(collectBindings, project, errorRecovery); 
        CompilationUnit[] result = new CompilationUnit[] { null };
        parser.createASTs(getFiles(project), new String[0], new ASTRequestor() {
            @Override
            public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
                if (result[0] != null) {
                    throw new RuntimeException("Got more than one AST?");
                }
                result[0] = ast;
            }
        }, null);
        return result[0];
    }
    
    protected ISourceLocation getLocation(ISourceLocation root, IJavaProject project, ICompilationUnit source) {
        try {
            return URIUtil.changePath(root, source.getPath().makeRelativeTo(project.getPath()).toString());
        }
        catch (URISyntaxException e) {
            return URIUtil.invalidLocation();
        }
    }

    private ICompilationUnit[] getFiles(IJavaProject project) {
        ArrayList<ICompilationUnit> result = new ArrayList<>();
        try {
            for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    Queue<IParent> subDirs = new LinkedList<>();
                    subDirs.add(root);
                    IParent currentEntry;
                    while ((currentEntry = subDirs.poll()) != null) {
                        for (IJavaElement child: currentEntry.getChildren()) {
                            switch (child.getElementType()) {
                                case IJavaElement.PACKAGE_FRAGMENT:
                                case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                                    subDirs.add((IParent)child);
                                    break;
                                case IJavaElement.COMPILATION_UNIT:
                                    result.add((ICompilationUnit) child);
                                    break;
                            }
                        }
                    }
                }
            }
        }
        catch (JavaModelException e) {
            return null;
        }
        return result.toArray(new ICompilationUnit[result.size()]);
    }

    public IJavaProject getProject(ISourceLocation root) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(root.getAuthority());
        if (project == null || !project.isOpen()) {
            throw RuntimeExceptionFactory.io(VF.string("project " + root.getAuthority() + " could not be opened."), null, null);
        }
        return JavaCore.create(project);
    }
    
    protected ASTParser constructASTParser(boolean resolveBindings, IJavaProject project, boolean errorRecovery) {
        IString javaVersion = VF.string(project.getOption(JavaCore.COMPILER_COMPLIANCE, true));
        ASTParser result = super.constructASTParser(resolveBindings, errorRecovery, javaVersion, null, null);
        result.setProject(project);
        return result;
    }

}
