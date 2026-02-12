import java.io.FileReader;
import java.util.*;

public class EMST {
   
/*
Struttura del file (dall'alto verso il basso) :
   - Metodo main
   - Implementazione dell'algoritmo di Prim
   - Altri metodi di supporto
   - Classi di supporto
*/

// --------------------------------------- MAIN ---------------------------------------
   public static void main(String[] args) {
      
      // Argomenti da riga di comando non validi
      if (args.length != 2) {
         System.err.println("Argomenti non validi. Uso corretto: java EMST <percorso file input> <parametro alfa>");
         return;
      }

      String filePath = args[0];
      double alpha    = 0.0;
      
      // Alfa non può essere interpretato come double
      try {
         alpha = Double.parseDouble(args[1]);
      } catch (NumberFormatException e) {
         System.err.println("Errore nella lettura del parametro alfa");
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

// -------------------------------- IMPLEMENTAZIONE STRATEGIA DI PRIM -------------------------------

   // Informazioni ottenute dal file di input
   private ArrayList<Point> points;                   
   private double alpha;
   private int vCount;            

   // Informazioni per l'output
   private double totalWeight;
   private ArrayList<Edge> treeEdges;

   // Struturre di appoggio  
   private HashMap<Integer, ArrayList<Point>> grid;   // suddivisione del piano n*n in caselle di dimensione alfa*alfa   
   private PriorityQueue<Edge> minPQ;                 // pq per l'algoritmo di Prim

   // Inizializza leggendo i punti dal file, costruisci la griglia di supporto ed esegui l'algoritmo di Prim
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

   // Determina EMST con l'algoritmo di Prim 
   private void PrimEMST () {

      treeEdges   = new ArrayList<>();
      totalWeight = 0.0;

      minPQ = new PriorityQueue<>();

      // Scegliamo di partire dal primo punto
      Point startPoint = points.get(0);
      visitPointNeighborhood(startPoint);

      while (!minPQ.isEmpty() && treeEdges.size() < vCount-1) {
         
         Edge minEdge = minPQ.poll();

         Point u = minEdge.first;
         Point v = minEdge.second;

         // entrambi i punti sono già conosciuti, salta questo arco
         if (u.inEMST && v.inEMST) continue;

         treeEdges.add(minEdge);
         totalWeight += minEdge.weight;

         // Determina quale dei due punti è quello nuovo e controlla i suoi vicini
         Point newPoint = u.inEMST ? v : u;
         visitPointNeighborhood(newPoint);

      }

   }
   
   // Determina in quale casella si trova e p e controlla le sue distanze dai punti 
   // nelle caselle vicine (gli unici che potrebbero essere entro la distanza desiderata)
   private void visitPointNeighborhood(Point p) {

      p.inEMST = true;

      int xCell = (int)(p.xPos/alpha);
      int yCell = (int)(p.yPos/alpha);

      // Controlla 9 caselle (quella di p e le 8 vicine)
      // una casella è di dimensione alfa*alfa, per cui contiene al più alfa^2 punti
      // => costo O(alfa^2)
      for (int xShift = -1; xShift <= 1; xShift++) {
         for (int yShift = -1; yShift <= 1; yShift++) {

            int neighborCellKey = Objects.hash(xCell + xShift, yCell + yShift);

            if (grid.containsKey(neighborCellKey)) {
               ArrayList<Point> neighborPoints = grid.get(neighborCellKey);

               for ( Point nb : neighborPoints ) {

                  // Salta il punto stesso e quelli già nell'EMST
                  if (nb == p || nb.inEMST) continue;
 
                  Edge candidate = new Edge(p, nb);

                  // Aggiungi i candidati alla coda solo se sono validi
                  if (candidate.weight <= alpha) {
                     minPQ.add(candidate);
                  }

               }
            }

         }
      }

   }

   // --------------------------------------- METODI DI SUPPORTO ---------------------------------------

   // Legge i punti dal file di input
   private void parsePoints (String filePath) {

      try (Scanner fileScan = new Scanner(new FileReader(filePath)) ) {

         while (fileScan.hasNextLine()) {
            
            // Scansiona il prossimo punto (ignora parentesi e virgola)
            Scanner pointScan = new Scanner(fileScan.nextLine());
            pointScan.useDelimiter("[(),\\s]");
            
            points.add( new Point(pointScan.nextInt(), pointScan.nextInt()) );
            
            pointScan.close();
         }

      } catch (Exception e) { 
         System.err.println(e);
      }

   }

   // Riempie la griglia di supporto : punti nella stessa casella finiscono nello stesso bucket
   private void fillGrid () {

      for (Point p : points) {
         int xCell = (int)(p.xPos/alpha);
         int yCell = (int)(p.yPos/alpha);

         int cellKey = Objects.hash(xCell,yCell);

         grid.putIfAbsent(cellKey, new ArrayList<Point>());
         grid.get(cellKey).add(p);
      }

   }

   // Per output finale
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

   // Distanza euclidea
   private static double euclideanDist (Point a, Point b) {

      double xDiff = a.xPos - b.xPos;
      double yDiff = a.yPos - b.yPos;

      return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
   }

   // --------------------------------------- CLASSI DI SUPPORTO ---------------------------------------

   // Rappresentazione di un punto
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

   // Rappresentazione di un arco
   static class Edge implements Comparable<Edge> {

      Point first, second;
      double weight;

      public Edge (Point aPoint, Point bPoint) {
         first = aPoint;
         second = bPoint;
         
         weight = euclideanDist(aPoint, bPoint);
      }

      // Per far si che la minPQ funzioni
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