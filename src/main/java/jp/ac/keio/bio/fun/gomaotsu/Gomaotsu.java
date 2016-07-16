package jp.ac.keio.bio.fun.gomaotsu;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkGraphML;

/**
 * 
 */

/**
 * @author Akira Funahashi
 *
 */
public class Gomaotsu {
  private TreeSet<Otome> otomeSet;
  private Scraper sc;
  private Graph graph;

  public Gomaotsu() {
    sc = new Scraper();
    otomeSet = sc.createOtomeSet();
    initGraph();
  }

  public void initGraph() {
    this.graph = new SingleGraph("OtmGraph");
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    graph.addAttribute("ui.stylesheet", Constants.stylesheet);
    graph.addAttribute("ui.quality");
    graph.addAttribute("ui.antialias");
    graph.setStrict(false);
    graph.setAutoCreate(true);
  }

  /**
   * 指定した id (int) を持つ乙女を返す
   * 
   * @param id
   * @return
   */
  public Otome getOtomeById(int id) {
    for (Otome o : otomeSet) {
      if (o.getId() == id) {
        return o;
      }
    }
    return null;
  }

  /**
   * 指定した sid (String) を持つ乙女を返す
   * 
   * @param sid
   * @return
   */
  public Otome getOtomeBySid(String sid) {
    for (Otome o : otomeSet) {
      if (o.getSid().equals(sid)) {
        return o;
      }
    }
    return null;
  }

  /**
   * 引数に与えられた String が "1" か "true"(ignore case) ならば true を返す
   * 
   * @param s
   * @return
   */
  public boolean isOne(String s) {
    if (s.equalsIgnoreCase("true") || s.equals("1")) {
      return true;
    }
    return false;
  }

  /**
   * Webから取得した乙女リストの情報に加え、ファイルから所有・愛情情報を付加する
   */
  public void addOtomeInfoFromFile() {
    FileSystem fs = FileSystems.getDefault();
    Path otomeFile = fs.getPath(Constants.otomeFileName);
    if (Files.exists(otomeFile)) {
      try {
        List<String> list = Files.readAllLines(otomeFile);
        for (String line : list) {
          String[] sa = line.split(",");
          Integer id = Integer.parseInt(sa[0]);
          Otome o = getOtomeById(id);
          boolean own = isOne(sa[12]);
          boolean isLoveMax = isOne(sa[13]);
          if (o != null) {
            o.setOwn(own);
            o.setLoveMax(isLoveMax);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 仲良し情報をWeb/fileから取得し、otomeSetに追加する
   * 
   * @param fromWeb
   *          trueならwebから情報を取得、falseならファイル(FriendList.csv)から取得する。
   */
  public void addFriendInfo(boolean fromWeb) {
    sc.addFriendInfo(otomeSet, fromWeb);
  }

  /**
   * "file.csv" を "file-bak.csv" に mv
   * 
   * @param src
   */
  public void createBackup(Path src) {
    if (Files.exists(src)) {
      FileSystem fs = FileSystems.getDefault();
      Path dst = fs.getPath(src.toString().replace(".csv", "-bak.csv"));
      try {
        Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void exportEdgeCSV() {
    ArrayList<String> edgeList = new ArrayList<String>();
    for (Otome o : otomeSet) {
      for (Otome f : o.getFriendSet()) {
        addEdgeToList(o, f, edgeList);
      }
    }
    FileSystem fs = FileSystems.getDefault();
    Path csvFile = fs.getPath("EdgeList.csv");
    try {
      Files.write(csvFile, edgeList, StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void exportOtomeSet() {
    ArrayList<String> otomeStringList = new ArrayList<String>();
    ArrayList<String> friendStringList = new ArrayList<String>();
    for (Otome o : otomeSet) {
      otomeStringList.add(o.toString());
      String s = o.getSid();
      for (Otome f : o.getFriendSet()) {
        s += "," + f.getName();
      }
      friendStringList.add(s);
    }
    FileSystem fs = FileSystems.getDefault();
    Path otomeFile = fs.getPath(Constants.otomeFileName);
    Path friendFile = fs.getPath(Constants.friendFileName);
    createBackup(otomeFile);
    createBackup(friendFile);
    try {
      Files.write(otomeFile, otomeStringList, StandardOpenOption.CREATE);
      Files.write(friendFile, friendStringList, StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @return the otomeSet
   */
  public TreeSet<Otome> getOtomeSet() {
    return otomeSet;
  }

  /**
   * @param otomeSet
   *          the otomeSet to set
   */
  public void setOtomeSet(TreeSet<Otome> otomeSet) {
    this.otomeSet = otomeSet;
  }

  /**
   * @return the sc
   */
  public Scraper getSc() {
    return sc;
  }

  /**
   * @param sc
   *          the sc to set
   */
  public void setSc(Scraper sc) {
    this.sc = sc;
  }

  /**
   * @return the graph
   */
  public Graph getGraph() {
    return graph;
  }

  /**
   * @param graph
   *          the graph to set
   */
  public void setGraph(Graph graph) {
    this.graph = graph;
  }

  public void addOtomeToGraph(Otome o) {
    Node n = graph.addNode(o.getName());
    n.setAttribute("layout.weight", Constants.nodeWeight);
    n.addAttribute("ui.label", o.getHoshi() + ":" + o.getName());
    n.addAttribute("ui.class", o.getZokuseiAscii(), o.getBunruiAscii(), o.getMaxAscii());
  }

  public void addEdgeToGraph(Otome src, Otome dst) {
    if (src.isOwn() && dst.isOwn() && !src.isLoveMax() && !dst.isLoveMax()) {
    //if (src.isOwn() && dst.isOwn() && !src.isLoveMax()) {
      addOtomeToGraph(src);
      addOtomeToGraph(dst);
      Edge e = graph.addEdge(src.getName() + ":" + dst.getName(), src.getName(), dst.getName(), true);
      e.setAttribute("layout.weight", Constants.edgeWeight);
    }
  }

  public void addEdgeToList(Otome src, Otome dst, ArrayList<String> al) {
    if (src.isOwn() && dst.isOwn() && src.isLoveMax() == false && dst.isLoveMax() == false) {
      String s = src.getName() + "," + dst.getName();
      al.add(s);
    }
  }

  public void drawGraph() {
    getGraph().display();
    for (Otome o : otomeSet) {
      for (Otome f : o.getFriendSet()) {
        addEdgeToGraph(o, f);
      }
    }
  }
  
  public void writeGraphML() {
    FileSinkGraphML fs = new FileSinkGraphML();
    try {
      fs.writeAll(this.getGraph(), "Otome.graphml");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    boolean fromWeb = false; // download friendlist from Web
    if (args.length > 0 && args[0].equals("-u")) {
      fromWeb = true;
    }
    Gomaotsu g = new Gomaotsu();
    g.addOtomeInfoFromFile();
    g.addFriendInfo(fromWeb);
    g.drawGraph();
    g.writeGraphML();
    // g.exportEdgeCSV();
    // g.exportOtomeSet();
    if (fromWeb) {
      g.exportOtomeSet();
    }
  }

}
