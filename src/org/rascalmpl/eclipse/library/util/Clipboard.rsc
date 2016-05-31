@doc{
.Synopsis
Access to the system's copy and paste clipboard

.Description
Using the functions in this library module you can copy content to the userâ€™s clipboard and get it back from the clipboard.
}
module util::Clipboard

@javaClass{org.rascalmpl.eclipse.library.util.Clipboard}
@doc{
.Synopsis
Copy a value to the user's clipboard.

.Description

This uses Java standard library functionality to copy contents to the clipboard. The
string which is copied is equal to what is printed using the [print] function of [$Rascal/Libraries/Prelude/IO].

.Examples

[source,rascal-shell]
----
import util::Clipboard;
copy([1,2,3]);
paste();
----
}
public java void copy(value contents);

@javaClass{org.rascalmpl.eclipse.library.util.Clipboard}
@doc{
.Synopsis
Copy a value to the user's clipboard.

.Description

This uses Java standard library functionality to copy contents from the clipboard. The
string which is retrieved is the exact contents of the clipboard if the content is indeed
representable as a string.

.Examples

[source,rascal-shell]
----
import util::Clipboard;
copy("Hello clipboard!");
paste();
----
}
public java str paste();