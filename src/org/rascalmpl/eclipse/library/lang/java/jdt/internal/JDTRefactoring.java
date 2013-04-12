/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
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
    
	public ISet encapsulateFields(ISet fieldOffsetsFromLoc, ISourceLocation loc) {
		ISet result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), new NullProgressMonitor());
    	try {
    		IFile file = jdt.getJavaIFileForLocation(loc);
			Set<IField> fields = new FindIFields().findFieldsAtLocs(fieldOffsetsFromLoc, loc, file);
			for (IField field : fields) {
				String fieldForGS = field.getElementName().substring(0,1).toUpperCase();
				if (field.getElementName().length() > 1)
					fieldForGS += field.getElementName().substring(1);
				String getterName = "__get" + fieldForGS;
				String setterName = "__set" + fieldForGS;
				result.union(encapsulateField(field, Flags.AccPublic, getterName, setterName ));
			}
    	} finally {
    		Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
    	}
    	return result;
    	
    }


	private ISet encapsulateField(IField field, int flags, String getterName, String setterName) {
		try {
			SelfEncapsulateFieldRefactoring refactoring = new SelfEncapsulateFieldRefactoring(field);
			refactoring.setVisibility(flags);
			refactoring.setConsiderVisibility(false); // only matters for IMethod objects
			refactoring.setGetterName(getterName);
			refactoring.setSetterName(setterName);
			PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(new NullProgressMonitor());
			ISet result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
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

	public ISet makeMethodsPublic(ISet methodOffsetsFromLoc, ISourceLocation loc) {
    	IFile file = jdt.getJavaIFileForLocation(loc);

		Set<IMethod> methods = new FindIMethods().findMethodsAtLocs(methodOffsetsFromLoc, loc, file);
		ISet result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		for (IMethod method : methods) {
			result.union(changeMethodSignature(method, Modifier.PUBLIC));
		}
		
		return result;
    }    
    
	private ISet changeMethodSignature(IMethod method, int visibility) {
		ISet result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		try {
			Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), new NullProgressMonitor());

			ChangeSignatureProcessor processor = new ChangeSignatureProcessor(method);
			Refactoring refactoring = new ProcessorBasedRefactoring(processor);
			processor.setVisibility(visibility);

			PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			operation.run(new NullProgressMonitor());

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
		} catch (JavaModelException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		} catch (CoreException e) {
			throw new Throw(VF.string(e.getMessage()), (ISourceLocation) null, null);
		} finally {
			Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
		}
		return result;
	}   
    
    public ISet cleanUpSource(ISourceLocation loc) {
    	IFile file = jdt.getJavaIFileForLocation(loc);

		ISet result = VF.relation(TF.tupleType(TF.stringType(), TF.stringType()));
		
		try {
			Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), new NullProgressMonitor());

			// Create the refactoring for the cleanup
			CleanUpRefactoring refactoring = new CleanUpRefactoring();
			
			// Add the given file as the compilation unit we will clean
			ICompilationUnit icu = JavaCore.createCompilationUnitFrom(file);
			refactoring.addCompilationUnit(icu);
			
			// Set the options: we want to identify local accesses with this, and static accesses with
			// the class name
			Map<String,String> cleanUpOptions = new HashMap<String,String>();
			// NOTE: We could use "true" instead of "true", but this requires 3.5 at least
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
		} finally {
			Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
		}
			
		return result;
    	
    }
    
	public IValue fullyQualifyTypeNames(ISourceLocation loc) {
    	IFile file = jdt.getJavaIFileForLocation(loc);
		return new FullyQualifyTypeNames().fullyQualifyTypeNames(loc, file);
    }

	public void removeMethods(ISet methodOffsetsFromLoc, ISourceLocation loc) {
    	IFile file = jdt.getJavaIFileForLocation(loc);
		new RemoveMethods().removeMethodsAtLocs(methodOffsetsFromLoc, loc, file);
    }

	public void unqualifyTypeNames(ISourceLocation loc) {
    	IFile file = jdt.getJavaIFileForLocation(loc);
		new UnqualifyTypeNames().unqualifyTypeNames(loc, file);
    }

}
