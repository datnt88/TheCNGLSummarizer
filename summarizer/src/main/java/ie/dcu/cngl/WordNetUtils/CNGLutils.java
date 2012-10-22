package ie.dcu.cngl.WordNetUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;

public class CNGLutils {

    public static String normalizeText(String txt) {
        return Normalizer.normalize(txt, Normalizer.Form.NFKC);
    }


    public static String vectorString(Vector<String> vec) {
        String res = "";
        for (int i = 0; i < vec.size(); i++) {
            res += (String) vec.elementAt(i);
            if (i != (vec.size() - 1)) {
                res += " ";
            }
        }
        return res;
    }

    public static String utf_string(String term) {
        StringBuffer buf = new StringBuffer();
        try {
            byte[] b = term.getBytes("UTF-8");
            System.out.println(" '" + term + "' ");
//             for (int i=0; i< term.length(); i++) {
//                 System.out.print ("i:" + i + " "+ term.charAt(i) + " // "
//                                   // + Character.getType(i) + " "
//                                   + Character.getType(term.charAt(i)) + " "
//                                   // + (term.charAt(i)==' ')
// 		    );
//                 // System.out.println(b[i]);
//             }
            char[] chars = term.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                String hex = Integer.toHexString(chars[i] & 0xffff);
                for (int j = 0; j < (4 - hex.length()); j++) {
                    buf.append("0");
                }
                buf.append(hex);
                if (i <= chars.length) {
                    buf.append(" ");
                }
            }
        // System.out.print("buf:" + buf.toString());
        // CNGLutils.pressKey();

        } catch (Exception e) {
            System.out.println("ERROR: utf_string" + e);
        }
        return buf.toString();
    }


    public static Vector string2vec(String in) {
        String splitc = ";.!?\"- ";
        String[] svec = StringUtils.split(in, splitc);
        Vector<String> vec = new Vector<String>();
        for (int i = 0; i < Arrays.asList(svec).size(); i++) {
            String svelt = svec[i];
            vec.add((String)svelt.toLowerCase());
        }
        return vec;
    }

    public float safe_div(float a, float b) {
        if (b == 0) {
            return 0;
        }
        return a / b;
    }

    public static String pressKey() {
        String s = "";
        try {
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            s = in.readLine();
        } catch (Exception e) {
            System.out.println("ERROR: pressKey: " + e);
        }
        return s;
    }
}

