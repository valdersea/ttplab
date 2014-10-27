package solver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import ttp.TTP1Instance;
import ttp.TTPSolution;
import utils.Deb;
import utils.Quicksort;

public class Constructive extends TTPHeuristic {
  
  public Constructive() {
    super();
  }
  
  public Constructive(TTP1Instance ttp) {
    super(ttp);
  }
  
  
  /**
   * construct a solution
   * 
   * @param ref
   * @return
   */
  public TTPSolution generate(String ref) {
    
    if (ref.length()<2) {
      return null;
    }
    
    int x[], z[];
    
    int h = 1;
    if (ref.length()==3) {
      h = Integer.parseInt(""+ref.charAt(2));
    }
    
    /* get tour */
    switch (ref.charAt(0)) {
      
      case 's':
        x = simpleTour();
      break;
      
      case 'r':
        x = randomTour();
      break;
      
      case 'g':
        x = greedyTour();
      break;
      
      case 'o':
        x = optimalTour();
      break;
      
      default:
        x = simpleTour();
      break;
    }
    
    /* get picking plan */
    switch (ref.charAt(1)) {
      
      case 'z':
        z = zerosPickingPlan();
      break;
      
      case 'r':
        z = randomPickingPlan(h);
      break;
      
      case 'g':
        z = greedyPickingPlan(h);
      break;
      
      default:
        z = zerosPickingPlan();
      break;
    }
    
    return new TTPSolution(x, z);
  }
  
  
  
  /**
   * generate simple tour: [1 2 3 4 ...]
   * 
   * @return the found tour
   */
  public int[] simpleTour() {
    
    int[] tour = new int[ttp.getNbCities()];
    
    for (int i=0;i<tour.length;i++) {
      tour[i] = i+1;
    }
    return tour;
  }
  
  
  /**
   * simple greedy algorithm
   * based on distances between nodes
   * 
   * @return the found tour
   */
  public int[] greedyTour() {
    
    /* TTP data */
    long[][] D = ttp.getDist();
    int m = ttp.getNbCities();
    int[] tour = new int[m];
    
    /* the tour generated using greedy algorithm */
    ArrayList<Integer> li = new ArrayList<Integer>();
    long[] raw;
    long vMin;
    int iMin;
    li.add(0, 1);
    for (int i=1;i<m;i++) {
      raw = D[i];
      iMin = 0;
      vMin = Long.MAX_VALUE;
      for (int j=1;j<m;j++) {
        if (li.contains(j+1) || i==j) {
          continue;
        }
        
        if (raw[j]<vMin) {
          vMin = raw[j];
          iMin = j+1;
        }
      }
      li.add(i, iMin);
    }
    
    for (int i=0; i<m; i++) {
      tour[i] = li.get(i);
    }
    
    return tour;
  }
  
  
  /**
   * generate random tour
   * 
   * @return
   */
  public int[] randomTour() {
    
    int[] tour = new int[ttp.getNbCities()];
    
    ArrayList<Integer> li = new ArrayList<Integer>(tour.length-1);
    for (int i=0; i<tour.length-1; i++) {
      li.add(i, i+2);
    }
    Collections.shuffle(li);
    
    tour[0] = 1;
    for (int i=1; i<tour.length; i++) {
      tour[i] = li.get(i-1);
    }
    return tour;
  }
  
  
  
