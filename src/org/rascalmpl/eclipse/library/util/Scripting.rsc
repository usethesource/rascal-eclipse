module util::Scripting

@javaClass{org.rascalmpl.eclipse.library.util.scripting.CommandScripter}
public void java execute(list[str] commands, list[loc] projects, bool closeConsoles, int timeout);