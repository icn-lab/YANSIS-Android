/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;
import util.jplevelanalyzer.WordWithGrade;

/**
 *
 * @author aito
 */
public class WordProperty {
    private WordWithGrade wgrade;
    public WordProperty(WordWithGrade w) {
        wgrade = w;
    }
    /** 
     * ３・4級単語、基礎文法項目、記号の場合にtrueを返す。
     * @return
     */
    public boolean is_easy() {
        return (wgrade.grade >= 3) || is_proper_noun() || is_digits();
    }
    /**
     * １・２級単語の場合にtrueを返す。
     * @return
     */
    public boolean is_difficult() {
        if (is_proper_noun() || is_digits())
            return false;
        return wgrade.grade == 1 || wgrade.grade == 2;
    }
    /**
     * 級外単語の場合にtrueを返す。
     * 
     * @return
     */
    public boolean is_verydifficult() {
        if (is_proper_noun() || is_digits())
            return false;
        return wgrade.grade == 0;
    }
    /**
     * 活用語のときtrueを返す．
     * @return
     */
    public boolean is_conjugate() {
        return wgrade.cform != null;
    }
    /**
     * 半角数字の場合にtrueを返す．
     * @return
     */
    public boolean is_digits() {
        String s = wgrade.word;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)))
                return false;
        }
        return true;
    }
    /**
     * 内容語の場合にtrueを返す．
     * @return
     */
    public boolean is_content_word() {
        String pos = getPOS();
        String posElem[] = pos.split("-");
        if (posElem[0].equals("名詞") || posElem[0].equals("動詞") || 
                posElem[0].equals("形容詞") || posElem[0].equals("副詞") ||
                posElem[0].equals("連体詞") || posElem[0].equals("接続詞")) {
            if (posElem.length > 1 && 
                    (posElem[1].equals("非自立") ||
                     posElem[1].equals("接尾")))
                return false;
            else
                return true;
        } 
        return false;
    }
    /**
     * 固有名詞かどうか
     * @return 固有名詞の場合にtrue
     */
    public boolean is_proper_noun() {
        String pos = getPOS();
        return pos.startsWith("名詞-固有名詞");
    }
    @Override
    public String toString() {
        return wgrade.word;
    }
    public String getBasicString() {
        return wgrade.basicString;
    }
    public String getPOS() {
        return wgrade.pos;
    }
    public int getGrade() {
        return wgrade.grade;
    }
    public String getReading() {
        return wgrade.reading;
    }
    public String getPronunciation() {
        return wgrade.pronunciation;
    }
    public String getCform() {
        return wgrade.cform;
    }
    /**
     * 読みのモーラ数を返す。
     * @return
     */
    public int pronunciationLength() {
        int n = 0;
        if (wgrade.pos.startsWith("記号")) {
            return 0;
        }
        if (wgrade.pronunciation == null) {
            // 発音がない場合（未知語）には表記の文字数を使う
            return wgrade.word.length();
        }
        for (int i = 0; i < wgrade.pronunciation.length(); i++) {
            char c = wgrade.pronunciation.charAt(i);
            if (c != 'ァ' && c != 'ィ' && c != 'ゥ' && c != 'ェ' && c != 'ォ' &&
                    c != 'ャ' && c != 'ュ' && c != 'ョ')
                n++;
        }
        //System.err.println("length of "+wgrade.pronunciation+" = "+Integer.toString(n));
        return n;
    }
}