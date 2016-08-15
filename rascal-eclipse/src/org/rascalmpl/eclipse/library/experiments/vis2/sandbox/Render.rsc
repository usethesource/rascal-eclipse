module experiments::vis2::sandbox::Render
import util::HtmlDisplay;
import experiments::vis2::sandbox::FigureServer;
import experiments::vis2::sandbox::Figure;
import experiments::vis2::sandbox::IFigure;

public void render(Figure fig1, int width = 800, int height = 800, 
     Alignment align = <0.5, 0.5>, tuple[int, int] size = <0, 0>,
     str fillColor = "none", str lineColor = "black", bool debug = false,  
     Event event = on(nullCallback), int borderWidth = -1, str borderStyle = "", str borderColor = ""
     ,int lineWidth = -1, bool resizable = true, str cssFile="")
     {
     renderWeb(fig1, width = width,  height = height,  align = align, fillColor = fillColor
     , lineColor = lineColor, lineWidth = lineWidth, size = size, event = event
     , borderWidth = borderWidth, borderStyle = borderStyle, borderColor=borderColor
     , resizable = resizable, defined = (width? && height?)||(size?), cssFile = cssFile, debug = debug);
     // println(toString());
     htmlDisplay(getSite());
     }