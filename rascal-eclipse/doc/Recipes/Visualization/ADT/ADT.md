---
title: ADT
---

#### Synopsis

Visualize an Algebraic Datatype as a tree.

#### Syntax

#### Types

#### Function

#### Description

In ((ColoredTrees)) we have discussed the Algebraic Data Type `ColoredTree`.
Here we show how to create a visualization for them. The global approach is:

*  Define a function `visColoredTree` that has a ColoredTree as argument and 
  creates a `Figure` for it.
*  Display the resulting figure using [$Rascal:Render/render].

#### Examples

Here is our solution:
```rascal
include::{LibDir}demo/vis/VisADT.rsc[tags=module]
```

<1> A `leaf` is represented as its number converted to text, surrounded by a lightyellow box.
<2> The figure for non-leaf nodes of a ColoredTree is generated by the auxiliary function `visNode`.
<3> `visNode` represents the node itself as a [$Rascal:Figures/tree] that has a colored ellipse as root and the visualization of
    two ColoredTrees as children.


For the example `ColoredTree` `rb` we can set a standard 
(see []((Library:std))) [size]((Library:Properties-size)) and standard [gap]((Library:Properties-gap)):
```rascal-figure,width=,height=,file=a1.png
                import demo::vis::VisADT;
render(space(visColoredTree(rb), std(size(30)), std(gap(30))));
```
and the result is:


![]((a1.png))


Note that:

*  We place the Figure that is produced by `viscoloredTree` in a `space` for the sole purpose that add extra properties to it.
*  We use `std(size(30))` and ` std(gap(30))` to achieve that these properties are set for all subfigures.


Some further custumizations are possible. By default, the tree visualization 
uses [manhattan]((Library:Properties-manhattan)) style. If we turn it off
```rascal-figure,width=,height=,file=a2.png
                import demo::vis::VisADT;
render(space(visColoredTree(rb), std(size(30)), std(gap(30)), std(manhattan(false))));
```
the result is:


![]((a2.png))


It is also possible to change the [orientation]((Library:Properties-orientation)) of the tree and draw it, for example, from left to right:
```rascal-figure,width=,height=,file=a3.png
                import demo::vis::VisADT;
render(space(visColoredTree(rb), std(size(30)), std(gap(30)), std(orientation(leftRight()))));
```
the result is:


![]((a3.png))


#### Benefits

#### Pitfalls

