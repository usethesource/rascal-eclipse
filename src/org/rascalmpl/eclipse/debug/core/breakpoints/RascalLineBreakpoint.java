package org.rascalmpl.eclipse.debug.core.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.NullVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

/**
 * Rascal line breakpoint
 */
public class RascalLineBreakpoint extends LineBreakpoint {
	
	// target currently installed in
	protected RascalDebugTarget target;
	protected IResource resource;
	
	public IResource getResource() {
		return resource;
	}

	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	public RascalLineBreakpoint() {
		super();
	}
	
	@Override
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
		//restore the resource for persisted breakpoints
		resource = marker.getResource();
	}
	
	/**
	 * Constructs a line breakpoint on the given resource at the given
	 * line number. The line number is 1-based (i.e. the first line of a
	 * file is line number 1). The Rascal VM uses 0-based line numbers,
	 * so this line number translation is done at breakpoint install time.
	 * 
	 * @param editor that is associated with the resource.
	 * @param resource file on which to set the breakpoint
	 * @param lineNumber 1-based line number of the breakpoint
	 * @throws CoreException if unable to create the breakpoint
	 */
	public RascalLineBreakpoint(final UniversalEditor editor, final IResource resource, final int lineNumber) throws CoreException {
		this.resource = resource;
		
		final ISourceLocation location = calculateClosestLocation(editor, lineNumber);
		
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker("rascal.markerType.lineBreakpoint");
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				int offset = location.getOffset();
				marker.setAttribute(IMarker.CHAR_START, offset);
				marker.setAttribute(IMarker.CHAR_END, offset + location.getLength());
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Line Breakpoint: " + resource.getName() + " [line: " + lineNumber + "]");
			}
		};
		run(getMarkerRule(resource), runnable);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return IRascalResources.ID_RASCAL_DEBUG_MODEL;
	}
	
	/**
	 * Returns whether this breakpoint is a run-to-line breakpoint
	 * 
	 * @return whether this breakpoint is a run-to-line breakpoint
	 */
	public boolean isRunToLineBreakpoint() {
		return false;
	}
    
    /**
     * Installs this breakpoint in the given interpretor.
     * 
     * @param target Rascal interpretor
     * @throws CoreException if installation fails
     */
    public void install(RascalDebugTarget target) throws CoreException {
    	this.target = target;
    }
    
    
    /**
     * Removes this breakpoint from the given interpretor.
     * Removes this breakpoint as an event listener and clears
     * the request for the interpretor.
     * 
     * @param target Rascal interpretor
     * @throws CoreException if removal fails
     */
    public void remove(RascalDebugTarget target) throws CoreException {
    	this.target = null;
    }
    
    /**
     * Returns the target this breakpoint is installed in or <code>null</code>.
     * 
     * @return the target this breakpoint is installed in or <code>null</code>
     */
    protected RascalDebugTarget getDebugTarget() {
    	return target;
    }
    
	private static ISourceLocation calculateClosestLocation(UniversalEditor editor, final int lineNumber){
		IConstructor parseTree = (IConstructor) editor.getParseController().getCurrentAst();
		class OffsetFinder extends NullVisitor<IValue>{
			public ISourceLocation location = null;
			
			public IValue visitConstructor(IConstructor o) throws VisitorException{
				IValue locationAnnotation = o.getAnnotation(Factory.Location);
				if(locationAnnotation != null){
					ISourceLocation sourceLocation = ((ISourceLocation) locationAnnotation);
					if(sourceLocation.getBeginLine() == lineNumber){
						String sortName = TreeAdapter.getSortName(o);
						if(sortName.equals("Statement") || sortName.equals("Expression")){
							location = sourceLocation;
							throw new VisitorException("Stop");
						}
					}
				}
				
				for(IValue child : o){
					child.accept(this);
				}
				
				return null;
			}
			
			public IValue visitNode(INode o) throws VisitorException{
				for(IValue child : o){
					child.accept(this);
				}
				
				return null;
			}
			
			public IValue visitList(IList o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitSet(ISet o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitRelation(IRelation o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
			
			public IValue visitMap(IMap o) throws VisitorException{
				for(IValue v : o){
					v.accept(this);
				}
				return null;
			}
		}
		
		OffsetFinder of = new OffsetFinder();
		try{
			parseTree.accept(of);
		}catch(VisitorException vex){
			// Ignore.
		}
		
		return of.location;
	}
}