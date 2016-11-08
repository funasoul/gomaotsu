package jp.ac.keio.bio.fun.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Combination class.
 * Base code of this class is taken from
 * http://stackoverflow.com/questions/29910312/algorithm-to-get-all-the-combinations-of-size-n-from-an-array-java
 * @author Akira Funahashi
 *
 */
public class Combination {
  protected int n, k, count;
  protected List<List<Integer>> listOfCombinations;

  /**
   * Constructor
   * @param n
   * @param k
   */
  public Combination(int n, int k) {
    this.n = n;
    this.k = k;
    count = getCombinationNum(n, k);
  }

  /**
   * Generates list of indices by this.n and this.k.
   * @return the list of combinations
   */
  public List<List<Integer>> getListOfCombinations() {
    listOfCombinations = new ArrayList<List<Integer>>();
    List<Integer> al = new ArrayList<Integer>(); // here we'll keep indices pointing to elements in input array
    if (k <= n) {
      // first index sequence: 0, 1, 2, ...
      for (int i = 0; i < k; i++) {
        al.add(i, i);
      }
      listOfCombinations.add(new ArrayList<Integer>(al));
      for (;;) {
        int i;
        // find position of item that can be incremented
        for (i = k - 1; i >= 0 && al.get(i) == n - k + i; i--) ;
        if (i < 0) {
          break;
        } else {
          al.set(i, al.get(i)+1); // increment this item
          for (++i; i < k; i++) { // fill up remaining items
            al.set(i, al.get(i-1) + 1);
          }
          listOfCombinations.add(new ArrayList<Integer>(al));
        }
      }
    }
    return listOfCombinations;
  }

  /**
   * Returns the number of combinations by given n and k.
   * @param n
   * @param k
   * @return the num of combinations
   */
  public int getCombinationNum(int n, int k) {
    int c = 1;
    for (int i = 1; i <= k; i++) {
      c = c * (n - i + 1) / i;
    }
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
   * print all combinations
   */
  public void printCombinations() {
    if (listOfCombinations == null) {
      getListOfCombinations();
    }
    for (List<Integer> l : listOfCombinations) {
      System.out.println(l);
    }
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    int n, k;
    n = 5;
    k = 3;
    Combination c = new Combination(n, k);
    System.out.println(c.getCombinationNum(n, k));
    long s1 = System.nanoTime();
    List<List<Integer>> comb = c.getListOfCombinations();
    long s2 = System.nanoTime();
    c.printCombinations();
    System.out.println("Size: " + comb.size() + ", Time: "+ (s2 - s1)/1000000 + " msec");
  }
}