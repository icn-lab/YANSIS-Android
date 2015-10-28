/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

class Morpheme {
    public String surface;
    public String base;
    public String pos;
    public String cform;
    
    public Morpheme(String surface, String base, String pos, String cform) {
        this.surface = surface;
        this.base = base;
        this.pos = pos;
        this.cform = cform;
    }
    public boolean equals(WordProperty w) {
        if (!surface.equals("*") && !surface.equals(w.toString()))
            return false;
        if (!base.equals("*") && !base.equals(w.getBasicString()))
            return false;
        if (!pos.equals("*") && !pos.equals(w.getPOS()))
            return false;
        if (cform != null && !cform.equals("*") && !cform.equals(w.getCform()))
            return false;
        return true;
    }
    public String toString() {
        return surface+"/"+base+"/"+pos;
    }
}

class RecommendationPattern {
    Morpheme morph[];
    String advice;
    public RecommendationPattern(Morpheme morph[], String advice) {
        this.morph = morph;
        this.advice = advice;
    }
    public int length() {
        return morph.length;
    }
    public boolean matchAt(WordProperty w[], int pos) {
        if (pos+length()-1 > w.length-1) 
            return false;
        for (int i = 0; i < length(); i++) {
            if (!morph[i].equals(w[i+pos]))
                return false;
        }
        return true;
    }
    public String firstWord() {
        return morph[0].surface;
    }
    public String getAdvice() {
        return advice;
    }
}

class RecommendationHash {
    Hashtable<String,Vector<RecommendationPattern>> hash;
    public RecommendationHash() {
        hash = new Hashtable<String,Vector<RecommendationPattern>>();
    }
    public void add(RecommendationPattern pat) {
        Vector<RecommendationPattern> v = hash.get(pat.firstWord());
        if (v == null) {
            v = new Vector<RecommendationPattern>();
            hash.put(pat.firstWord(), v);
        }
        v.add(pat);
    }
    public Vector<String> getAdvices(WordProperty w[], int pos) {
        Vector<String> adv = new Vector<String>();
        Vector<RecommendationPattern> v = hash.get(w[pos].toString());
        if (v != null) {
            for (RecommendationPattern pat : v) {
                if (pat.matchAt(w, pos)) {
                    adv.add(pat.getAdvice());
                }
            }
        }
        v = hash.get("*");
        if (v != null) {
            for (RecommendationPattern pat : v) {
                if (pat.matchAt(w, pos)) {
                    adv.add(pat.getAdvice());
                }
            }
        }
        return adv;
    }
}

/**
 * 文法的なアドバイスを提供するためのクラス。
 * @author aito
 */
public class Recommendation {
    RecommendationHash h;
    public Recommendation(String filename) throws IOException {
        h = new RecommendationHash();
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "Shift_JIS"));
        int nline = 0;
        while (true) {
            nline++;
            String line = rd.readLine();
            if (line == null) break;
            // パターンデータベースの構造：
            //  先頭が # の行はコメント。
            //  形態素1,形態素2,...,形態素n,アドバイス
            // 形態素の構造：
            //  表層/原形/品詞
            line = line.trim();
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }
            String[] morphStr = line.split(",");
            int morphlen = morphStr.length-1;
            while (morphlen > 0 && morphStr[morphlen] == "")
                morphlen--;
            if (morphlen == 0)
                continue;
            Morpheme morphs[] = new Morpheme[morphlen];
            for (int i = 0; i < morphlen; i++) {
                String m[] = morphStr[i].split("/");
                if (m.length < 3) {
                    throw new IllegalArgumentException("Ill-formed morpheme in Recommendation at line "+
                            Integer.toString(nline)+":"+morphStr[i]);
                }
                String cform = null;
                if (m.length > 3)
                    cform = m[3];
                morphs[i] = new Morpheme(m[0],m[1],m[2],cform);
            }
            h.add(new RecommendationPattern(morphs,morphStr[morphlen]));
        }
        rd.close();
    }
    public String[] getRecommendations(WordProperty[] w) {
        Vector<String> res = new Vector<String>();
        // 長さに関する警告
        int yomiLength = 0;
        for (int i = 0; i < w.length; i++) {
            //yomiLength += w[i].getPronunciation().length();
            yomiLength += w[i].pronunciationLength();
        }
        if (yomiLength >= 40)
            res.add("文が長すぎます("+Integer.toString(yomiLength)+
                    "拍)。文を分割してください。");
        else if (yomiLength >= 30) 
            res.add("文がやや長いので("+Integer.toString(yomiLength)+
                    "拍)、文の分割を検討してください。");

        HashMap<String,String> map = new HashMap<String,String>();

        // 形態素パターンに関する警告
        for (int i = 0; i < w.length; i++) {
            Vector<String> adv = h.getAdvices(w, i);
            for (int j = 0; j < adv.size(); j++) {
                String advStr = adv.get(j);
                if(map.containsKey(advStr)){
                    continue;
                }
                else {
                    res.add(advStr);
                    map.put(advStr, "1");
                }
            }
        }

        String[] ret_val = new String[res.size()];
        for (int i = 0; i < res.size(); i++) {
            ret_val[i] = res.get(i);
        }
        return ret_val;
    }

}
