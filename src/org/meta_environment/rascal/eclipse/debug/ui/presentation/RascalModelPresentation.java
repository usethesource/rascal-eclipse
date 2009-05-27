/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.meta_environment.rascal.eclipse.debug.ui.presentation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.meta_environment.rascal.eclipse.IRascalResources;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalLineBreakpoint;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalStackFrame;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;

/**
 * Renders Rascal debug elements
 */
public class RascalModelPresentation extends LabelProvider implements IDebugModelPresentation {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attribute, Object value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof RascalDebugTarget) {
			return getTargetText((RascalDebugTarget)element);
		} else if (element instanceof RascalThread) {
			return getThreadText((RascalThread)element);
		} else if (element instanceof RascalStackFrame) {
			return getStackFrameText((RascalStackFrame)element);
		}
		return null;
	}

	/**
	 * Returns a label for the given stack frame
	 * 
	 * @param frame a stack frame
	 * @return a label for the given stack frame 
	 */
	private String getStackFrameText(RascalStackFrame frame) {
		try {
			return frame.getName() + " (line: " + frame.getLineNumber() + ")"; 
		} catch (DebugException e) {
		}
		return null;

	}

	/**
	 * Returns a label for the given debug target
	 * 
	 * @param target debug target
	 * @return a label for the given debug target
	 */
	private String getTargetText(RascalDebugTarget target) {
		try {
			String pgmPath = target.getLaunch().getLaunchConfiguration().getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
			if (pgmPath != null) {
				IPath path = new Path(pgmPath);
				String label = "";
				if (target.isTerminated()) {
					label = "<terminated>";
				}
				return label + "Rascal [" + path.lastSegment() + "]";
			}
		} catch (CoreException e) {
		}
		return "Rascal";

	}

	/**
	 * Returns a label for the given thread
	 * 
	 * @param thread a thread
	 * @return a label for the given thread
	 */
	private String getThreadText(RascalThread thread) {
		String label;
		try {
			label = thread.getName();
		} catch (DebugException e) {
			//TODO: to improve
			label = "noname";
		}
		if (thread.isSuspended()) {
			IBreakpoint[] breakpoints = thread.getBreakpoints();
			if (breakpoints.length == 0) {
				label += " (suspended)";
			} else {
				label += " (suspended at line breakpoint)";
			}
		} else if (thread.isStepping()) {
			label += " (stepping)";
		} else if (thread.isTerminated()) {
			label = "<terminated> " + label;
		}
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String detail = "";
		try {
			detail = value.getValueString();
		} catch (DebugException e) {
		}
		listener.detailComputed(value, detail);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile) {
			return new FileEditorInput((IFile)element);
		}
		if (element instanceof ILineBreakpoint) {
			return new FileEditorInput((IFile)((ILineBreakpoint)element).getMarker().getResource());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object element) {
		if (element instanceof IFile || element instanceof ILineBreakpoint) {
			return IRascalResources.ID_RASCAL_EDITOR;
		}
		return null;
	}

}
