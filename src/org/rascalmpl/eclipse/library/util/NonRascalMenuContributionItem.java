package org.rascalmpl.eclipse.library.util;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.eclipse.util.RascalInvoker;
import org.rascalmpl.interpreter.asserts.NotYetImplemented;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.values.ValueFactoryFactory;


public class NonRascalMenuContributionItem extends CompoundContributionItem {
	
	private static String NON_RASCAL_CONTRIBUTION_COMMAND_CATEGORY = "org.rascalmpl.eclipse.library.util.NRCMCC";
	private static String NON_RASCAL_CONTRIBUTION_COMMAND_PREFIX = "org.rascalmpl.eclipse.library.util.NRCMCP";
	private final static TypeFactory TF = TypeFactory.getInstance();
	private final static IValueFactory VF = ValueFactoryFactory.getValueFactory();

	
    /**
     * Creates a compound contribution item with a <code>null</code> id.
     */
    public NonRascalMenuContributionItem() {
        super();
    }

    /**
     * Creates a compound contribution item with the given (optional) id.
     *
     * @param id the contribution item identifier, or <code>null</code>
     */
    public NonRascalMenuContributionItem(String id) {
        super(id);
    }
    
    private class ContributionCacheItem {
    	public WeakReference<ISet> rascalContributions; // avoid being to only one with a reference to a bunch of rascal callbacks with evaluators
    	public List<String> eclipseContributionsIds;
    }
    private static Map<String, ContributionCacheItem> contributionCache = new ConcurrentHashMap<String, ContributionCacheItem>();
	private static void cleanupCacheFor(String currentEditorId) {
		ContributionCacheItem cachedItemIds;
		synchronized (contributionCache) { // avoid double deletion
			cachedItemIds = contributionCache.remove(currentEditorId);
		}
		if (cachedItemIds != null){
			ICommandService cmdService = getCommandService();
			for (String cmdId : cachedItemIds.eclipseContributionsIds) {
				Command currentCommand = cmdService.getCommand(cmdId);
				IHandler currentHandler = currentCommand.getHandler();
				currentCommand.undefine();
				currentHandler.dispose();
			}
			
		}			

	}
	
	@Override
	protected IContributionItem[] getContributionItems() {
		String currentEditorId = getCurrentEditorID();
		if (currentEditorId.isEmpty()) {
			return new IContributionItem[0];
		}
		ISet contribs = TermLanguageRegistry.getInstance().getNonRascalContributions(currentEditorId);
		if (contribs == null) {
			cleanupCacheFor(currentEditorId);
			return new IContributionItem[0];
		}
		
		ContributionCacheItem cachedItemIds = contributionCache.get(currentEditorId);
		List<String> contributionItemIds;
		if (cachedItemIds != null && cachedItemIds.rascalContributions.get() == contribs) {
			contributionItemIds = cachedItemIds.eclipseContributionsIds;
		}
		else {
			cleanupCacheFor(currentEditorId); // first cleanup the cache items to avoid reusing an old one
			contributionItemIds = generateContributions(contribs);
			Collections.sort(contributionItemIds);// make sure the members are always in the same order.
			
			// updating the cache
			cachedItemIds = new ContributionCacheItem();
			cachedItemIds.rascalContributions = new WeakReference<ISet>(contribs);
			cachedItemIds.eclipseContributionsIds = contributionItemIds;
			contributionCache.put(currentEditorId, cachedItemIds);
		}
		// we cannot cache this because eclipse disposes these menu items.
		IContributionItem[] result = new IContributionItem[contributionItemIds.size()];
		IServiceLocator serviceLocator = getServiceLocator();
		for (int i = 0; i < contributionItemIds.size(); i++ ) {
			CommandContributionItemParameter newCommandParams = new CommandContributionItemParameter(
					serviceLocator, null, contributionItemIds.get(i), CommandContributionItem.STYLE_PUSH);
			result[i] =  new CommandContributionItem(newCommandParams);
		}
	    return result;
	}



	private List<String> generateContributions(ISet contribs) {
		ICommandService cmdService = getCommandService();
		IHandlerService handlerService = getHandlerService();
		Category defaultCategory = getDefaultCategory(cmdService);
		List<String> result = new ArrayList<String>();
		for (IValue contrib : contribs) {
			IConstructor node = (IConstructor) contrib;
			if (node.getName().equals("popup")) {
				result.add(contribute(defaultCategory, cmdService, handlerService, (IConstructor)node.get("menu")));
			}
		}
		return result;
	}

	private String contribute(Category defaultCategory, ICommandService cmdService, IHandlerService handlerService, IConstructor menu) {

		String label = ((IString) menu.get("label")).getValue();
		if (menu.getName().equals("edit")) {
			throw new RuntimeException("Edit is not support by non rascal windows");
		}
		else if (menu.getName().equals("action") && menu.has("handler")) {
			//Because we are not sure a label will not break the characters in a command id, we encode it.
			String commandId = NON_RASCAL_CONTRIBUTION_COMMAND_PREFIX + encodeLabel(label);
			Command newCommand = cmdService.getCommand(commandId);
			if (!newCommand.isDefined()) {
				newCommand.define(label, "A non rascal contribution", defaultCategory);
			}
			final ICallableValue func = (ICallableValue) menu.get("handler");
			IHandler handler = new AbstractHandler() {
				public Object execute(ExecutionEvent event) throws ExecutionException {
					ITextSelection selection = (ITextSelection)HandlerUtil.getActiveWorkbenchWindowChecked(event).getSelectionService().getSelection();
					IEditorInput activeEditorInput = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
					URI fileRef = new Resources(VF).makeFile(activeEditorInput).getURI();
					final ISourceLocation selectedLine = VF.sourceLocation(fileRef, selection.getOffset(), selection.getLength(), selection.getStartLine() + 1, selection.getEndLine() + 1, 0, 0);
					final IString selectedText = VF.string(selection.getText());
					if (selectedLine != null) {
						RascalInvoker.invokeAsync(new Runnable() {
							public void run() {
								func.call(new Type[] { TF.stringType(), TF.sourceLocationType() }, new IValue[] { selectedText,  selectedLine }, null);
							}
						}, func.getEval());
					}
					return null;
				}
			};
			newCommand.setHandler(handler);
			return commandId;
			
		}
		else {
			throw new NotYetImplemented("Advanced menu structures are not yet implemented.");
		}
	}
	

	private String encodeLabel(String label) {
		try {
			return URLEncoder.encode(label, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return label;
		}
		
	}

	private static String getCurrentEditorID() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getId();
	}

	private static IServiceLocator getServiceLocator() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	   
	private static IHandlerService getHandlerService() {
		return (IHandlerService) getServiceLocator().getService(IHandlerService.class);
	}
	
	private static ICommandService getCommandService() {
		return (ICommandService)getServiceLocator().getService(ICommandService.class);
	}

	private Category getDefaultCategory(ICommandService cmdService) {
		Category defaultCategory = cmdService.getCategory(NON_RASCAL_CONTRIBUTION_COMMAND_CATEGORY);
		if (!defaultCategory.isDefined()) {
			defaultCategory.define("Non Rascal Contributions", "A category for non rascal contributions");
		}
		return defaultCategory;
	}

	

}
