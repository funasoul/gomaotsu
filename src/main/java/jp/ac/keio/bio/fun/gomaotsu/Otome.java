package jp.ac.keio.bio.fun.gomaotsu;
import java.io.Serializable;
import java.util.TreeSet;

/**
 * 
 */

/**
 * @author Akira Funahashi
 *
 */
public class Otome implements Serializable, Comparable<Otome> {
  private int id;
  private int hoshi;
  private String zokusei;
  private String name;
  private int cost;
  private int maryoku;
  private int hp;
  private String bunrui;
  private String shot;
  private String skill;
  private String skillName;
  private String skillKouka;
  private boolean own;
  private boolean isLoveMax;
  private String url;
  private TreeSet<Otome> friendSet;

  /**
   * 
   */
  public Otome() {

  }

  public Otome(int id, int hoshi, String zokusei, String name, int cost, int maryoku, int hp, String bunrui, String shot, String skill,
      String skillName, String skillKouka, boolean own, boolean isLoveMax, String url) {
    this.id = id;                 // No.
    this.hoshi = hoshi;           // ★
    this.zokusei = zokusei;       // 属性
    this.name = name;             // 使い魔
    this.cost = cost;             // コスト
    this.maryoku = maryoku;       // 魔力
    this.hp = hp;                 // HP
    this.bunrui = bunrui;         // ショット分類
    this.shot = shot;             // ショット種類
    this.skill = skill;           // スキル種類
    this.skillName = skillName;   // スキル名
    this.skillKouka = skillKouka; // スキル効果
    this.own = own;               // 所有しているか
    this.isLoveMax = isLoveMax;   // 愛を注ぎきっているか
    this.url = url;
    friendSet = new TreeSet<Otome>();
  }
  
  /**
   * just for test in Scraper.main().
   * @param url
   */
  public Otome(String url) {
    this.url = url;
    friendSet = new TreeSet<Otome>();
  }

  /**
   * 引数で与えられた名前を持つか ("【人魚】カトレア" は引数"カトレア"に対してtrueを返す)
   * @param vname
   * @return
   */
  public boolean isVariant(String vname) {
    if (this.name.endsWith(vname)) {
      return true;
    }
    return false;
  }
  
  /**
   * 引数で与えられた乙女の名前を持つか ("【人魚】カトレア" は引数 Otome("カトレア") に対してtrueを返す)
   * @param votome
   * @return
   */
  public boolean isVariant(Otome votome) {
    if (this.name.endsWith(votome.getName())) {
      return true;
    }
    return false;
  }
   
  /**
   * toString用 boolean -> "1"/"0" 変換
   * @param b
   * @return
   */
  public String booleanToString(boolean b) {
    if (b) {
      return "1";
    }
    return "0";
  }
  
  public String toString() {
    // No. ★ 属性 使い魔 コスト 魔力 HP 分類 ショット種類 スキル種類 スキル名 スキル効果
    String s = getSid() + "," + getHoshi() + "," + getZokusei() + "," + getName() + "," +
        getCost() + "," + getMaryoku() + "," + getHp() + "," + getBunrui() + "," + getShot() + "," +
        getSkill() + "," + getSkillName() + "," + getSkillKouka() + "," +
        booleanToString(isOwn()) + "," + booleanToString(isLoveMax());
    return s;
  }
  
  public boolean is5Otome() {
    if (name.equals("ラナン") || name.equals("カトレア") || name.equals("スフレ") ||
        name.equals("プルメリア") || name.equals("ロザリー") ) {
      return true;
    }
    return false;
  }
  
  /**
   * 全体攻撃スキルか
   * @return
   */
  public boolean isZentaiSkill() {
    if (getSkill().startsWith("全体攻撃")) {
      return true;
    }
    return false;
  }
  
  /**
   * ギルドバトルに向いているか
   * @return
   */
  public boolean isRecommendedForGuild() {
    String shot = getShot();
    if (shot.equals("ホーミング改") || shot.equals("ロックビーム") || 
        shot.equals("ガトリング") || shot.equals("メガレーザー") || shot.equals("ツインショット")) {
      return true;
    }
    return false;
  }
  
  /**
   * @return the id (No.)
   */
  public int getId() {
    return id;
  }

  /**
   * @param id (No.)
   *          the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return id as String (1 -> "001").
   */
  public String getSid() {
    return String.format("%03d", this.id);
  }

  /**
   * @return the hoshi (★)
   */
  public int getHoshi() {
    return hoshi;
  }

