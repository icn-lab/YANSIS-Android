package util.jplevelanalyzer;
/*
 * WordWithGrade.java
 *
 * Created on 2008/02/13, 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author aito
 */
//
// 単語とその級の組のクラス
//
import net.java.sen.Token;

public class WordWithGrade {
    public static final WordWithGrade EOS = new WordWithGrade("EOS","EOS","EOS","EOS","EOS","EOS",-1);
    public String word;
    public String basicString;
    public int grade;
    public String pos;
    public String reading;
    public String pronunciation;
    public String cform;
    public WordWithGrade(String word, String basicString, String reading, String pronunciation, String pos, String cform, int grade) {
	this.word = word;
	this.grade = grade;
        this.pos = pos;
        this.basicString = basicString;
        this.cform = cform;
        this.reading = reading;
        this.pronunciation = pronunciation;
    }
    public WordWithGrade(Token tok, int grade) {
	this.word = tok.toString();
	this.grade = grade;
        this.pos = tok.getPos();
        this.basicString = tok.getBasicString();
        this.reading = tok.getReading();
        this.pronunciation = tok.getPronunciation();
        this.cform = tok.getCform();
    }
}
