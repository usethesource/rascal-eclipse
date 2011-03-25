package org.rascalmpl.eclipse.nature;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.asserts.ImplementationError;

public class RascalMonitor implements IRascalMonitor {
	private SubRascalMonitor subMon = null;
	private final IProgressMonitor monitor;

	public RascalMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public int endJob(boolean succeeded) {
		int worked = subMon.getWorkDone();
		subMon = subMon.endJob();
		return worked;
	}

	public void event(String name) {
		event(name, 1);
	}

	public void event(String name, int inc) {
		if(subMon != null) {
			event(inc);
			subMon.setName(name);
		}
		else
			throw new ImplementationError("event() called before startJob()");
	}

	public void event(int inc) {
		if(subMon != null)
			subMon.event(inc);
		else
			throw new ImplementationError("event() called before startJob()");
	}

	public void startJob(String name) {
		startJob(name, 10, 0);
	}

	public void startJob(String name, int totalWork) {
		startJob(name, totalWork, totalWork);
	}

	public void startJob(String name, int workShare, int totalWork) {
		if(subMon == null)
			subMon = new SubRascalMonitor(SubMonitor.convert(monitor), name, workShare, totalWork);
		else
			subMon = subMon.startJob(name, workShare, totalWork);
	}
	
	public void todo(int workRemaining) {
		if(subMon != null)
			subMon.todo(workRemaining);
		else
			throw new ImplementationError("event() called before startJob()");
	}
	
	private class SubRascalMonitor {
		private final SubRascalMonitor parent;
		private final SubMonitor monitor;
		private int workActuallyDone;
		private final String name;
		private int workRemaining;
		private int nextWorkUnit;
		
		
		SubRascalMonitor(SubRascalMonitor parent, String name, int workShare, int totalWork) {
			this.name = name;
			this.monitor = parent.monitor.newChild(workShare);
			monitor.beginTask(name, totalWork);
			this.workRemaining = totalWork;
			this.parent = parent;
			parent.nextWorkUnit = workShare;
		}
		
		SubRascalMonitor(SubMonitor monitor, String name, int workShare, int totalWork) {
			this.name = name;
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
			RuntimePlugin.getInstance().getConsoleStream().println("Work done: " + workActuallyDone + " (" + name + ")");

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

		/** TODO: this method won't actually help you get any work done... */
		public int getWorkDone() {
			return workActuallyDone;
		}

		public String getName() {
			return name;
		}

	}
}
