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

  /**
   * 
   */
  public Scraper() {
    this.baseURL = "http://seesaawiki.jp/mahouotome/d/%C0%AD%C7%BD%C8%E6%B3%D3";
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
      String name = cols.get(3).text();
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
  
  public TreeSet<Otome> addFriendInfo(TreeSet<Otome> otomeList, boolean fromWeb) {
    ArrayList<Otome> errList = new ArrayList<Otome>();
    int count = 0;
    int tick = (int)(otomeList.size()*0.05);
    System.out.print("Loading Otome... ");
    for (Otome o : otomeList) {
      count++;
      if (count > tick && count % tick == 0) {
        System.out.print("=");
      }
      ArrayList<String> al;
      if (fromWeb) {
        al = this.getFriendListFromWeb(o);
        if (al.size() == 0) errList.add(o);
      } else {
        al = this.getFriendListFromFile(o);
      }
      // Add friend(and variants) to otome.
      // this is required only on getFriendListFromWeb, but just to make sure, it is also executed for FromFile.
      for (String friend : al) {
        for (Otome target : otomeList) {
          if (target.isVariant(friend)) {
            o.getFriendSet().add(target);
          }
        }
      }
    }
    System.out.println(" Done.");
    if (errList.size() > 0) {
      System.out.println("Friend not found for following otomes.Please check" + 
          System.getProperty("line.separator") + baseURL + " and fix its entry.");
      for (Otome o : errList) {
        System.out.println(o.getId() + ":" + o.getName());
      }
    }
    return otomeList;
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
