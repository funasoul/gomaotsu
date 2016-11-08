/**
 * 
 */
package jp.ac.keio.bio.fun.gomaotsu;

import java.util.HashMap;

/**
 * @author Akira Funahashi
 *
 */
public final class Constants {
  public final static int numSupportOtome = 3; // num. of support otome
  public final static int nodeWeight = 5;
  public final static int edgeWeight = 5;
  public final static String baseURL = "http://seesaawiki.jp/mahouotome/d/%C0%AD%C7%BD%C8%E6%B3%D3";
  public final static String otomeFileName = "OtomeList.csv";
  public final static String friendFileName = "FriendList.csv";
  public final static String stylesheet = "graph { padding: 45px; }"
      + "edge { z-index: 1; }"
      + "node { z-index: 0; fill-color: #EEE; stroke-mode: plain; stroke-color: #333; }"
      + "node { text-mode: normal; text-alignment: at-right; text-offset: 5px, 0px; text-padding: 5px, 4px; }"
      + "node { text-background-mode: rounded-box; text-background-color: #A7CC; text-color: #5e5e5e; text-style: bold; }"
      + "node.hi { text-background-color: #ff7e79; }"
      + "node.mizu { text-background-color: #76d6ff; }"
      + "node.kaze { text-background-color: #73fcd6; }"
      + "node.hikari { text-background-color: #ebebeb; }"
      + "node.yami { text-background-color: #d783ff; }"
      + "node.max { stroke-mode: none; } "
      + "node.notmax { stroke-mode: plain; } "
      + "node.shot { shape: circle; }"
      + "node.wide { shape: cross; }";
  
  public final static String stylesheet2 = "graph { padding: 45px; }"
      + "edge { }"
      + "node { size-mode: fit; shape: rounded-box; padding: 5px, 4px; }"
      + "node { fill-color: #EEE; stroke-mode: plain; stroke-color: #333; }"
/*      + "node { text-mode: normal; text-alignment: at-right; text-offset: 5px, 0px; text-padding: 5px, 4px; }"
      + "node { text-background-mode: rounded-box; text-background-color: #A7CC; text-color: #5e5e5e; text-style: bold; }"
      + "node.hi { text-background-color: #ff7e79; }"
      + "node.mizu { text-background-color: #76d6ff; }"
      + "node.kaze { text-background-color: #73fcd6; }"
      + "node.hikari { text-background-color: #ebebeb; }"
      + "node.yami { text-background-color: #d783ff; }"
      + "node.max { stroke-mode: none; } "
      + "node.notmax { stroke-mode: plain; } "
      + "node.shot { shape: circle; }"
      + "node.wide { shape: cross; }"; */ ;

  public final static HashMap<String, String> zokuseiMap = new HashMap<String, String>();
  static {
      zokuseiMap.put("火", "hi");
      zokuseiMap.put("水", "mizu");
      zokuseiMap.put("風", "kaze");
      zokuseiMap.put("光", "hikari");
      zokuseiMap.put("闇", "yami");
  }

  public final static HashMap<String, String> bunruiMap = new HashMap<String, String>();
  static {
      bunruiMap.put("集中", "shot");
      bunruiMap.put("拡散", "wide");
  }

  public final static HashMap<Boolean, String> maxMap = new HashMap<Boolean, String>();
  static {
      maxMap.put(true, "max");
      maxMap.put(false, "notmax");
  }
  
  public static final String RESET = "\u001B[0m";
  public static final String BLACK = "\u001B[30m";
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";
  public static final String WHITE = "\u001B[37m";
  /**
   * 
   */
  public Constants() {

  }

}
