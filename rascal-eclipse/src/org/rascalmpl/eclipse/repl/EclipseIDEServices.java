package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.IDEServices;

import io.usethesource.vallang.ISourceLocation;

public class EclipseIDEServices implements IDEServices {

	@Override
	public void startJob(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startJob(String name, int totalWork) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startJob(String name, int workShare, int totalWork) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void event(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void event(String name, int inc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void event(int inc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int endJob(boolean succeeded) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void todo(int work) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warning(String message, ISourceLocation src) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void browse(URI uri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edit(Path path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void watch(Path dir) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unwatch(Path dir) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unwatchAll() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterable<Path> fileChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean anyFileChanges() {
		// TODO Auto-generated method stub
		return false;
	}

}
