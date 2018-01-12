package org.rascalmpl.eclipse.tutor;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

import io.usethesource.impulse.builder.ProjectNatureBase;
import io.usethesource.impulse.runtime.IPluginLog;

public class TutorNature extends ProjectNatureBase {

    @Override
    public String getNatureID() {
        return IRascalResources.ID_TUTOR_NATURE;
    }

    @Override
    public String getBuilderID() {
        return IRascalResources.ID_TUTOR_BUILDER;
    }

    @Override
    public IPluginLog getLog() {
        return Activator.getInstance();
    }

    @Override
    protected void refreshPrefs() {
        
    }


}
