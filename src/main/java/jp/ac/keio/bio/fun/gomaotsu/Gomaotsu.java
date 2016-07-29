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
public class Gomaotsu {
  private TreeSet<Otome> otomeSet;
  private Scraper sc;
  private Graph graph;

  @Option(name="-h", aliases={"--help"}, usage="display usage")
  private boolean isHelp = false;
  @Option(name="-u", aliases={"--update"}, usage="download and update friendlist from web")
  private boolean updateFromWeb = false;
  @Option(name="-g", aliases={"--guild"}, usage="generate graph for guild battle")
  private boolean forGuild = false;
  @Option(name="-a", aliases={"--add"}, usage="always add 5 otome to graph") // 5乙女は必ずグラフに描画するか
  private boolean add5OtometoGraph = false;

  //receives other command line parameters than options
  @Argument
  private List<String> arguments = new ArrayList<String>();

  public Gomaotsu() {
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
        } else {
          return false;
        }
      } else { // 通常用
        if (!dst.isLoveMax() || (isAdd5OtometoGraph() && dst.is5Otome())) {
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

  public void displayUsage(CmdLineParser parser) {
    System.err.println("java -jar target/Gomaotsu-${version}-SNAPSHOT-jar-with-dependencies.jar [options...]");
    parser.printUsage(System.err);
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
