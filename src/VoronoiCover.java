import Geometry.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class VoronoiCover {

    static List<Point> points;
    static List<Circle> circles;
    static double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
    static double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    static int numberGridX;
    static int numberGridY;
    static List<Circle>[][] grid;
    static List<Circle>[][] gatheredGrid;
    static List<Circle>[][] coverGrid;
    static double diameter;
    static boolean[][] noChangePossible;
    static List<Circle> setCoverList;
    static boolean threadFoundSolution = false;
    static boolean foundReplacement = true;

    //nice to know variables
    static boolean showTheseVariablesInOutput = false;
    static double maxRadius = 0.0;
    static double minRadius = Double.POSITIVE_INFINITY;
    static int maxNumberPointsInCircle = 0;
    static int minNumberPointsInCircle = Integer.MAX_VALUE;
    static int maximalCovered = 0;
    static double averageCovered = 0;

    //Set these Variables the way you like them
    //Higher k means better solution but more Runtime (recommended: 2)
    static int k = 2;
    //Variables to take only some (random) points/circles of the Input
    //Set this higher than the number of circles/points in the input to take all
    //The Number is the expected number of points/circles since it is random
    static int numCirclesToTake = 5000;
    static int numPointsToTake = 5000;
    //Set takeSame to true if you want to take the same points as center of the circles and points for the Set-Cover
    //If takeSame is true numPointsToTake has no effect
    static boolean takeSame = false;

    //Sets the Bound of Maximum Points a Circle can cover
    static int numberPointsPerCircle = 300;
    //Only for Multithreaded Solution (currently not implemented)
    static int maxNumOfThreads = 8;
    //Print a counter every Round to see how far the algorithm is?
    static boolean printCounterEveryRound = true;

    public static void main(String[] args) throws IOException {

        circles = new ArrayList<>(numCirclesToTake);
        points = new ArrayList<>(numPointsToTake);

        String inputFilePath = "resources/input.txt";
        String outputFilePathForTakenPoints = "resources/points.csv";
        parseInput(inputFilePath, outputFilePathForTakenPoints);

        initiateGrid();
        putCirclesInGrid();

        long startTime = System.nanoTime();
        int counter = 0;


        for(int i = 1; i <= k; i++){
            int distToEdgeOfGrid = Math.max(0,2*i-2);
            //nXs=O(xDistance/(2*radius)
            List<Integer> xValues = java.util.stream.IntStream.rangeClosed(0, numberGridX-1-distToEdgeOfGrid).boxed().collect(Collectors.toList());
            //nYs=O(yDistance/(2*radius)
            List<Integer> yValues = java.util.stream.IntStream.rangeClosed(0, numberGridY-1-distToEdgeOfGrid).boxed().collect(Collectors.toList());
            List<Coordinate> coor = new LinkedList<>();
            for(int x: xValues) {//nXs*nYs
                for (int y : yValues) {
                    coor.add(new Coordinate(x,y));
                }
            }
            updateGatheredGrid(i);//O((i-1)^2*numberOfCircles)
            noChangePossible = new boolean[numberGridX][numberGridY];//nXs*nYs
            foundReplacement = true;
            while(foundReplacement){
                foundReplacement = false;
                Collections.shuffle(coor);
                if(printCounterEveryRound){
                    System.out.println("Round " + counter + " with i = " + i + " and " + points.size() + " Points");
                }
                Iterator<Coordinate> iterator = coor.iterator();
                List<Thread> threads = new LinkedList<>();//Multi-Thread-Solution currently not implemented, because it has some race conditions
				int cellsPerThread = (int)Math.ceil((coor.size()+0.0)/(maxNumOfThreads+0.0));																			 
                while(iterator.hasNext()){//nXs*nYs*innerLoop
                    List<Coordinate> acCoor = new LinkedList<>();
                    for(int ii = 0; ii < cellsPerThread; ii++){
                        if(iterator.hasNext()){
                            acCoor.add(iterator.next());
                        }else{
                            break;
                        }
                    }
                    int finalI = i;
					//if(acCoor.size()>0){					  
                    //Thread t = new Thread(() -> {
                        for(Coordinate co: acCoor){
                            int x = co.getX();
                            int y = co.getY();
                            if(!noChangePossible[x][y]){
                                if(gatherInGrid(x, y, finalI)){
                                    foundReplacement = true;
                                }else if(!threadFoundSolution){
                                    noChangePossible[x][y] = true;
                                }
                            }
                        }
                    //});
                    //threads.add(t);
                    //t.start();
                }
                //wait for every thread
                /*for(Thread t: threads){
                    if(t!=null){
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                threadFoundSolution = false;
                counter++;
            }
        }

        long duration = System.nanoTime()-startTime;
        double seconds = (double)duration/1000000000;
        System.out.println("Time: " + seconds);
        String outputPath = "resources/solution.csv";
        manageOutput(outputPath);
    }

    private static void parseInput(String inputFilePath, String outputFilePath) throws IOException {
        /*Input has to have the form of:
        numberOfCircles,radius//Followed by the coordinates of the circle-centers
        x1,y1
        ...
        xn,yn
        numberOfPoints//Followed by the coordinates of the points
        x1,y1
        ...
        xm,ym
        **/
        BufferedReader br = new BufferedReader(new FileReader(new File(inputFilePath)));
        String line = br.readLine();
        FileWriter fwr = new FileWriter(new File(outputFilePath));
        int numCircles = Integer.parseInt(line.split(",")[0]);
        double radius = Double.parseDouble(line.split(",")[1]);
        diameter = 2 * radius;
        double prob = ((double)numCirclesToTake) / numCircles;
        int cid = 1;
        int pid = 1;
        for(int i = 0; i < numCircles; i++){
            String[] splitLine = br.readLine().split(",");
            if(Math.random() <= prob){
                double x = parseX(splitLine[0]);
                double y = parseY(splitLine[1]);
                insertCircle(x, y, radius, cid);
                cid++;
                if(takeSame){
                    insertPoint(x, y, pid);
                    pid++;
                }
            }

        }
        if(!takeSame){
            int numPoints = Integer.parseInt(br.readLine());
            prob = ((double)numPointsToTake)/numPoints;
            for(int i = 0; i < numPoints; i++){
                String[] splitLine = br.readLine().split(",");
                if(Math.random()<=prob){
                    double x = parseX(splitLine[0]);
                    double y = parseY(splitLine[1]);
                    insertPoint(x, y ,pid);
                    pid++;
                }
            }
        }
        for(Point p: points){
            for(Circle c: circles){
                double distance = getDistanceBetweenPoints(p.getX(), p.getY(), c.getX(), c.getY());
                if(distance < c.getRadius()){
                    c.addToAllPointInside(new PointWithDistance(p, distance));
                    CircleWithDistance cWD = new CircleWithDistance(c, distance);
                    p.addToAllCirclesInRange(cWD);
                    p.addToNearbyCircles(cWD);
                }
            }
        }
        for(Circle c: circles){
            c.sortAllPointsInside();
        }
        for(Point p: points){
            CircleWithDistance cWD = p.getClosestPoint();
            cWD.getC().addPointInside(new PointWithDistance(p, cWD.getDistance()));
        }
        fwr.write("x,y\n");
        for(Circle c: circles){
            fwr.write(c.getX() + "," + c.getY() + "\n");
        }
        fwr.close();
    }

    private static void initiateGrid(){
        numberGridX = (int) Math.ceil((maxX - minX)/(diameter));
        numberGridY = (int) Math.ceil((maxY - minY)/(diameter));
        grid = new List[numberGridX][numberGridY];
        coverGrid = new List[numberGridX][numberGridY];
        gatheredGrid = new List[numberGridX][numberGridY];
        noChangePossible = new boolean[numberGridX][numberGridY];
        for(int x = 0; x < numberGridX; x++){
            for(int y = 0; y < numberGridY; y++){
                grid[x][y] = new LinkedList<>();
                coverGrid[x][y] = new LinkedList<>();
            }
        }
    }

    private static void putCirclesInGrid(){
        for(Circle c: circles){
            int pGridX = (int)((c.getX()-minX)/(diameter));
            int pGridY = (int)((c.getY()-minY)/(diameter));
            c.setGridX(pGridX);
            c.setGridY(pGridY);
            List<Circle> cur = grid[pGridX][pGridY];
            cur.add(c);
            coverGrid[pGridX][pGridY].add(c);
        }
    }

    private static void updateGatheredGrid(int i){//O((i-1)^2*numberOfCircles)
        int distToEdge = Math.max(0,2*i-2);
        gatheredGrid = new List[numberGridX-distToEdge][numberGridY-distToEdge];
        for(int x = 0; x < numberGridX - distToEdge; x++){
            for(int y = 0; y < numberGridY - distToEdge; y++){
                gatheredGrid[x][y] = new LinkedList<>();
                for(int innerX = x; innerX <= x + distToEdge; innerX++){
                    for(int innerY = y; innerY <= y +  distToEdge; innerY++){
                        gatheredGrid[x][y].addAll(grid[innerX][innerY]);
                    }
                }
            }
        }
    }

    private static double parseX(String xString){
        double x = Double.parseDouble(xString);
        if(x < minX)
            minX = x;
        if(x > maxX)
            maxX = x;
        return x;
    }

    private static double parseY(String yString){
        double y = Double.parseDouble(yString);
        if(y < minY)
            minY = y;
        if(y > maxY)
            maxY = y;
        return y;
    }

    private static void insertCircle(double x, double y, double radius, int id){
        circles.add(new Circle(x, y, radius, new HashSet<>(), id));
    }

    private static void insertPoint(double x, double y, int id){
        points.add(new Point(x, y, id));
    }

    private static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private static boolean gatherInGrid(int x, int y, int i){
        List<Circle> gridCircles = gatheredGrid[x][y];
        List<Circle> coverCircles = new LinkedList<>();
        int distToEdgeOfGrid = Math.max(0,2*i-2);
        for(int innerX = x; innerX <= x + distToEdgeOfGrid; innerX++) {//O(numberOfCircles)
            for (int innerY = y; innerY <= y + distToEdgeOfGrid; innerY++) {
                coverCircles.addAll(coverGrid[innerX][innerY]);
            }
        }
        Collections.shuffle(gridCircles);//O(numberOfCircles)
        Collections.shuffle(coverCircles);//O(numberOfCircles)
        if(threadFoundSolution){
            return false;
        }
        return takeICirclesOutOfCoverGrid(gridCircles, coverCircles, x, y, i);
    }

    private static boolean takeICirclesOutOfCoverGrid(List<Circle> gridCircles, List<Circle> coverCircles, int x, int y, int i){//O((numberOfCircles choose i)*findGridReplacementRuntime)
        List<CircleWithIndex> toReplacePoints = new LinkedList<>();
        ListIterator<Circle> it = coverCircles.listIterator();
        while(it.hasNext()){
            if(threadFoundSolution){
                return false;
            }
            Circle c = it.next();
            if (toReplacePoints.size() >= i) {
                toReplacePoints.remove(i - 1);
            }
            toReplacePoints.add(new CircleWithIndex(c,it.previousIndex()));
            if(toReplacePoints.size() == i && findGridReplacement(gridCircles, toReplacePoints, x, y, i)){
                return true;
            }
            if(threadFoundSolution){
                return false;
            }
            if(!it.hasNext()){
                CircleWithIndex cur = toReplacePoints.get(toReplacePoints.size()-1);
                while(cur.getIndex() == coverCircles.size()-1){
                    toReplacePoints.remove(cur);
                    if(toReplacePoints.size()==0){
                        return false;
                    }
                    cur = toReplacePoints.get(toReplacePoints.size()-1);
                }
                cur = toReplacePoints.remove(toReplacePoints.size()-1);
                it = coverCircles.listIterator(cur.getIndex()+1);
            }
        }
        return false;
    }

    private static boolean findGridReplacement(List<Circle> gridCircles, List<CircleWithIndex> toReplaceCircles, int x, int y, int i){
        if(threadFoundSolution){
            return false;
        }
        HashMap<Point, Integer> needToBeCovered = new HashMap<>();
        HashMap<Circle, LinkedList<PointWithDistance>> wherePointsGoAfterRemovalOfCircles =
                getWherePointsGoAfterRemovalOfCircles(toReplaceCircles, needToBeCovered);
        HashMap<Circle, Integer> numOfPointsToTakeAway = new HashMap<>();
        getHowManyPointsToTakeAway(wherePointsGoAfterRemovalOfCircles, numOfPointsToTakeAway);
        List<CircleWithIndex> replacement = new LinkedList<>();
        if(numOfPointsToTakeAway.isEmpty() && needToBeCovered.isEmpty()){
            replaceInGrid(toReplaceCircles, replacement, x, y, i);
            return true;
        }
        if(toReplaceCircles.size()==1){
            return false;
        }
        HashMap<Circle, Integer> whereInCoveredByOldIsCircle = new HashMap<>();
        List<List<CoveredPoint>> coveredByOld = new LinkedList<>();
        List<List<CoveredPoint>> coveredByNew = new LinkedList<>();
        buildWhereInCoveredByOldIsCircle(numOfPointsToTakeAway, coveredByOld, whereInCoveredByOldIsCircle);
        //Collections.shuffle(gridCircles);
        ListIterator<Circle> it = gridCircles.listIterator();
        while(it.hasNext()){
            if(threadFoundSolution){
                return false;
            }
            Circle c = it.next();
            if(!c.isCurrentlyInCover()){
                if (replacement.size() >= toReplaceCircles.size()-1) {
                    replacement.remove(replacement.size()-1);
                    removeUncoveredFromCovered(needToBeCovered, whereInCoveredByOldIsCircle, coveredByOld, coveredByNew);
                }
                List<CoveredPoint> coveredByNewList = new LinkedList<>();
                coveredByNew.add(coveredByNewList);
                replacement.add(new CircleWithIndex(c,it.previousIndex()));
                for(PointWithDistance pWD: c.getAllPointsInside()){
                    if(threadFoundSolution){
                        return false;
                    }
                    if(needToBeCovered.containsKey(pWD.getP())){
                        coveredByNewList.add(new CoveredPoint(null, pWD.getP(), c, pWD.getDistance()));
                        int val = needToBeCovered.get(pWD.getP());
                        needToBeCovered.put(pWD.getP(), val+1);
                    }else{
                        CircleWithDistance oldC = pWD.getP().getClosestPoint();
                        try{
                            if(pWD.getDistance() < oldC.getDistance()){
                                CoveredPoint cP = new CoveredPoint(oldC.getC(), pWD.getP(), c, pWD.getDistance());
                                addToCoveredByOld(whereInCoveredByOldIsCircle, coveredByOld, cP);
                                coveredByNewList.add(cP);
                            }
                        }catch (NullPointerException e){
                            System.err.println(pWD.getP().getId());
                            System.exit(0);
                        }
                    }
                }
                if(threadFoundSolution){
                    return false;
                }
                //If all Points are covered and the critical and new Circles do net exceed the pointLimit take this replacement
                if(allCoveredAndNotToManyPoints(numOfPointsToTakeAway, needToBeCovered, coveredByOld, coveredByNew)){
                    replaceInGrid(toReplaceCircles, replacement, x, y, i);
                    return true;
                }

            }
            if(!it.hasNext()&&replacement.size()>0){
                CircleWithIndex cur = replacement.get(replacement.size()-1);
                while(cur.getIndex() == gridCircles.size()-1){
                    removeUncoveredFromCovered(needToBeCovered, whereInCoveredByOldIsCircle, coveredByOld, coveredByNew);
                    replacement.remove(cur);
                    if(replacement.size()==0){
                        return false;
                    }
                    cur = replacement.get(replacement.size()-1);
                }
                cur = replacement.remove(replacement.size()-1);
                removeUncoveredFromCovered(needToBeCovered, whereInCoveredByOldIsCircle, coveredByOld, coveredByNew);
                it = gridCircles.listIterator(cur.getIndex()+1);
            }
        }
        return false;
    }


    private static void getHowManyPointsToTakeAway(HashMap<Circle, LinkedList<PointWithDistance>> wherePointsGoAfterRemovalOfCircles, HashMap<Circle, Integer> numOfPointsToTakeAway){
        for(Map.Entry<Circle, LinkedList<PointWithDistance>> ent: wherePointsGoAfterRemovalOfCircles.entrySet()){
            Circle c = ent.getKey();
            int numPointsToBeInC = c.getPointsInside().size() + ent.getValue().size();
            if(numPointsToBeInC > numberPointsPerCircle){
                int toMuch = numPointsToBeInC - numberPointsPerCircle;
                numOfPointsToTakeAway.put(c,toMuch);
            }
        }
    }

    private static void buildWhereInCoveredByOldIsCircle(HashMap<Circle, Integer> numOfPointsToTakeAway, List<List<CoveredPoint>> coveredByOld, HashMap<Circle, Integer> whereInCoveredByOldIsCircle){
        int counter = 0;
        for(Map.Entry<Circle, Integer> ent: numOfPointsToTakeAway.entrySet()){
            whereInCoveredByOldIsCircle.put(ent.getKey(),counter);
            coveredByOld.add(new LinkedList<>());
            counter++;
        }
    }

    private static boolean allCoveredAndNotToManyPoints(HashMap<Circle, Integer> numOfPointsToTakeAway,
                                                        HashMap<Point, Integer> needToBeCovered,
                                                        List<List<CoveredPoint>> coveredByOld,
                                                        List<List<CoveredPoint>> coveredByNew){
        //Check Points that where not covered
        for(Map.Entry<Point, Integer> ent: needToBeCovered.entrySet()){
            if(ent.getValue() < 1){
                return false;
            }
        }
        //Check if new Circles would have too many Points
        for(List<CoveredPoint> cPList: coveredByNew){
            if(cPList.size() > numberPointsPerCircle){
                return false;
            }
        }
        //Check if Circles got too many Points while toReplaceCircles where removed
        for(List<CoveredPoint> cPList: coveredByOld){
            if(cPList.size() < 1){
                return false;
            }
            Circle oldC = cPList.get(0).getOldC();
            int duplicates = 0;
            HashMap<Point, Integer> howOftenTakenAway = new HashMap<>(); //TODO Maybe sort by points instead
            for(CoveredPoint cP: cPList){
                Point p = cP.getP();
                if(howOftenTakenAway.containsKey(p)){
                    duplicates++;
                    howOftenTakenAway.put(p, howOftenTakenAway.get(p)+1);
                } else {
                    howOftenTakenAway.put(p,1);
                }
            }
            if(cPList.size() - duplicates < numOfPointsToTakeAway.get(oldC)){
                return false;
            }
        }
        return true;
    }

    private static void removeUncoveredFromCovered(HashMap<Point, Integer> needToBeCovered,
                                                   HashMap<Circle, Integer> whereInCoveredByOldIsCircle,
                                                   List<List<CoveredPoint>> coveredByOld,
                                                   List<List<CoveredPoint>> coveredByNew) {
        List<CoveredPoint> uncovered = coveredByNew.remove(coveredByNew.size()-1);
        for(CoveredPoint cP: uncovered){
            if(cP.getOldC() == null){
                int val = needToBeCovered.get(cP.getP());
                needToBeCovered.put(cP.getP(), val - 1);
            }
            removeFromCoveredByOld(whereInCoveredByOldIsCircle, coveredByOld, cP);
        }
    }

    private static void removeFromCoveredByOld(HashMap<Circle, Integer> whereInCoveredByOldIsCircle,
                                               List<List<CoveredPoint>> coveredByOld,
                                               CoveredPoint cP){
        if(whereInCoveredByOldIsCircle.containsKey(cP.getOldC())){
            int pos = whereInCoveredByOldIsCircle.get(cP.getOldC());
            coveredByOld.get(pos).remove(cP);
        }
    }

    private static void addToCoveredByOld(HashMap<Circle, Integer> whereInCoveredByOldIsCircle,
                                          List<List<CoveredPoint>> coveredByOld,
                                          CoveredPoint cP){
        if(whereInCoveredByOldIsCircle.containsKey(cP.getOldC())){
            int pos = whereInCoveredByOldIsCircle.get(cP.getOldC());
            List<CoveredPoint> list = coveredByOld.get(pos);
            list.add(cP);
        }
    }

    private static HashMap<Circle, LinkedList<PointWithDistance>> getWherePointsGoAfterRemovalOfCircles(List<CircleWithIndex> toReplaceCircles, HashMap<Point, Integer> needToBeCovered){
        HashMap<Circle, LinkedList<PointWithDistance>> wherePointsGo = new HashMap<>();
        for(CircleWithIndex cWI: toReplaceCircles){
            if(threadFoundSolution){
                return null;
            }
            Circle c = cWI.getC();
            HashSet<PointWithDistance> pointsInside = c.getPointsInside();
            for(PointWithDistance pWD: pointsInside){
                CircleWithDistance dummy = new CircleWithDistance(c,pWD.getDistance());
                Point p = pWD.getP();
                p.removeFromNearbyCircles(dummy);
                CircleWithDistance closest = p.getClosestPoint();
                LinkedList<CircleWithDistance> addBackAgain = new LinkedList<>();
                while(closest!=null && toReplaceCircles.contains(new CircleWithIndex(closest.getC(),0))){
                    p.removeFromNearbyCircles(closest);
                    addBackAgain.add(closest);
                    closest = p.getClosestPoint();
                }
                for(CircleWithDistance cWD: addBackAgain){
                    p.addToNearbyCircles(cWD);
                }
                p.addToNearbyCircles(dummy);
                if(closest == null){
                    //This Point is not covered anymore!
                    needToBeCovered.put(p, 0);
                } else {
                    LinkedList<PointWithDistance> list;
                    if(wherePointsGo.containsKey(closest.getC())){
                        list = wherePointsGo.get(closest.getC());
                    } else {
                        list = new LinkedList<>();
                        wherePointsGo.put(closest.getC(), list);
                    }
                    list.add(new PointWithDistance(p, closest.getDistance()));
                }
            }
        }
        return wherePointsGo;
    }

    private static synchronized void replaceInGrid(List<CircleWithIndex> toReplacePoints, List<CircleWithIndex> replacement, int x, int y, int i){
        if(threadFoundSolution){
            return;
        }
        threadFoundSolution = true;
        for(CircleWithIndex cWI: toReplacePoints){
            Circle c = cWI.getC();
            c.setCurrentlyInCover(false);
            coverGrid[c.getGridX()][c.getGridY()].remove(c);
            for(PointWithDistance pWD: c.getAllPointsInside()){
                Circle prevClosest = pWD.getP().getClosestPoint().getC();
                pWD.getP().removeFromNearbyCircles(new CircleWithDistance(c, pWD.getDistance()));
                if(prevClosest.equals(c)){
                    CircleWithDistance newCircleWithDistance = pWD.getP().getClosestPoint();
                    if(newCircleWithDistance!=null){
                        newCircleWithDistance.getC().addPointInside(new PointWithDistance(pWD.getP(), newCircleWithDistance.getDistance()));
                    }
                }

            }
            c.resetPointsInside();
        }
        for(CircleWithIndex cWI: replacement){
            Circle c = cWI.getC();
            c.setCurrentlyInCover(true);
            coverGrid[c.getGridX()][c.getGridY()].add(c);
            for(PointWithDistance pWD: c.getAllPointsInside()){
                Point p = pWD.getP();
                CircleWithDistance prevClosest = p.getClosestPoint();
                p.addToNearbyCircles(new CircleWithDistance(c, pWD.getDistance()));
                CircleWithDistance closest = p.getClosestPoint();
                if(!closest.equals(prevClosest)){
                    if(prevClosest!=null)
                        prevClosest.getC().removePointInside(new PointWithDistance(pWD.getP(), prevClosest.getDistance()));
                    c.addPointInside(pWD);
                }
            }
        }
        updateMarkerArray(x, y, i);
    }

    private static void updateMarkerArray(int x, int y, int i){
        if(i>=3){
            int distToEdge = Math.max(0,2*i-2);
            for(int innerX = x-distToEdge; innerX <= x + distToEdge; innerX++){
                if(innerX>=0){
                    for(int innerY = y - distToEdge; innerY <= y + distToEdge; innerY++){
                        if(innerY>=0){
                            noChangePossible[innerX][innerY] = false;
                        }
                    }
                }
            }
        }

    }

    private static void manageOutput(String outputPath) throws IOException {
        setCoverList = new LinkedList<>();
        for(int x = 0 ; x < numberGridX; x++){
            for(int y = 0; y < numberGridY; y++){
                setCoverList.addAll(coverGrid[x][y]);
            }
        }
        FileWriter fw = new FileWriter(new File(outputPath));
        fw.write("x,y,radius,numPoints\n");
        for(Circle c : setCoverList){
            fw.write(c.getX()+","+c.getY()+"," + c.getRadius() + "," + c.getPointsInside().size() + "\n");
        }
        fw.close();
        System.out.println(setCoverList.toString());
        System.out.println(setCoverList.size());
        if(showTheseVariablesInOutput)
            niceToKnowOutput();
    }

    private static void niceToKnowOutput(){
        System.out.println("-Nice-to-know-variables-");
        long numberOfCovers = 0;
        for(Point p: points){
            int thisPointsCovers = p.getNearbyCircles().size();
            numberOfCovers += thisPointsCovers;
            if(thisPointsCovers > maximalCovered){
                maximalCovered = thisPointsCovers;
            }
        }
        averageCovered = (numberOfCovers+0.0) / (points.size()+0.0);
        System.out.println("Maximum Number of Points covered by one Circle: " + maxNumberPointsInCircle);
        System.out.println("Minimum Number of Points covered by one Circle: " + minNumberPointsInCircle);
        System.out.println("Maximum Radius: " + maxRadius);
        System.out.println("Minimum Radius: " + minRadius);
        System.out.println("");
        System.out.println("Maximum covered Point has this many covering Circles: " + maximalCovered);
        System.out.println("Average number of covering Circles: " + averageCovered);
    }
}
