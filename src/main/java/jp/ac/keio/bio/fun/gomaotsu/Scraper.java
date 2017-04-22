package jp.ac.keio.bio.fun.gomaotsu;
/**
 * 
 */
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Akira Funahashi
 *
 */
public class Scraper {
  private String baseURL;
  private TreeMap<String, ArrayList<String>> allFriendListFromWeb;

  /**
   * 
   */
  public Scraper() {
    this.baseURL = Constants.baseURL;
  }

  public TreeSet<Otome> createOtomeSet() {
    TreeSet<Otome> otomeSet = new TreeSet<Otome>();
    Elements listTable = getOtomeTable();
    Elements rows = listTable.select("tr");
    for (int i = 1; i < rows.size(); i++) {
      Element row = rows.get(i);
      Elements cols = row.select("td");
      // No. ★ 属性 使い魔 コスト 魔力 HP 分類 ショット種類 スキル種類 スキル名 スキル効果
      String sid = cols.get(0).text();
      if (sid.equals("")) {
        continue;
      }
      try {
      int id = Integer.parseInt(sid);
      int hoshi = Integer.parseInt(cols.get(1).text());
      String zokusei = cols.get(2).text();
      String name = cols.get(3).text().replaceAll("\\?$", "");   // chomp last "?" in string.
      int cost = Integer.parseInt(cols.get(4).text());
      int maryoku = Integer.parseInt(cols.get(5).text());
      int hp = Integer.parseInt(cols.get(6).text());
      String bunrui = cols.get(7).text();
      String shot = cols.get(8).text();
      String skill = cols.get(9).text();
      String skillName = cols.get(10).text();
      String skillKouka = cols.get(11).text();
      // URL
      Elements links = cols.get(3).select("a[href]");
      String url = links.get(0).attr("abs:href");
      Otome otm = new Otome(id, hoshi, zokusei, name, cost, maryoku, hp, bunrui, shot, skill, skillName, skillKouka, false, false, url);
      System.out.println(otm);
      otomeSet.add(otm);
      } catch (NumberFormatException ne) {
        System.err.println("failed to parse Otome " + sid);
      }
    }
    System.out.println("Read " + otomeSet.size() + " otomes.");
    return otomeSet;
  }
  
  public TreeSet<Otome> addFriendInfo(TreeSet<Otome> otomeSet, boolean fromWeb) {
    TreeSet<Otome> errSet = new TreeSet<Otome>();
    int count = 0;
    int tick = (int)(otomeSet.size()*0.05);
    if (fromWeb) {
      System.out.println("Downloading Aishou list from web...");
      createAllFriendListFromWeb();
    }
    System.out.print("Loading Otome... ");
    for (Otome o : otomeSet) {
      count++;
      if (count > tick && count % tick == 0) {
        System.out.print("=");
      }
      ArrayList<String> al;
      if (fromWeb) {
        al = this.getFriendListFromAllFriendList(o);
        if (al.size() == 0) errSet.add(o);
      } else {
        al = this.getFriendListFromFile(o);
        if (al.size() == 0) errSet.add(o);
      }
      // Add friend(and variants) to otome.
      // this is required only on getFriendListFromWeb, but just to make sure, it is also executed for FromFile.
      for (String friend : al) {
        for (Otome target : otomeSet) {
          if (target.isVariant(friend)) {
            o.getFriendSet().add(target);
          }
        }
      }
    }
    return errSet;
  }

  public Elements getOtomeTable() {
    Elements listTable = null;
    try {
      Document document = Jsoup.connect(this.baseURL).get();
      listTable = document.select("table#content_block_3");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return listTable;
  }

  public void createAllFriendListFromWeb() {
    this.allFriendListFromWeb = new TreeMap<String, ArrayList<String>>();
    try {
      Document document = Jsoup.connect(Constants.aishouURL).timeout(0).get();
      Element table = document.getElementsByTag("table").get(0); // first <table>
      Elements rows = table.select("tr");
      for (int i = 0; i < rows.size(); i++) {
        Element row = rows.get(i);
        Elements tmpRow = row.select("td");
        if (tmpRow.size() > 0) {  // <td></td> を含む行の場合
          Elements supportCols = row.select("td").get(0).select("p");
          Elements shotCols = row.select("td").get(1).select("p");
          String otomeName = supportCols.text();
          ArrayList<String> al = new ArrayList<String>();
          for (int j = 0; j < shotCols.size(); j++) {
            String friendName = StringUtil.fixFriendName(shotCols.get(j).text());
            al.add(friendName);
          }
          allFriendListFromWeb.put(otomeName,  al);
        }
      }
    } catch (IOException e) {
      System.err.println("Connection error on: " + Constants.aishouURL);
      e.printStackTrace();
    }
  }

  /**
   * allFriendListFromWeb から、指定された乙女と相性が良い乙女のリストを見つけて返す。
   * allFriendListFromWeb には variant は含まれていないため、引数で与えられた乙女が variant かを調べ、該当するリストを返す。
   * @param otm
   * @return
   */
  public ArrayList<String> getFriendListFromAllFriendList(Otome otm) {
    for (Map.Entry<String, ArrayList<String>> e : allFriendListFromWeb.entrySet()) {
      if (otm.isVariant(e.getKey())) {
        return e.getValue();
      }
    }
    return new ArrayList<String>();
  }

  /**
   * 引数で与えられた乙女の web page を取得し、相性の良い乙女のリストを返す。
   * 乙女毎にこのメソッドを呼ぶことになるため、実行時間がかかるため getFriendListFromAllFriendList() を使用するように変更。
   * @deprecated
   * @param otm
   * @return
   */
  public ArrayList<String> getFriendListFromWeb(Otome otm) {
    ArrayList<String> al = new ArrayList<String>();
    String otomeUrl = otm.getUrl();
    //System.out.println(otomeUrl);
    try {
      Document document = Jsoup.connect(otomeUrl).timeout(0).get();
      Elements tables = document.getElementsByTag("table");
      for (Element table : tables) {
        Elements rows = table.select("tr");
        for (int i = 0; i < rows.size(); i++) {
          Element row = rows.get(i);
          if (row.text().contains("相性の良い")) {
            Elements cols = row.select("td").select("p");
            for (int j = 0; j < cols.size(); j++) {
              al.add(cols.get(j).text());
            }
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Connection error on: " + otm.getName());
      e.printStackTrace();
    }
    return al;
  }

  public ArrayList<String> getFriendListFromFile(Otome otm) {
    ArrayList<String> al = new ArrayList<String>();
    FileSystem fs = FileSystems.getDefault();
    Path path = fs.getPath(Constants.friendFileName);
    try {
      List<String> list = Files.readAllLines(path);
      for (String line : list) {
        String[] sa = line.split(",");
        if (sa[0].equals(otm.getSid())) {
          for (int i = 1; i < sa.length; i++) {
            al.add(sa[i]);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return al;
  }

  /**
   * @return the baseURL
   */
  public String getBaseURL() {
    return baseURL;
  }

  /**
   * @param baseURL
   *          the baseURL to set
   */
  public void setBaseURL(String baseURL) {
    this.baseURL = baseURL;
  }

}
