/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Anya Helene Bagge - A.H.S.Bagge@cwi.nl (Univ. Bergen)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.asserts.ImplementationError;

public class RascalMonitor implements IRascalMonitor {
	private SubRascalMonitor subMon = null;
	private final IProgressMonitor monitor;
	private String topName;
  private final IWarningHandler handler;

	public RascalMonitor(IProgressMonitor monitor, IWarningHandler handler) {
		this.monitor = monitor;
		this.handler = handler;
	}
	
	@Override
	public int endJob(boolean succeeded) {
		if (subMon == null) {
			throw new UnsupportedOperationException("endJob without startJob");
		}
		int worked = subMon.getWorkDone();
		subMon = subMon.endJob();
		monitor.setTaskName(topName);
		return worked;
	}

	@Override
	public void event(String name) {
		event(name, 1);
	}

	@Override
	public void event(String name, int inc) {
		if(subMon != null) {
			event(inc);
			subMon.setName(name);
		}
		else {
			throw new ImplementationError("event() called before startJob()");
		}
	}

	@Override
	public void event(int inc) {
		if(subMon != null)
			subMon.event(inc);
		else
			throw new ImplementationError("event() called before startJob()");
	}

	@Override
	public void startJob(String name) {
		startJob(name, 10, 0);
	}

	@Override
	public void startJob(String name, int totalWork) {
		startJob(name, totalWork, totalWork);
	}

	@Override
	public void startJob(String name, int workShare, int totalWork) {
	  if (topName == null) {
	    topName = name;
	  }
	  monitor.setTaskName(name);
	  
		if(subMon == null)
			subMon = new SubRascalMonitor(SubMonitor.convert(monitor), name, workShare, totalWork);
		else
			subMon = subMon.startJob(name, workShare, totalWork);
	}
	
	@Override
	public void todo(int workRemaining) {
		if(subMon != null)
			subMon.todo(workRemaining);
		else
			throw new ImplementationError("event() called before startJob()");
	}
	
	private long nextPoll = 0;
	private boolean previousResult;
	@Override
	public boolean isCanceled() {
		if (System.currentTimeMillis() < nextPoll) {
			return previousResult;
		}
		nextPoll = System.currentTimeMillis() + 100;
		previousResult = monitor.isCanceled();
		return previousResult;
	}

	private class SubRascalMonitor {
		private final SubRascalMonitor parent;
		private final SubMonitor monitor;
		private int workActuallyDone;
		private int workRemaining;
		private int nextWorkUnit;
		
		
		SubRascalMonitor(SubRascalMonitor parent, String name, int workShare, int totalWork) {
			this.monitor = parent.monitor.newChild(workShare);
			monitor.beginTask(name, totalWork);
			this.workRemaining = totalWork;
			this.parent = parent;
			parent.nextWorkUnit = workShare;
		}
		
		SubRascalMonitor(SubMonitor monitor, String name, int workShare, int totalWork) {
			this.monitor = SubMonitor.convert(monitor, workShare);
			monitor.beginTask(name, totalWork);
			this.workRemaining = totalWork;
			this.parent = null;
		}
		
		void event(int inc) {
			monitor.worked(nextWorkUnit);
			workActuallyDone += nextWorkUnit;
			if(workRemaining == 0)
				monitor.setWorkRemaining(200);
			else
				workRemaining -= nextWorkUnit;
			nextWorkUnit = inc;
		}
		
		void todo(int work) {
			workRemaining = work;
			monitor.setWorkRemaining(work);
		}
		
		SubRascalMonitor startJob(String name, int workShare, int totalWork) {
			return new SubRascalMonitor(this, name, workShare, totalWork);
		}
		
		SubRascalMonitor endJob() {
			monitor.done();
			workActuallyDone += nextWorkUnit;
			nextWorkUnit = 0;

			if (parent != null) {
				parent.workActuallyDone += parent.nextWorkUnit;
				if (parent.workRemaining != 0)
					parent.workRemaining -= parent.nextWorkUnit;
				parent.nextWorkUnit = 0;
			}
			return parent;
		}

		void setName(String name) {
			monitor.subTask(name);
		}

		public int getWorkDone() {
			return workActuallyDone;
		}
	}

  @Override
  public void warning(String msg, ISourceLocation src) {
    handler.warning(msg, src);
  }
}
