/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bert Lisser    - Bert.Lisser@cwi.nl
 *******************************************************************************/
module util::HtmlDisplay
import lang::dot::Dot;
import lang::json::IO;
import analysis::statistics::BarChart;


@javaClass{org.rascalmpl.eclipse.library.util.HtmlDisplay}
@reflect{Uses URI Resolver Registry}
public java void htmlDisplay(loc location, str htmlInput); 

/* For Example  the data:
    
    Name  Age Sex
    Piet  25   M
    Anne  40   V
    
    must input as [(Name:Piet, Age:25, Sex:M), (Name:Anne, Age:40, Sex:F)]
 */
 
@javaClass{org.rascalmpl.eclipse.library.util.HtmlDisplay}

public java void htmlDisplay(loc location); 

public void drawBarChart(loc location, str title, str x, str y, list[map[str, value]] dat,
 value series) {
       barChart(location, title, x, y, dat, series);
       htmlDisplay(location);
       }
 
