@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Mark Hills - Mark.Hills@cwi.nl (CWI)}
module util::ResourceMarkers

import Message;

@doc{Remove all message markers from a resource.}
@javaClass{org.rascalmpl.eclipse.library.util.ResourceMarkers}
public java void removeMessageMarkers(loc resourceLoc);

@doc{Add message markers to a resource.}
@javaClass{org.rascalmpl.eclipse.library.util.ResourceMarkers}
public java void addMessageMarkers(set[Message] markers);