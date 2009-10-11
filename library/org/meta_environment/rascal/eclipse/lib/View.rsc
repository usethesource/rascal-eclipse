module View

// Various views on values

@doc{Show the string representation of a value in a text editor}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java graphView(value v);

@doc{Show any value as a hierarchical graph}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java textView(value v);

@doc{Show a collapsable tree of a value}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java treeView(value v);

// Various charting functions

// TODO The following should be synchronized with the declarations in rascal -> Chart.rsc
// with all @javaClass annotations adapted.

data chartSetting =            // supported by
                               // barChart pieChart xyChart histogram boxplot
     area()                    //                   x
   | dim3()                    // x        x                              
   | domainLabel(str txt)      // x                 x       x         x
   | horizontal()              // x                 x       x         x
   | noSectionLabels()         //          x
   | rangeLabel(str txt)       // x                 x       x         x
   | ring()                    //          x
   | scatter()                 //                   x
   | stacked()                 // x  
   | subtitle(str txt)         // x        x        x       x         x
   | vertical()                // x                 x       x         x

   ;

// Input data for charts based on (x,y) coordinates (xychart):
// Each series has a name and a list of xy values; values may be int or real

public alias intSeries  = tuple[str name,list[int]  xyvalues];
public alias realSeries = tuple[str name,list[real] xyvalues];

// Input data for charts based on input categories (barChart, histogram)
// Each series has a name, and a list of <category-name, value> tuples; values maybe int or real

public alias intCategorySeries  = tuple[str name,list[tuple[str category, int val]] values];
public alias realCategorySeries = tuple[str name,list[tuple[str category, real val]] values];

// Some charts need multiple values instead of a single one:

public alias intCategorySeriesMultipleData  = tuple[str name,list[tuple[str category, list[int] values]] allvalues];
public alias realCategorySeriesMultipleData = tuple[str name,list[tuple[str category, list[real] values2]] allvalues];

// barchart

@doc{draw a bar chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java barChart(str title, map[str,int] facts, value settings...);

@doc{draw a bar chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java barChart(str title, map[str,real] facts, value settings...);

@doc{draw a bar chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java barChart(str title, list[str] categories, list[intSeries] facts, value settings...);

@doc{draw a bar chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java barChart(str title, list[str] categories, list[realSeries] facts, value settings...);

// boxplot aka BoxAndWiskerPlot

@doc{draw a boxplot}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java boxplot(str title, list[intCategorySeriesMultipleData] facts, value settings...);

@doc{draw a boxplot}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java boxplot(str title, list[realCategorySeriesMultipleData] facts, value settings...);

// histogram

@doc{draw a histogram}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java histogram(str title, list[intSeries] facts, int nbins, value settings...);

@doc{draw a histogram}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java histogram(str title, list[realSeries] facts, int nbins, value settings...);

//piechart

@doc{draw a pie chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java pieChart(str title, map[str,int] facts, value settings...);  // TODO value -> chartSetting

@doc{draw a pie chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java pieChart(str title, map[str,real] facts, value settings...);  // TODO value -> chartSetting

// xyChart

@doc{draw an xy chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java xyChart(str title, list[intSeries] facts, value settings...);

@doc{draw an xy chart}
@javaClass{org.meta_environment.rascal.eclipse.lib.View}
public void java xyChart(str title, list[realSeries] facts, value settings...);
