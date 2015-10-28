package util.jplevelanalyzer;
/*
 * VocabAnalyzer.java
 *
 * Created on 2008/02/13, 13:57
 *
 * To change this template, choose Tools | Template Manager
* and open the template in the editor
 */

/**
 *
 * @author aito
 */
import net.java.sen.Token;
import java.util.*;


public class VocabAnalyzer {
    
    GradeTable[] grade;
    
    /*
    static int match(VocabGrade vocab, Token[] toks, int i, int grade) {
        int m = vocab.match(toks,i);
        if (m > 0) {
            if (m == 1)
                System.out.print(toks[i].getBasicString());
            else {
                for (int j = 0; j < m; j++)
                    System.out.print(toks[i+j].toString());
            }
            System.out.println(" "+Integer.toString(grade));
        }
        return m;
    }
     */
    
    public VocabAnalyzer(GradeTable[] tab) {
        grade = tab;
    }
    
    public WordWithGrade[] analyze(Token toks[]) {
        ArrayList<WordWithGrade> result = new ArrayList<WordWithGrade>();
        int i = 0;
        int m;
        while (toks != null && i < toks.length) {
            // まず複合語の照合
            int g;
            m = 0;
            for (g = 0; g < grade.length; g++) {
                m = grade[g].matchMulti(toks,i);
                if (m > 0) {
                    String w = VocabGrade.concat(toks,i,m);
                    result.add(new WordWithGrade(VocabGrade.concatSurface(toks,i,m),
                            VocabGrade.concat(toks,i,m),
                            VocabGrade.concatReading(toks,i,m),
                            VocabGrade.concatPronunciation(toks,i,m),
                            toks[i+m-1].getPos(),
                            toks[i+m-1].getCform(),
                            grade[g].grade));
                    i += m;
                    break;
                }
            }
            if (m > 0)
                continue;
            // 続いて1単語の照合
            for (g = 0; g < grade.length; g++) {
                m = grade[g].matchOne(toks,i);
                if (m > 0) {
                    /*
                    System.out.println("["+Integer.toString(g)+"]"+
                                       Integer.toString(grade[g].grade)+
                                       ":matchOne "+
                                       toks[i].toString());
                     */
                    result.add(new WordWithGrade(toks[i], grade[g].grade));
                    i++;
                    break;
                }
            }
            if (m > 0)
                continue;
            result.add(new WordWithGrade(toks[i],0));
            i++;
        }
        WordWithGrade[] res = new WordWithGrade[result.size()];
        for (i = 0; i < result.size(); i++)
            res[i] = result.get(i);
        return (WordWithGrade[])res;
    }
}
