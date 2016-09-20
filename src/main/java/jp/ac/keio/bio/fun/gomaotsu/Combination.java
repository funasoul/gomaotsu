package jp.ac.keio.bio.fun.gomaotsu;

import java.util.ArrayList;

public class Combination {
  private int[] c;
  private int n, k, count;
  private boolean isOneOrigin;
  private ArrayList<ArrayList<Integer>> listOfCombinations;

  public Combination(int n, int k) {
    this.n = n;
    this.k = k;
    count = getCombinationNum(n, k);
    c = new int[count];
    isOneOrigin = false;
  }
  
  public Combination(int n, int k, boolean oneOrigin) {
    this(n, k);
    this.isOneOrigin = oneOrigin;
  }

  public int getCombinationNum(int n, int k) {
    int c = 1;
    for(int i = 1; i <= k; i++) {
      c = c * (n - i + 1) / i;
    }
    return c;
  }
  
  public void combine(int m) {
    if (m > k) {
      //printComb();
      addOneComb();
    } else {
      for (int i = c[m - 1] + 1; i <= n - k + m; i++) {
        c[m] = i;
        combine(m + 1);
      }
    }
  }

  public ArrayList<ArrayList<Integer>> getListOfCombinations() {
    listOfCombinations = new ArrayList<ArrayList<Integer>>();
    combine(1);
    return listOfCombinations;
  }

  public void addOneComb() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    for (int i = 1; i <= k; i++) {
      if (isOneOrigin) al.add(c[i]); // one-origin
      else al.add(c[i]-1);           // zero-origin
    }
    listOfCombinations.add(al);
  }

  public void printComb() {
    for (int i = 1; i <= k; i++) {
      System.out.print(c[i] + " "); // print index
    }
    System.out.println();
  }

  /**
   * @return the c
   */
  public int[] getC() {
    return c;
  }

  /**
   * @return the n
   */
  public int getN() {
    return n;
  }

  /**
   * @return the k
   */
  public int getK() {
    return k;
  }

  /**
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * @return the isOneOrigin
   */
  public boolean isOneOrigin() {
    return isOneOrigin;
  }

  public static void main(String[] args) {
    int n, k;
    n = 21;
    k = 3;
    Combination c = new Combination(n, k); // zero-origin
    //Combination c = new Combination(n, k, true); // one-origin
    ArrayList<ArrayList<Integer>> comb = c.getListOfCombinations();
    for (ArrayList<Integer> al : comb) {
      System.out.println(al);
    }
  }
}