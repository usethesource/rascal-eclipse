package org.rascalmpl.eclipse.library;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.sef.SelfEncapsulateFieldRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.structure.ChangeSignatureProcessor;
import org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUp;
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

@SuppressWarnings("restriction")
public class JDTRefactoring {
    private final IValueFactory VF;
    private final JDT jdt;
	private static final TypeFactory TF = TypeFactory.getInstance();
	
    public JDTRefactoring(IValueFactory vf) {
    	this.VF = vf;
    	this.jdt = new JDT(vf);
	}
    
	public IRelation encapsulateFields(ISet fieldOffsetsFromLoc, ISourceLocation loc) {
    	IFile file = jdt.getIFileForLocation(loc);

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
    	IFile file = jdt.getIFileForLocation(loc);

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
    	IFile file = jdt.getIFileForLocation(loc);

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
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD, "true");
			cleanUpOptions.put(CleanUpConstants.MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS, "true");
			
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
    	IFile file = jdt.getIFileForLocation(loc);
		new FullyQualifyTypeNames().fullyQualifyTypeNames(loc, file);
    }

}
