module View

// TODO This should be synchronized with the data declaration in Chart.rsc

public data chartSetting =
     xlabel(str txt)
   | ylabel(str txt)
   | horizontal()
   | vertical()
   | noSectionLabels()
   | noLegend()
   | noToolTips()
   | stacked()
   | dim3()
   | circular()
//   | background(int r, int g, int b, real alpha);
   ;

@doc{Show any value as a hierarchical graph}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java show(value v);

@doc{Show a chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java pieChart(str label, map[str, int] v, value settings ...);

@doc{Show the string representation of a value in a text editor}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java edit(value v);


