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
      if (args.length != 2) {
         System.err.println("Invalid arguments. Correct use is: java EMST <path/to/file.txt> <alpha parameter>");
         return;
      }

      String filePath = args[0];
      double alpha    = 0.0;
      
      // Error parsing alpha 
      try {
         alpha = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
         System.err.println("Can't interpret alpha as double");
         return;
      }

      EMST emst;

      try {
         emst = new EMST(filePath, alpha);
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

   // Output informations
   private double totalWeight;
   private ArrayList<Edge> treeEdges;

   // Helper data structures  
   private HashMap<Integer, ArrayList<Point>> grid;   // Grid subdivision of the plane. Each grid has dimensions alpha x alpha    
   private PriorityQueue<Edge> minPQ;                 // Priority Queue for Prim's algorithm

   // Initializes by reading the input file, constructs the grid and runs Prim's algorithm 
   public EMST(String filePath, double a) throws Exception {

      alpha = a;

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