  /**
   * @param hoshi (★)
   *          the hoshi to set
   */
  public void setHoshi(int hoshi) {
    this.hoshi = hoshi;
  }

  /**
   * @return the zokusei (属性)
   */
  public String getZokusei() {
    return zokusei;
  }
  
  /**
   * 属性を ASCII 文字で返す。sytlesheetの定義で必要。
   * @return
   */
  public String getZokuseiAscii() {
    return Constants.zokuseiMap.get(zokusei);
  }

  /**
   * @param zokusei (属性)
   *          the zokusei to set
   */
  public void setZokusei(String zokusei) {
    this.zokusei = zokusei;
  }

  /**
   * 乙女の名前を返す
   * @return the name (使い魔)
   */
  public String getName() {
    return name;
  }

  /**
   * @param name (使い魔)
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the cost (コスト)
   */
  public int getCost() {
    return cost;
  }

  /**
   * @param cost (コスト)
   *          the cost to set
   */
  public void setCost(int cost) {
    this.cost = cost;
  }

  /**
   * @return the maryoku (魔力)
   */
  public int getMaryoku() {
    return maryoku;
  }

  /**
   * @param maryoku (魔力)
   *          the maryoku to set
   */
  public void setMaryoku(int maryoku) {
    this.maryoku = maryoku;
  }

  /**
   * @return the hp (HP)
   */
  public int getHp() {
    return hp;
  }

  /**
   * @param hp (HP)
   *          the hp to set
   */
  public void setHp(int hp) {
    this.hp = hp;
  }

  /**
   * ショットの分類(集中 or 拡散)を返す
   * @return the bunrui (分類)
   */
  public String getBunrui() {
    return bunrui;
  }
  
  /**
   * 属性を ASCII 文字で返す。sytlesheetの定義で必要。
   * @return
   */
  public String getBunruiAscii() {
    return Constants.bunruiMap.get(bunrui);
  }

  /**
   * @param bunrui (分類)
   *          the bunrui to set
   */
  public void setBunrui(String bunrui) {
    this.bunrui = bunrui;
  }

  /**
   * ショット種類(ショット、Wショット、スプレッド、etc)を返す
   * @return the shot (ショット種類)
   */
  public String getShot() {
    return shot;
  }

  /**
   * @param shot (ショット種類)
   *          the shot to set
   */
  public void setShot(String shot) {
    this.shot = shot;
  }

  /**
   * スキル種類(全体攻撃、特殊弾、設置、回復、etc)を返す
   * @return the skill (スキル種類)
   */
  public String getSkill() {
    return skill;
  }

  /**
   * @param skill (スキル種類)
   *          the skill to set
   */
  public void setSkill(String skill) {
    this.skill = skill;
  }

  /**
   * @return the skillName (スキル名)
   */
  public String getSkillName() {
    return skillName;
  }

  /**
   * @param skillName (スキル名)
   *          the skillName to set
   */
  public void setSkillName(String skillName) {
    this.skillName = skillName;
  }

  /**
   * @return the skillKouka (スキル効果)
   */
  public String getSkillKouka() {
    return skillKouka;
  }

  /**
   * @param skillKouka (スキル効果)
   *          the skillKouka to set
   */
  public void setSkillKouka(String skillKouka) {
    this.skillKouka = skillKouka;
  }

  /**
   * @return the own (所有しているか)
   */
  public boolean isOwn() {
    return own;
  }
  
  /**
   * @param own (所有しているか) the own to set
   */
  public void setOwn(boolean own) {
    this.own = own;
  }

  /**
   * @return the isLoveMax (愛を注ぎきっているか)
   */
  public boolean isLoveMax() {
    return isLoveMax;
  }

  /**
   * 愛を注ぎきっているかを ASCII 文字で返す。sytlesheetの定義で必要。
   * @return
   */
  public String getMaxAscii() {
    return Constants.maxMap.get(isLoveMax);
  }

  /**
   * @param isLoveMax (愛を注ぎきっているか) the isLoveMax to set
   */
  public void setLoveMax(boolean isLoveMax) {
    this.isLoveMax = isLoveMax;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url
   *          the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the friendList
   */
  public TreeSet<Otome> getFriendSet() {
    return friendSet;
  }

  /**
   * @param friendSet the friendList to set
   */
  public void setFriendSet(TreeSet<Otome> friendSet) {
    this.friendSet = friendSet;
  }

  @Override
  public int compareTo(Otome o) {
    return this.id - o.id;
  }

}
