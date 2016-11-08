/**
 * 
 */
package jp.ac.keio.bio.fun.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Combination class.
 * Base code of this class is taken from
 * http://stackoverflow.com/questions/127704/algorithm-to-return-all-combinations-of-k-elements-from-n
 * @author Akira Funahashi
 *
 */
public class CombinationRecursive extends Combination {

  /**
   * @param n
   * @param k
   */
  public CombinationRecursive(int n, int k) {
    super(n, k);
  }

  /**
   * Generates list of indices by this.n and this.k.
   * 
   * @return the list of combinations
   */
  public List<List<Integer>> getListOfCombinations() {
    listOfCombinations = new ArrayList<List<Integer>>();
    Integer[] array = new Integer[k]; // here we'll keep indices pointing to elements in input array
    combinations(n, k, 0, array);
    return listOfCombinations;
  }
  
  /**
   * recursive call version of generating list of combinations
   * @param arrn
   * @param len
   * @param startPosition
   * @param result
   */
  public void combinations(int arrn, int len, int startPosition, Integer[] result) {
    if (len == 0) {
      listOfCombinations.add(new ArrayList<Integer>(Arrays.asList(result)));
      return;
    }
    for (int i = startPosition; i <= arrn - len; i++) {
      result[result.length - len] = i;
      combinations(arrn, len - 1, i + 1, result);
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
    CombinationRecursive c = new CombinationRecursive(n, k);
    c.printCombinations();
  }

}