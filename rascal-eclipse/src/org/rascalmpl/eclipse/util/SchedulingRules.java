package org.rascalmpl.eclipse.util;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class SchedulingRules {
    public static ISchedulingRule getRascalProjectsRule() {
        return new MultiRule(Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
                .filter(SchedulingRules::isRascalProject)
                .map(SchedulingRules::getBinFolder)
                .toArray(ISchedulingRule[]::new));
    }
    
    private static IPath getBinFolder(IProject p) {
        try {
            return JavaCore.create(p).getOutputLocation();
        } catch (JavaModelException e) {
            Activator.log("could not get bin folder", e);
            return p.getFullPath();
        }
    }

    private static boolean isRascalProject(IProject p) {
        try {
            return p.hasNature(IRascalResources.ID_RASCAL_NATURE);
        } catch (CoreException e) {
            Activator.log("unexpected error while checking for Rascal project", e);
            return false;
        }
    }
}
