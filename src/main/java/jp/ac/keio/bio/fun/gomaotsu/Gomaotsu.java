package jp.ac.keio.bio.fun.gomaotsu;

import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkGraphML;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 
 */

/**
 * @author Akira Funahashi
 *
 */
public class Gomaotsu implements ViewerListener {
  private TreeSet<Otome> otomeSet;
  private Scraper sc;
  private Graph graph;
  private Viewer viewer;
  protected boolean loop;

  @Option(name="-h", aliases={"--help"}, usage="display usage")
  private boolean isHelp = false;
  @Option(name="-e", aliases={"--edit"}, usage="edit graph (click on nodes to remove)")
  private boolean isEdit = false;
  @Option(name="-u", aliases={"--update"}, usage="download and update friendlist from web")
  private boolean updateFromWeb = false;
  @Option(name="-g", aliases={"--guild"}, usage="generate graph for guild battle")
  private boolean forGuild = false;
  @Option(name="-a", aliases={"--add"}, usage="always add 5 otome to graph") // 5乙女は必ずグラフに描画するか
  private boolean add5OtometoGraph = false;
  @Option(name="-A", aliases={"--addAllParent"}, usage="always add parent(dst) otome to graph") // 矢印の先となる乙女は必ずグラフに描画するか
  private boolean addAllParenttoGraph = false;

  //receives other command line parameters than options
  @Argument
  private List<String> arguments = new ArrayList<String>();

  public Gomaotsu() {
    loop = true;
    sc = new Scraper();
  }

