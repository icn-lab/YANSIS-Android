/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;
import java.util.ArrayList;
import util.jplevelanalyzer.CSVAnalyzer;
import java.io.*;

/**
 * 事例の検索をするクラス．
 * @author aito
 */
public class ExampleFinder {
    ArrayList<EJExample> examples;
    ExampleFinder(String filename) throws IOException {
        examples = new ArrayList<EJExample>();
        FileInputStream fir = new FileInputStream(filename);
        BufferedReader rd = new BufferedReader(new InputStreamReader(fir,"SHIFT_JIS"));
        String line;
        CSVAnalyzer sp = new CSVAnalyzer();
        while ((line = rd.readLine()) != null) {
            String[] x;
            try {
                x = sp.split(line, true);
            } catch(CSVAnalyzer.ContinuationException e) {
                continue;
            }
            if (x.length < 2)
                throw new IOException("Illegal number of fields:"+Integer.toString(x.length));
            examples.add(new EJExample(x[0],x[1]));
        }
        rd.close();
    }

    public EJExample[] grepNJ(String key) {
        ArrayList<EJExample> res = new ArrayList<EJExample>();
        for (int i = 0; i < examples.size(); i++) {
            EJExample e = examples.get(i);
            if (e.containsNJ(key))
                res.add(e);
        }
        EJExample[] ret_val = new EJExample[1];
        ret_val = res.toArray(ret_val);
        return ret_val;
    }

}
