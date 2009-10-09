module View

// TODO This should be synchronized with the data declaration in Chart.rsc

data chartSetting =            // supported by
                               // barChart pieChart xyChart
     area()                    //                   x
   | dim3()                    // x        x                              
   | domainLabel(str txt)      // x                 x
   | rangeLabel(str txt)       // x                 x
   | horizontal()              // x        x        x
   | noLegend()                // x        x        x
   | noSectionLabels()         //          x
   | noToolTips()              // x        x        x
   | ring()                    //          x
   | seriesLabels(list[str] s) // x
   | stacked()                 // x  
   | subtitle(str txt)         // x        x        x  
   | vertical()                // x        x        x

   ;
    

@doc{Show any value as a hierarchical graph}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java graphView(value v);

@doc{Show a bar chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java barChart(str label, value facts, value settings ...);

@doc{Show a pie chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java pieChart(str label, value facts, value settings ...);

@doc{Show a XY chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java xyChart(str label, value facts, value settings ...);

@doc{Show the string representation of a value in a text editor}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java textView(value v);

@doc{Show a collapsable tree of a value}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java treeView(value v);

