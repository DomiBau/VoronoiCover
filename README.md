# VoronoiCover
Implements the Voronoi Cover, a type of Set-Cover with extra restrictions.

# Input
The program requires an input-file (resources/input.txt) of the following format:

numberOfCircles,maxRadius\n

followed by the coordinates of circle-centers (x,y\n)

numberOfPoints\n

followed by the coordinates of the points (x,y\n)

You can check the input-file of this project as an example.

# Distance-function
Currently the distance-function implements euclidean distance.
You can modify the distance-function "getDistanceBetweenPoints" in VoronoiCover.java to change this.
