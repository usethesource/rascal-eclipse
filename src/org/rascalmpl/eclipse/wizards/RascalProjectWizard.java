package org.rascalmpl.eclipse.wizards;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.rascalmpl.eclipse.IRascalResources;


public class RascalProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		boolean result =  super.performFinish();
		try {
			IProjectDescription description = getNewProject().getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = IRascalResources.ID_RASCAL_NATURE;
			description.setNatureIds(newNatures);
			getNewProject().setDescription(description, null);
		} catch (CoreException e) {
			// Something went wrong
		}
		return result;

	}


}