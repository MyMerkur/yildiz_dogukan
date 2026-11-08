# apgr assignment 01

this project is for the first apgr assignment
the program will draw lines and polygons on a raster canvas

## features

the program draws line segments with my bresenham rasterization code
it creates closed polygons from mouse points
it has seed fill with separate background color and border color restriction modes
it has scanline polygon fill with sorted intersections
it uses sutherland hodgman clipping with a convex clipping polygon

## controls
press L to select line drawing mode
drag with the left mouse button to draw a line
hold shift while dragging for horizontal vertical or diagonal lines
press P to select polygon drawing mode
in polygon mode drag with the left mouse button and release it to add a point
the red preview connects the new point to the first and last polygon points
press enter to close a polygon with three or more points
middle click to use seed fill
press B to switch between background color restriction and border color restriction
press f to fill the last closed polygon with green scanline fill
make two closed polygons and press k to clip the first one by the last one
the clipping polygon is yellow and the clipped result is cyan
press c to clear the canvas and all saved drawing data


## run

compile the java files in the src folder
run the Main class
the project only needs a java development kit

## ai use

i did not use ai to write code. i used it only to review the assignment according to the assignment guidelines, to avoid doing any part incorrectly due to a potential misunderstanding
