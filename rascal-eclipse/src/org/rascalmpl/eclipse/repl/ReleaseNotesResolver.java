package org.rascalmpl.eclipse.repl;

import java.io.IOException;

import org.rascalmpl.library.util.SemVer;
import org.rascalmpl.uri.ILogicalSourceLocationResolver;
import org.rascalmpl.uri.URIUtil;

import io.usethesource.vallang.ISourceLocation;

public final class ReleaseNotesResolver implements ILogicalSourceLocationResolver {
    @Override
    public String scheme() {
        return "release-notes";
    }

    @Override
    public ISourceLocation resolve(ISourceLocation input) throws IOException {
        if (input.getAuthority() == null || input.getAuthority().isEmpty()) {
            return input;
        }
        
        SemVer current = new SemVer(input.getAuthority());
        
        return URIUtil.correctLocation("http", "usethesource.io", "rascal-" + current.getMajor() + "-" + current.getMinor() + "-x-release-notes");
    }

    @Override
    public String authority() {
        return "";
    }
}