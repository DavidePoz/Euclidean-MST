import java.io.FileReader;
import java.util.*;

public class EMST {
   
/*
File structure (top to bottom)
   - Main method
   - Prim's algorithm implementation
   - Support methods
   - Support static classes
*/

// --------------------------------------- MAIN ---------------------------------------
   public static void main(String[] args) {
      
      // Check command line arguments 
      if (args.length < 2 || args.length > 3) {
         System.err.println("Invalid arguments. Correct use is: java EMST <path/to/file.txt> <alpha> [-v]");
         return;
      }

      String filePath = args[0];
      double alpha    = 0.0;
      boolean vMode   = false;
      
      // Error parsing alpha 
      try {
         alpha = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
         System.err.println("Can't interpret alpha as double");
         return;
      }

      if ( args.length == 3 && args[2].equals("-v") ) vMode = true;

      EMST emst;

      try {
         emst = new EMST(filePath, alpha, vMode);
      } catch (Exception e) {
         System.err.println(e);
         return;
      }

      System.out.println(emst);

   }

// -------------------------------- PRIM'S STRATEGY IMPLEMENTATION -------------------------------

   // Informations read from input file
   private ArrayList<Point> points;                   
   private double alpha;
   private int vCount;
   private boolean visual;

   // Output informations
   private double totalWeight;
   private ArrayList<Edge> treeEdges;

   // Helper data structures  
   private HashMap<Integer, ArrayList<Point>> grid;   // Grid subdivision of the plane. Each grid has dimensions alpha x alpha    
   private PriorityQueue<Edge> minPQ;                 // Priority Queue for Prim's algorithm

   // Initializes by reading the input file, constructs the grid and runs Prim's algorithm 
   public EMST(String filePath, double a, boolean v) throws Exception {

      alpha = a;
      visual = v;
   
      points = new ArrayList<>();
      parsePoints(filePath);
      vCount = points.size();
      
      if (vCount == 0) {
         throw new Exception("Errore durante la lettura dei punti");
      } 
         
      grid = new HashMap<>();
      fillGrid();

      PrimEMST();
   }

   // Prim's algorithm
   private void PrimEMST () {

      if (visual) setupVisuals();

      treeEdges   = new ArrayList<>();
      totalWeight = 0.0;

      minPQ = new PriorityQueue<>();

      // Start from the first point 
      Point startPoint = points.get(0);
      visitPointNeighborhood(startPoint);

      while (!minPQ.isEmpty() && treeEdges.size() < vCount-1) {
         
         Edge minEdge = minPQ.poll();

         Point u = minEdge.first;
         Point v = minEdge.second;

         // Both points are already in the EMST, skip this edge
         if (u.inEMST && v.inEMST) continue;

         treeEdges.add(minEdge);
         totalWeight += minEdge.weight;

         if (visual) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius(0.002);
            StdDraw.line(u.xPos, u.yPos, v.xPos, v.yPos);

            StdDraw.show();
            StdDraw.pause(20);
         }

         // Only check the neighbors of the new point
         Point newPoint = u.inEMST ? v : u;
         visitPointNeighborhood(newPoint);

      }
   }
   
   // Determines p's cell in the grid and computes its distances from the points
   // in neighboring cells (the only ones that may fall within the desired distance)
   private void visitPointNeighborhood(Point p) {

      p.inEMST = true;

      int xCell = (int)(p.xPos/alpha);
      int yCell = (int)(p.yPos/alpha);

      // Check the neighboring cells
      for (int xShift = -1; xShift <= 1; xShift++) {
         for (int yShift = -1; yShift <= 1; yShift++) {

            int neighborCellKey = Objects.hash(xCell + xShift, yCell + yShift);

            if (grid.containsKey(neighborCellKey)) {
               ArrayList<Point> neighborPoints = grid.get(neighborCellKey);

               for ( Point nb : neighborPoints ) {

                  // Skip p and points that are already in the EMST
                  if (nb == p || nb.inEMST) continue;
 
                  Edge candidate = new Edge(p, nb);

                  // Only add valid edges to the PQ
                  if (candidate.weight <= alpha) {
                     minPQ.add(candidate);
                  }

               }
            }

         }
      }

   }

   // --------------------------------------- SUPPORT METHODS ---------------------------------------

   // Reads points from the input file
   private void parsePoints (String filePath) {

      try (Scanner fileScan = new Scanner(new FileReader(filePath)) ) {

         while (fileScan.hasNextLine()) {
            
            Scanner pointScan = new Scanner(fileScan.nextLine());
            pointScan.useDelimiter("[(),\\s]");
            
            points.add( new Point(pointScan.nextInt(), pointScan.nextInt()) );
            
            pointScan.close();
         }

      } catch (Exception e) { 
         System.err.println(e);
      }

   }

   // Construct the grid. Points in the same cell are mapped in the same bucket
   private void fillGrid () {

      for (Point p : points) {
         int xCell = (int)(p.xPos/alpha);
         int yCell = (int)(p.yPos/alpha);

         int cellKey = Objects.hash(xCell,yCell);

         grid.putIfAbsent(cellKey, new ArrayList<Point>());
         grid.get(cellKey).add(p);
      }

   }

   private void setupVisuals () {
      int maxCoord = 0;

      for (Point p : points ) {
         if (p.xPos > maxCoord ) maxCoord = p.xPos;
         if (p.yPos > maxCoord ) maxCoord = p.yPos;
      }
      
      StdDraw.setCanvasSize(1200,1200);

      StdDraw.setXscale(-maxCoord * 0.05, maxCoord * 1.05);
      StdDraw.setYscale(-maxCoord * 0.05, maxCoord * 1.05);

      StdDraw.enableDoubleBuffering();

      StdDraw.setPenRadius(0.005);
      StdDraw.setPenColor(StdDraw.BLACK);

      for (Point p : points) {
         StdDraw.point(p.xPos, p.yPos);
      }

      StdDraw.show();
   }

   @Override
   public String toString () {
      
      if (treeEdges.size() < vCount-1) {
         return "FAIL";
      }

      String outStr = String.format(java.util.Locale.US, "%.2f", totalWeight);

      if (vCount <= 10) {
         for (Edge e : treeEdges) {
            outStr = outStr + "\n" +  e;
         }
      }
      
      return outStr;
   }

   // Euclidean distance 
   private static double euclideanDist (Point a, Point b) {

      double xDiff = a.xPos - b.xPos;
      double yDiff = a.yPos - b.yPos;

      return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
   }

   // --------------------------------------- SUPPORT CLASSES ---------------------------------------

   private static class Point {

      int xPos, yPos;
      boolean inEMST;

      public Point (int x, int y) {
         xPos = x;
         yPos = y;
         inEMST = false;
      }

      @Override
      public String toString () {
         return "(" + xPos + "," + yPos + ")";
      }
   }

   // Edge representation
   static class Edge implements Comparable<Edge> {

      Point first, second;
      double weight;

      public Edge (Point aPoint, Point bPoint) {
         first = aPoint;
         second = bPoint;
         
         weight = euclideanDist(aPoint, bPoint);
      }

      // Must be implemented to make the PQ work
      @Override
      public int compareTo (Edge other) {
         return Double.compare(weight, other.weight);
      }

      @Override
      public String toString () {
         return first.toString() + second.toString();
      }
   }

}
