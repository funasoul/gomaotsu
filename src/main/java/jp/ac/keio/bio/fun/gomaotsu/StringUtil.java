/**
 * 
 */
package jp.ac.keio.bio.fun.gomaotsu;

/**
 *
 */
public class StringUtil {

  /**
   * 
   */
  public StringUtil() {
  }

  /*
   * 文字列のバイト長を返すメソッド http://nanaowls.blogspot.com.es/2012/01/java.html
   */
  public static int getByteLength(String value) {
    int length = 0;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c >= 0x20 && c <= 0x7E) {
        // JISローマ字(ASCII)
        length++;
      } else if (c >= 0xFF61 && c <= 0xFF9F) {
        // JISカナ(半角カナ)
        length++;
      } else {
        // その他(全角)
        length += 2;
      }
    }
    return length;
  }

  /*
   * 文字列の桁あわせを行うメソッド http://nanaowls.blogspot.com.es/2012/01/java.html
   * @param string 文字列
   * @param int 長さ
   * @return 指定した長さの文字列
   */
  public static String pad(String str, int len) {
    int bytelen = getByteLength(str);
    for (int i = 0; i < (len - bytelen); i++) {
      str = str + " ";
    }
    return str;
  }
}
