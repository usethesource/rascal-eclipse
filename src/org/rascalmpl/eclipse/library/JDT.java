package org.rascalmpl.eclipse.library;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.sef.SelfEncapsulateFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUp;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.rascalmpl.eclipse.library.jdt.FindIFields;
import org.rascalmpl.eclipse.library.jdt.FindIMethods;
import org.rascalmpl.eclipse.library.jdt.FullyQualifyTypeNames;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

@SuppressWarnings("restriction")
public class JDT {
	
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private final IValueFactory VF;
	private static final TypeFactory TF = TypeFactory.getInstance();
	
    public JDT(IValueFactory vf) {
    	this.VF = vf;
	}
    
    private  IProject getProject(String project) {
    	IProject p = ROOT.getProject(project);
		
		if (p == null) {
			throw new Throw(VF.string("Project does not exist"), (ISourceLocation) null, null);
		}
		
		return p;
	}

    private  IResource getResource(ISourceLocation loc) {
    	URI uri = loc.getURI();
		
		if (!uri.getScheme().equals("project")) {
			throw RuntimeExceptionFactory.schemeNotSupported(loc, null, null);
		}
		
		// ugly workaround b/c URI.getPath() doesn't always return the decoded path
		String path = "";
		try {
			path = URLDecoder.decode(uri.getPath(), java.nio.charset.Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (path.length() == 0) {
			throw new Throw(VF.string("URI is not a valid path"), (ISourceLocation) null, null);
		}		

		IProject p = getProject(uri.getHost());
		if (!p.exists(new Path(path))) {
			throw new Throw(VF.string("Path does not exist: " + path), (ISourceLocation) null, null);
		}

		IResource r = p.getFile(path);
		if (!r.exists()) {
			r = p.getFolder(path);
			if (!r.exists()) {
				throw new Throw(VF.string("Path is not a file nor a folder: " + path), (ISourceLocation) null, null);
			}
		}
		
		return r;
    }
    
	public IRelation encapsulateFields(ISet fieldOffsetsFromLoc, ISourceLocation loc) {
    	IFile file = getIFileForLocation(loc);

		Set<IField> fields = new FindIFields().findFieldsAtLocs(fieldOffsetsFromLoc, loc, file);
		IRelation result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		for (IField field : fields) {
			result.union(encapsulateField(field, Flags.AccPublic));
		}
		
		return result;
    }


	private IRelation encapsulateField(IField field, int flags) {
    	// TODO: Currently we only allow the visibility to be passed in. It may make sense to
    	// also pass in other options, so we can use this as a general method to interact with
    	// the encapsulate field refactoring.
		try {
			SelfEncapsulateFieldRefactoring refactoring = new SelfEncapsulateFieldRefactoring(field);
			refactoring.setVisibility(flags);
			refactoring.setConsiderVisibility(false); // only matters for IMethod objects
			PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(new NullProgressMonitor());
			IRelation result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
			RefactoringStatus rs = operation.getConditionStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(field.getElementName()), VF.string(rse.getMessage())));
				}
			}
			rs = operation.getValidationStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(field.getElementName()), VF.string(rse.getMessage())));
				}
			}
			return result;
		} catch (JavaModelException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		} catch (CoreException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		}
	}    

	public IRelation makeMethodsPublic(ISet methodOffsetsFromLoc, ISourceLocation loc) {
    	IFile file = getIFileForLocation(loc);

		Set<IMethod> methods = new FindIMethods().findMethodsAtLocs(methodOffsetsFromLoc, loc, file);
		IRelation result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		for (IMethod method : methods) {
			result.union(changeMethodSignature(method, Modifier.PUBLIC));
		}
		
		return result;
    }    
    
	private IRelation changeMethodSignature(IMethod method, int visibility) {
		try {
			ChangeSignatureProcessor processor = new ChangeSignatureProcessor(method);
			Refactoring refactoring = new ProcessorBasedRefactoring(processor);
			processor.setVisibility(visibility);

			PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(new NullProgressMonitor());

			IRelation result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
			RefactoringStatus rs = operation.getConditionStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(method.getElementName() + " " + method.getSignature()), VF.string(rse.getMessage())));
				}
			}
			rs = operation.getValidationStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(method.getElementName() + " " + method.getSignature()), VF.string(rse.getMessage())));
				}
			}
			return result;
		} catch (JavaModelException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		} catch (CoreException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		}
	}   
    
    public IRelation cleanUpSource(ISourceLocation loc) {
    	IFile file = getIFileForLocation(loc);

		IRelation result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		
		try {
			// Create the refactoring for the cleanup
			CleanUpRefactoring refactoring = new CleanUpRefactoring();
			
			// Add the given file as the compilation unit we will clean
			ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
			refactoring.addCompilationUnit(icu);
			
			// Set the options: we want to identify local accesses with this, and static accesses with
			// the class name
			Map<String,String> cleanUpOptions = new HashMap<String,String>();
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD, CleanUpOptions.TRUE);
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS, CleanUpOptions.TRUE);
			
			// Register this clean-up set of options 
			CodeStyleCleanUp cleanUp = new CodeStyleCleanUp(cleanUpOptions);
			refactoring.addCleanUp(cleanUp);
			
			// Now, actually do the clean-up
			refactoring.setValidationContext(null);
			PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(new NullProgressMonitor());
	
			RefactoringStatus rs = operation.getConditionStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(file.getName()), VF.string(rse.getMessage())));
				}
			}
			rs = operation.getValidationStatus();
			if (rs != null && rs.hasWarning()) {
				for (RefactoringStatusEntry rse : rs.getEntries()) {
					result.insert(VF.tuple(VF.string(file.getName()), VF.string(rse.getMessage())));
				}
			}
		} catch (JavaModelException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		} catch (CoreException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		}
			
		return result;
    	
    }
    
	public void fullyQualifyTypeNames(ISourceLocation loc) {
    	IFile file = getIFileForLocation(loc);
		new FullyQualifyTypeNames().fullyQualifyTypeNames(loc, file);
    }

	public  IConstructor extractClass(ISourceLocation loc) {
		IFile file = getIFileForLocation(loc);

		Map<String,IValue> facts = new org.rascalmpl.eclipse.library.jdt.JDTImporter().importFacts(loc, file);
		IConstructor resource = (IConstructor) Resources.file.make(VF, loc);
		return resource.setAnnotations(facts);
	}
	
	public  IValue isOnBuildPath(ISourceLocation loc) {
		IResource r = getResource(loc);
		IJavaProject jp = JavaCore.create(r.getProject());
		
		if (!jp.exists()) {
			throw new Throw(VF.string("Location is not in a Java project: " + loc), (ISourceLocation) null, null);
		}
		
		return VF.bool(jp.isOnClasspath(r));
	}
	
	private IFile getIFileForLocation(ISourceLocation loc) {
		IResource projectRes = getResource(loc);
		if (!(projectRes instanceof IFile)) {
			throw new Throw(VF.string("Location is not a file: " + loc), (ISourceLocation) null, null);
		}
		
		IFile file = (IFile) projectRes;
		if (!file.getFileExtension().equals("java")) {
			throw new Throw(VF.string("Location is not a Java file: " + loc), (ISourceLocation) null, null);
		}
		return file;
	}
	
}
