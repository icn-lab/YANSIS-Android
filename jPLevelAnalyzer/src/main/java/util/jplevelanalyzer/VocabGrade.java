package util.jplevelanalyzer;
/*
 * VocabGrade.java
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
// VocabGrade: 各級の語彙とその認定条件（品詞，複合語など）
//
import java.util.*;
import java.io.*;
import net.java.sen.Token;

class VocabGradeItem {
    public String word;
    public String yomi;
    public POS pos;
    public int grade;
    public int num_word;
    public VocabGradeItem(String word, String yomi, 
			  int grade, String pos,
			  int num_word) {
	this.word = word;
	this.yomi = yomi;
	this.grade = grade;
	if (!"".equals(pos))
	    this.pos = new POS(pos);
	else
	    pos = null;
	this.num_word = num_word;
	//System.out.println(word+":"+yomi);
    }
    public boolean matchOne(Token inword) {
	//	System.out.println(word+".matchOne("+inword.getBasicString()+")  pos="+pos);
	//System.out.println(inword.getBasicString().equals(word));
	if (pos != null) {
            // 品詞まで合わないとNG
            return inword.getBasicString().equals(word) &&
                    pos.match(new POS(inword.getPos()));
	}
	else return inword.getBasicString().equals(word);
    }
}

class VocabHash {
    HashMap<String,ArrayList<VocabGradeItem>> hash;
    public VocabHash() {
	hash = new HashMap<String,ArrayList<VocabGradeItem>>();
    }
    public void put(String key, VocabGradeItem value) {
	ArrayList<VocabGradeItem> its = hash.get(key);
	if (its == null) {
	    its = new ArrayList<VocabGradeItem>();
	    its.add(value);
	    hash.put(key,its);
	}
	else {
	    its.add(value);
	}
    }
    public VocabGradeItem[] get(String key) {
	ArrayList<VocabGradeItem> its;
	its = hash.get(key);
	if (its == null)
	    return null;
	VocabGradeItem[] items = new VocabGradeItem[its.size()];
	for (int i = 0; i < its.size(); i++)
	    items[i] = its.get(i);
	return items;
    }
}

public class VocabGrade {
    VocabHash[] vocab;
    static final int max_len = 10; // 複合語の最大長
    /** file: filename of the CSV file
     * @param file
     * @throws java.io.IOException */
    public VocabGrade(String file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis,"SHIFT_JIS");
	BufferedReader inp = new BufferedReader(isr);
	String line;
        CSVAnalyzer csvAnalyzer = new CSVAnalyzer();
	vocab = new VocabHash[max_len];
	for (int i = 0; i < max_len; i++) {
	    vocab[i] = new VocabHash();
	}
	while ((line = inp.readLine()) != null) {

	    // 入力行のフォーマット
	    // 読み, 級, 単語 [,{複合語の単語数 | 品詞}]
	    
	    String[] x;
            try {
                x = csvAnalyzer.split(line,true);
            } catch (CSVAnalyzer.ContinuationException e) {
                continue;
            }
	    int grade = Integer.parseInt(x[1]);
	    int num_word = 0;
	    String pos;
	    if (x.length == 3) {
		num_word = 1;
		pos = "";
	    } else {
		if (x[3].length() == 1) {
		    // 4番目のフィールドが複合語の単語数の場合
                    try {                         
                        num_word = Integer.parseInt(x[3]);
                    } catch (NumberFormatException e) {
                        System.err.println("CSV Format error: file="+file+", line="+line);
                        //e.printStackTrace();
                        //System.exit(1);
                    }
		    pos = "";
		}
		else {
		    // 4番目のフィールドが品詞の場合
		    num_word = 1;
		    pos = x[3];
		}
	    }
	    vocab[num_word-1].put(x[2],
		      new VocabGradeItem(x[2],x[0],grade,pos,num_word));
	    if (x[0] == null ? x[2] != null : !x[0].equals(x[2])) {
		// 見出し語と読みが異なる場合，読み（かな表記）を
		// 見出し語としたエントリも登録する
		vocab[num_word-1].put(x[0],
			  new VocabGradeItem(x[0],x[0],grade,pos,num_word));
	    }
	}
	inp.close();
    }
    /** 複合語をつなげて文字列を作成する
     * つなげる場合，最後のトークンが活
     * @param toks
     * @param ind
     * @param n
     * 用語のときのみ
     * 原型を使う．それ以外の場合には表層を使う．
     * @return 
     */
    public static String concat(Token[] toks, int ind, int n) {
	String w = "";
	for (int i = 0; i < n; i++) {
	    Token t = toks[ind+i];
	    String p = t.getPos();
	    if (i == n-1 &&
		(p.startsWith("動詞-") ||
		 p.startsWith("形容詞-") ||
		 p.startsWith("助動詞")))
		w += t.getBasicString();
	    else
		w += t.toString();
	}
	return w;
    }

    /** 原形をつなげて文字列を作成する
     * @param toks
     * @param ind
     * @param n
     * @return 
     */
    public static String concatSurface(Token[] toks, int ind, int n) {
	String w = "";
	for (int i = 0; i < n; i++) {
	    Token t = toks[ind+i];
            w += t.toString();
	}
	return w;
    }

    /** 発音をつなげて文字列を作成する
     * @param toks
     * @param ind
     * @param n
     * @return 
     */
    public static String concatPronunciation(Token[] toks, int ind, int n) {
	String w = "";
	for (int i = 0; i < n; i++) {
	    Token t = toks[ind+i];
            w += t.getPronunciation();
	}
	return w;
    }
   /** 読みをつなげて文字列を作成する
     * @param toks
     * @param ind
     * @param n
     * @return 
     */
    public static String concatReading(Token[] toks, int ind, int n) {
	String w = "";
	for (int i = 0; i < n; i++) {
	    Token t = toks[ind+i];
            w += t.getReading();
	}
	return w;
    }

    /** トークンの ind 番目から複合語をマッチングする。
     * マッチした単語数を返す。
     * @param toks
     * @param ind
     * @return 
     */
    public int matchMulti(Token[] toks, int ind) {
	VocabGradeItem[] items;
	VocabGradeItem item;
	//System.out.println(item);

	// maxは複合語の最大数。
	int max = toks.length-ind;
	if (max > max_len)
	    max = max_len;
	//System.out.println("toks.length="+Integer.toString(toks.length));
	//System.out.println("max="+Integer.toString(max));
	int i;
	for (i = max-1; i >= 1; i--) {
	    String w = concat(toks,ind,i+1);
	    items = vocab[i].get(w);
	    /*
	    System.out.println("composite match "+Integer.toString(i)+
	    	       "("+w+
	    	       ")="+Boolean.toString(items != null));
	    */
	    if (items != null)
		return i+1;
	}
	return 0;
    }
    // トークンの ind 番目から複合語をマッチングする。
    // マッチした単語数を返す。
    public int matchOne(Token[] toks, int ind) {
	VocabGradeItem[] items;
	items = vocab[0].get(toks[ind].getBasicString());
	if (items == null)
	    return 0;
        for (VocabGradeItem item : items) {
            if (item.matchOne(toks[ind])) {
                return 1;
            }
        }
	return 0;
    }
}
		
						
