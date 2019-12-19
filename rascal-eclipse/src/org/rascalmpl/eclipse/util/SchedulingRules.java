package org.rascalmpl.eclipse.util;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class SchedulingRules {
    public static ISchedulingRule getRascalProjectBinFolderRule(IProject project) {
        if (project != null && project.isOpen()) {
            return getBinFolder(project);
        }
        else {
            return getRascalProjectsRule();
        }
    }
    
    public static ISchedulingRule getRascalProjectsRule() {
        return new MultiRule(Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                .filter(SchedulingRules::isOpenRascalProject)
                .map(SchedulingRules::getBinFolder)
                .toArray(ISchedulingRule[]::new));
    }
    
    private static IResource getBinFolder(IProject p) {
        try {
            if (p.hasNature(JavaCore.NATURE_ID)) {
                return p.getFolder(JavaCore.create(p).getOutputLocation());
            } 
        }
        catch (CoreException e) {
            Activator.log("could not get bin folder", e);
        }

        return p;
    }

    private static boolean isOpenRascalProject(IProject p) {
        try {
            return p.isOpen() && p.hasNature(IRascalResources.ID_RASCAL_NATURE);
        } catch (CoreException e) {
            Activator.log("unexpected error while checking for Rascal project", e);
            return false;
        }
    }
}