  public void init() {
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
   * 指定した名前 (String) を持つ乙女を返す
   * 
   * @param name
   * @return
   */
  public Otome getOtomeByName(String name) {
    for (Otome o : otomeSet) {
      if (o.getName().equals(name)) {
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
          if (sa[0].startsWith("#")) continue; // skip header
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
    } else {
      // If OtomeList.csv does not exist, then save current set as OtomeList.csv.
      exportOtomeList();
    }
  }

  /**
   * 仲良し情報をWeb/fileから取得し、otomeSetに追加する。Wikiに反映されていない新しい乙女の場合、
   * variantsを探し、variantsのfriendを追加する。
   * 
   * @param fromWeb
   *          trueならwebから情報を取得、falseならファイル(FriendList.csv)から取得する。
   */
  public void addFriendInfo(boolean fromWeb) {
    TreeSet<Otome> errSet;   // Wikiに反映されていない新しい乙女のset
    errSet = sc.addFriendInfo(otomeSet, fromWeb);
    // Wikiに反映されていない乙女の friendSet を既にある乙女からコピー
    for (Iterator<Otome> it = errSet.iterator(); it.hasNext();) {
      Otome o = it.next();
      for (Otome target: otomeSet) {
        if (o.isVariant(target)) {    // o は target の variantか。("【人魚】カトレア" は "カトレア" のvariant)
          o.setFriendSet(target.getFriendSet());  // variantなら、originalと同じ friendSet を持つはず
          it.remove();  // errSet からこの乙女を削除
          break;
        }
      }
    }
    System.out.println(" Done.");
    // 未登録の friendList を持つ乙女を表示
    if (errSet.size() > 0) {
      System.out.println("Friend not found for following otomes. Please check" + 
          System.getProperty("line.separator") + sc.getBaseURL() + " and fix its entry.");
      for (Otome o : errSet) {
        System.out.println(o.getId() + ":" + o.getName());
      }
    }
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

  public String generateHeader() {
    return "#No.,星,属性,使い魔,コスト,魔力,HP,分類,ショット種類,スキル種類,スキル名,スキル効果,所持,親密度max";
  }

  public void exportOtomeList() {
    ArrayList<String> otomeStringList = new ArrayList<String>();
    otomeStringList.add(generateHeader());
    for (Otome o : otomeSet) {
      otomeStringList.add(o.toString());
    }
    FileSystem fs = FileSystems.getDefault();
    Path otomeFile = fs.getPath(Constants.otomeFileName);
    createBackup(otomeFile);
    try {
      Files.write(otomeFile, otomeStringList, StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void exportFriendList() {
    ArrayList<String> friendStringList = new ArrayList<String>();
    for (Otome o : otomeSet) {
      String s = o.getSid();
      for (Otome f : o.getFriendSet()) {
        s += "," + f.getName();
      }
      friendStringList.add(s);
    }
    FileSystem fs = FileSystems.getDefault();
    Path friendFile = fs.getPath(Constants.friendFileName);
    createBackup(friendFile);
    try {
      Files.write(friendFile, friendStringList, StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void exportCSVFiles() {
    exportOtomeList();
    exportFriendList();
  }

  /**
   * @return the add5OtometoGraph
   */
  public boolean isAdd5OtometoGraph() {
    return add5OtometoGraph;
  }

  /**
   * @param add5OtometoGraph the add5OtometoGraph to set
   */
  public void setAdd5OtometoGraph(boolean add5OtometoGraph) {
    this.add5OtometoGraph = add5OtometoGraph;
  }

  /**
   * @return the addAllParenttoGraph
   */
  public boolean isAddAllParenttoGraph() {
    return addAllParenttoGraph;
  }

  /**
   * @param addAllParenttoGraph the addAllParenttoGraph to set
   */
  public void setAddAllParenttoGraph(boolean addAllParenttoGraph) {
    this.addAllParenttoGraph = addAllParenttoGraph;
  }

  /**
   * ギルドバトル用の編成か
   * @return the forGuild
   */
  public boolean isForGuild() {
    return forGuild;
  }

  /**
   * ギルドバトル用の編成か
   * @param forGuild the forGuild to set
   */
  public void setForGuild(boolean forGuild) {
    this.forGuild = forGuild;
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

  /**
   * @return the viewer
   */
  public Viewer getViewer() {
    return viewer;
  }

  /**
   * @param viewer the viewer to set
   */
  public void setViewer(Viewer viewer) {
    this.viewer = viewer;
  }

  /**
   * @return the loop
   */
  public boolean isLoop() {
    return loop;
  }

  /**
   * @param loop the loop to set
   */
  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  public void addOtomeToGraph(Otome o) {
    Node n = graph.addNode(o.getName());
    n.setAttribute("layout.weight", Constants.nodeWeight);
    n.addAttribute("ui.label", o.getHoshi() + ":" + o.getName());
    n.addAttribute("ui.class", o.getZokuseiAscii(), o.getBunruiAscii(), o.getMaxAscii());
  }

  /**
   * ネットワークに追加すべきエッジか
   * @param src サポート
   * @param dst メイン
   * @return
   */
  public boolean isActiveEdge(Otome src, Otome dst) {
    if (src.isOwn() && dst.isOwn() && !src.isLoveMax()) {   // サポートがLoveMaxでないことが前提
      if (forGuild) { // ギルドバトル用
        if (src.isZentaiSkill() && (dst.isRecommendedForGuild() || (isAdd5OtometoGraph() && dst.is5Otome()))) { // ギルドバトル用
          return true;
        }
      } else { // 通常用
        if (!dst.isLoveMax() || (isAdd5OtometoGraph() && dst.is5Otome()) || isAddAllParenttoGraph()) {
          return true;
        }
      }
    }
    return false;
  }

  public void addEdgeToGraph(Otome src, Otome dst) {
    if (isActiveEdge(src, dst)) {
      addOtomeToGraph(src);
      addOtomeToGraph(dst);
      Edge e = graph.addEdge(src.getName() + ":" + dst.getName(), src.getName(), dst.getName(), true);
      e.setAttribute("layout.weight", Constants.edgeWeight);
    }
  }

  public void addEdgeToList(Otome src, Otome dst, ArrayList<String> al) {
    if (isActiveEdge(src, dst)) {
      String s = src.getName() + "," + dst.getName();
      al.add(s);
    }
  }

  public void drawGraph() {
    viewer = getGraph().display();
    viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    for (Otome o : otomeSet) {
      for (Otome f : o.getFriendSet()) {
        addEdgeToGraph(o, f);
      }
    }
    removeRedundantNodes();
    // the graph becomes a sink for the viewer.
    // We also install us as a viewer listener to intercept the graphic events.
    ViewerPipe fromViewer = viewer.newViewerPipe();
    fromViewer.addViewerListener(this);
    fromViewer.addSink(graph);
    // Then we need a loop to wait for events.
    // In this loop we will need to call the pump() method to copy back events that have
    // already occured in the viewer thread inside our thread.
    while (loop) {
      fromViewer.pump();
    }
  }
  
  /**
   * Comparator for Node.
   * Node is sorted by its ID's length.
   * @author Akira Funahashi <funa@bio.keio.ac.jp>
   *
   */
  class MyComp implements Comparator<Node> {
    public int compare(Node o1, Node o2) {
      return Integer.compare(o1.getId().length(), o2.getId().length());
    }
  }

  /**
   * Traverse graph and remove redundant nodes
   * Definition of redundant Node:
   *  1.  It is a variant of some otome, and
   *  2.  It is not yet love max, and
   *  3a. It is 拡散 shot and we already have 拡散 variant on the network or,
   *  3b. It is 集中 shot and we already have 集中 variant on the network.
   */
  public void removeRedundantNodes() {
    Iterator<Node> it = graph.getNodeIterator();
    while (it.hasNext()) {
      Node n = it.next();
      Iterator<Edge> ie = n.getLeavingEdgeIterator();
      ArrayList<Node> ln = new ArrayList<Node>();
      while (ie.hasNext()) {
        Edge e = ie.next();
        Node tn = e.getTargetNode(); // tn is a parent of Node n.
        ln.add(tn);
      }
      Collections.sort(ln, new MyComp());  // sort by the length of otome's name
      for (int i = 0; i < ln.size(); i++) {
        Node tni = ln.get(i);
        Otome oi = getOtomeByName(tni.getId());
        boolean hasShuuchuu = oi.isShuuchuu();
        boolean hasKakusan = oi.isKakusan();
        for (int j = ln.size()-1; j > i; j--) {
          Node tnj = ln.get(j);
          Otome oj = getOtomeByName(tnj.getId());
          if (tnj.getId().endsWith(tni.getId()) && oj.isLoveMax()) {
            if (oj.isKakusan()) {
              if (hasKakusan) {
                graph.removeNode(tnj);
              } else {
                hasKakusan = true;
              }
            }
            if (oj.isShuuchuu()) {
              if (hasShuuchuu) {
                graph.removeNode(tnj);
              } else {
                hasShuuchuu = true;
              }
            }
          }
        }
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

  public void displayUsage(CmdLineParser parser) {
    System.err.println("java -jar target/Gomaotsu-${version}-SNAPSHOT-jar-with-dependencies.jar [options...]");
    parser.printUsage(System.err);
  }
  
  @Override
  public void buttonPushed(String id) {
    
  }

  @Override
  public void buttonReleased(String id) {
    if (isEdit) {
      graph.removeNode(id);
    }
  }

  @Override
  public void viewClosed(String id) {
    loop = false;
  }

  public static void main(String[] args) {
    new Gomaotsu().doMain(args);
  }

  public void doMain(String[] args) {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      // parse the arguments.
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      displayUsage(parser);
      return;
    }
    if (isHelp) {
      displayUsage(parser);
      return;
    }

    // here we go.
    init();
    addOtomeInfoFromFile();
    addFriendInfo(updateFromWeb);
    drawGraph();
    writeGraphML();
    // exportEdgeCSV();
    if (updateFromWeb) {
      exportCSVFiles();
    }
  }

}
