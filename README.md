# apgr assignment 01

this project is for the first apgr assignment

the program will draw lines and polygons on a raster canvas

## current state

the project has a window and a raster canvas

the program can draw line segments with the mouse

the line uses my bresenham rasterization code

hold shift while dragging for horizontal vertical or diagonal lines

press c to clear the canvas and saved lines

the red line is a preview while the mouse is moving

right click to add polygon points

move the mouse to see the red polygon edge preview

press enter to close a polygon with three or more points

middle click on the background to use seed fill

the blue fill stops at the white border

press f to fill the last closed polygon with scanline fill

the green scanline fill sorts intersections and redraws the border

make two closed polygons and press k to clip the first one by the last one

the clipping polygon must be convex and its direction is found automatically

the clipping polygon is yellow and the clipped result is cyan

## run

compile the java files in the src folder

run the Main class

## ai use

i did not use ai to write code. i used it only to review the assignment according to the assignment guidelines, to avoid doing any part incorrectly due to a potential misunderstanding