  // testing ...
  public static void main(String[] args) {
    Constructive x = new Constructive(new TTP1Instance(    "./TTP1_data/eil51-ttp/eil51_n50_bounded-strongly-corr_01.ttp"));
    int[] t = x.optimalTour();
    Deb.echo(t);
  }
  
  
  /**
   * use optimal TSP tour
   * 
   * @return
   */
  public int[] optimalTour() {
    int nbCities = ttp.getNbCities();
    int[] tour = new int[nbCities];
    
    String fileName = ttp.getName().replaceAll("-.+", "");
    String dirName = "./TTP1_data/"+fileName+"-ttp";
    fileName += ".opt.tour";
    //Deb.echo(dirName + "/" + fileName);
    
    File file = new File(dirName + "/" + fileName);
    BufferedReader br = null;
    
    try {
      br = new BufferedReader(new FileReader(file));
      String line;
      
      // skip file header
      br.readLine();
      br.readLine();
      br.readLine();
      br.readLine();
      
      // scan tour
      int i = 0;
      while ((line = br.readLine()) != null && i<nbCities) {
        tour[i++] = Integer.parseInt(line);
      } // end while
      
      br.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
    return tour;
  }
  
  
  
  /**
   * empty knapsack
   * 
   * @return picking plan filled with zeros
   */
  public int[] zerosPickingPlan() {
    
    // picking plan
    int[] pp = new int[ttp.getNbItems()];
    
    for (int i=0;i<pp.length;i++) {
      pp[i] = 0;
    }
    return pp;
  }
  
  
  /**
   * greedy picking plan
   * 
   * @param h determines how much of the knapsack space will be filled
   * @return the found picking plan
   */
  public int[] greedyPickingPlan(int h) {
    
    // get TTP data
    int[] A = ttp.getAvailability();
    long capacity = ttp.getCapacity();
    int m = ttp.getNbCities(),
        n = ttp.getNbItems();
    TTPSolution s = new TTPSolution(m, n);
    int[] z = s.getPickingPlan();
    
    // maximum capacity
    long maxCapacity = capacity/h;
    
    // item scores
    double[] score = new double[n];
    
    for (int k=0; k<n; k++) {
      score[k] = ttp.profitOf(k)/ttp.weightOf(k);
    }
    
    // sort
    Quicksort qs = new Quicksort(score);
    qs.sort();
    
    int[] si = qs.getIndices();
    int wc = 0;
    for (int k=0; k<n; k++) {
      int i = si[k];
      int wi = ttp.weightOf(i);
      
      if (wi+wc <= maxCapacity) {
        z[i] = A[i];
        wc += wi;
      }
      else {
        break;
      }
    }
    
    return z;
  }
  
  /**
   * greedy picking plan (h=1)
   * 
   * @return the found picking plan
   */
  public int[] greedyPickingPlan() {
    return greedyPickingPlan(1);
  }  
  
  
  /**
   * random picking plan
   * @return
   */
  public int[] randomPickingPlan(int h) {
    
    // get TTP data
    int[] A = ttp.getAvailability();
    long capacity = ttp.getCapacity();
    int n = ttp.getNbItems();
    
    // maximum capacity
    long maxCapacity = capacity/h;
    
    // picking plan
    int[] pp = new int[n];
    
    ArrayList<Integer> li = new ArrayList<Integer>(n);
    for (int k=0; k<n; k++) {
      li.add(k, k);
    }
    Collections.shuffle(li);
    
    //P.echo(li);
    //pp[0] = 1;
    int wc = 0;
    for (int k=0; k<n; k++) {
      int i = li.get(k); // get item
      int wi = ttp.weightOf(i);
      //Deb.echo("<<"+wi+" -- "+maxCapacity);
      if (wi+wc <= maxCapacity) {
        pp[i] = A[i];
        wc += wi;
      }
      else {
        break;
      }
    }
    
    return pp;
  }
  
  
  /**
   * random picking plan
   * @return
   */
  public int[] randomPickingPlan() {
    return randomPickingPlan(1);
  }
  
  
  /**
   * basic heuristic no 1
   * based on the constructive heuristic SH
   * 
   * sub-TSP: greedy algorithm
   * sub-KP: created for the fixed TSP tour so it maximizes TTP value
   * 
   * @return found solution
   */
  public TTPSolution SH2() {
    
    // get TTP data
    long[][] D = ttp.getDist();
    int[] A = ttp.getAvailability();
    double maxSpeed = ttp.getMaxSpeed();
    double minSpeed = ttp.getMinSpeed();
    long capacity = ttp.getCapacity();
    double C = (maxSpeed - minSpeed) / capacity;
    double R = ttp.getRent();
    int m = ttp.getNbCities(),
        n = ttp.getNbItems();
    TTPSolution s = new TTPSolution(m, n);
    int[] x = s.getTour(),
          z = s.getPickingPlan();
    
    /*
     * the tour
     * generated using greedy algorithm
     */
    x = greedyTour();
    
    
    /*
     * the picking plan
     * generated so that the TTP objective value is maximized
     */
    ttp.objective(s);
    
    // partial distance from city x_i
    long di;
    
    // partial time with item k collected from city x_i
    double tik;
    
    // item scores
    double[] score = new double[n];
    
    // total time with no items collected
    double t_ = s.ft;
    
    // total time with only item k collected
    double tik_;
    
    // fitness value
    double u[] = new double[n];
    
    for (int k=0; k<n; k++) {
      
      int i;
      for (i=0; i<m; i++) {
        if (A[k]==x[i]) break;
      }
      //P.echo2("["+k+"]"+(i+1)+"~");
      
      // time to start with
      tik = i==0 ? .0 : s.timeAcc[i-1];
      int iw = ttp.weightOf(k),
          ip = ttp.profitOf(k);
      
      // recalculate velocities from start
      di = 0;
      for (int r=i; r<m; r++) {
        
        int c1 = x[r]-1;
        int c2 = x[(r+1)%m]-1;
        
        di += D[c1][c2];
        tik += D[c1][c2] / (maxSpeed-iw*C);
      }
      
      score[k] = ip - R*tik;
      tik_ = t_ - di + tik;
      u[k] = R*t_ + ttp.profitOf(k) - R*tik_;
      //P.echo(k+" : "+u[k]);
    }
    
    Quicksort qs = new Quicksort(score);
    qs.sort();
    int[] si = qs.getIndices();
    int wc = 0;
    for (int k=0; k<n; k++) {
      int i = si[k];
      int wi = ttp.weightOf(i);
      // eliminate useless
      if (wi+wc <= capacity && u[i] > 0) {
        z[i] = A[i];
        wc += wi;
      }
    }
    
    ttp.objective(s);
    
    return s;
  }
  
  
  
}
