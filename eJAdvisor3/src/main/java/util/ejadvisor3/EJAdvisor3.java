/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;


import java.io.IOException;
import java.util.ArrayList;
import android.util.Log;
import net.java.sen.Token;
/**
 *
 * @author Akinori
 */
public class EJAdvisor3 {
    private WordPropertyFactory analyzer;
    private EJConfig conf;
    private Recommendation recommend;
    private WordProperty[][] currentSent; // 現在分析中の文
    private static String base;
    private ExampleFinder examples;
    private ScoreEstimator scoreEstimator;
    private Token[] toks;
    private static final String[] REPLACE
            = {"～", "〜",
            "\\.", "．",
            "%", "％",
            "\\+", "＋",
            "\\*", "＊",
            "-", "−",
            "/", "／",
            "=", "＝"
    };

    public EJAdvisor3(String baseDir){
    	base = baseDir+"/";
    	initialize();
    }
        
    /**
     * Initialize myself
     */
    void initialize() {    
        String morphPath = base+"morph/";
        conf = new EJConfig(morphPath,6);
        conf.sen_conf = base+"sen/conf/sen.xml";
        conf.easyword = morphPath+"easyword.txt";
        
        try {            
            examples = new ExampleFinder(morphPath+"Examples.csv");
            conf.set_grade("vocabS.csv", 6); // 記号
            conf.set_grade("vocabB.csv", 5); // 文法項目
            conf.set_grade("vocab4.csv", 4); // 4級
            conf.set_grade("vocab3.csv", 3); // 3級
            conf.set_grade("vocab2.csv", 2); // 2級
            conf.set_grade("vocab1.csv", 1); // 1級
            //splash.setMessage("Senを起動しています...");
            analyzer = new WordPropertyFactory(conf);   
            //splash.setMessage("アドバイスを読みこんでいます...");
            recommend = new Recommendation(morphPath+"GrammaticalRecommendation.csv");
            scoreEstimator = new ScoreEstimator(morphPath+"score-foreign-all.w");
            //wordRecommender = new WordRecommenderByLSA(conf);
        } catch (IOException e) {
            Log.e("EJAdvisor3", "IO error on initialization:"+e.toString());
            System.exit(1);
        }
    }
    
    // 文終端かどうかを判別する
    private boolean isSentenceEnd(WordProperty w) {
        if (w.getPOS().equals("記号-句点"))
            return true;
        if (w.toString().equals("？"))
            return true;
        if (w.toString().equals("！"))
            return true;
        return false;
    }
    
    // WordPropertyの配列を句読点で区切って複数の文に分ける
    private WordProperty[][] splitSentence(WordProperty w[]) {
        ArrayList<Integer> bpos = new ArrayList<Integer>();
        bpos.add(new Integer(0));
        for (int i = 0; i < w.length; i++) {
            if (isSentenceEnd(w[i]) && i < w.length - 1) {
                bpos.add(new Integer(i + 1));
            }
        }
        bpos.add(new Integer(w.length));
        WordProperty[][] res = new WordProperty[bpos.size() - 1][];
        for (int i = 1; i < bpos.size(); i++) {
            int n = bpos.get(i) - bpos.get(i - 1);
            res[i - 1] = new WordProperty[n];
            for (int j = 0; j < n; j++) {
                res[i - 1][j] = w[bpos.get(i - 1) + j];
            }
        }
        return res;
    }
    
    public WordProperty currentMorph(int s, int i) {
        return currentSent[s][i];
    }
    
    /**
     * Performs analysis
     * @param t
     */
    public WordProperty[][] doAnalysis(String t)  {
        WordProperty[] w;
        
        try {
        	Log.d("doAnalysis", "text:"+t);
        	
            w = analyzer.analyzeText(t);
            toks = analyzer.getToken();
            currentSent = splitSentence(w);    
        }catch (IOException e) {
        	Log.e("EJAdvisor3", "IO error on doAnalysis:"+e.toString());
        	System.exit(1);
        }
        
        return currentSent;
    }

    public double estimateScore(WordProperty[] w){
        double score = scoreEstimator.estimateScore(w);
        System.out.println(""+score);
        score = score * 100.0 / 2.0;
        if(score > 100.0)
            score = 100.0;
        return score;
    }

    public String[] getRecommendations(WordProperty[] w){
        return recommend.getRecommendations(w);
    }

    /**
     * 与えられた単語に近い単語を推薦する
     * @param w
     */
    public EJExample[] exampleSentence(WordProperty w) {
        //String res[] = wordRecommender.getSimilarWord(w, n);
        EJExample[] res = examples.grepNJ(w.getBasicString());

        return res;
    }

    public String hankakuToZenkaku(String text) {
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if ('0' <= c && c <= '9') {
                sb.setCharAt(i, (char) (c - '0' + '０'));
            } else if ('A' <= c && c <= 'Z') {
                sb.setCharAt(i, (char) (c - 'A' + 'Ａ'));
            } else if ('a' <= c && c <= 'z') {
                sb.setCharAt(i, (char) (c - 'a' + 'ａ'));
            }
        }

        return sb.toString();
    }

    public String replace(String text) {
        for (int i = 0; i < REPLACE.length; i += 2) {
            text = text.replaceAll(REPLACE[i], REPLACE[i + 1]);
        }

        return text;
    }

    public Token[] getTokens() {
        return toks;
    }
}
