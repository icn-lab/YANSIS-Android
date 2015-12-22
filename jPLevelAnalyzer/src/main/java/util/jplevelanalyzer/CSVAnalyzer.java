/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util.jplevelanalyzer;

import java.util.ArrayList;

/**
 * A Class to analyze CSV file
 * @author aito
 */
public class CSVAnalyzer {

    static final int AFTER_COMMA = 0;
    static final int IN_RAW_ITEM = 1;
    static final int IN_QUOTED_ITEM = 2;
    
    public class ContinuationException extends Exception {
        public ContinuationException(String message) {
            super(message);
        }
    }

    int status;
    ArrayList<String> res;
    StringBuilder buf;
    /**
     * Generates an analyzer instance.
     * 
     */
    public CSVAnalyzer() {
        status = AFTER_COMMA;
        res = null;
    }
    /**
     * Splits the input CSV line into an array of String.
     * @param line the input line.
     * @param removeTrailingEmptyFields 
     * if true, empty elements at the last of the array will be removed.
     * @return array of the split strings
     * @throws ContinuationException
     */
    public String[] split(String line, boolean removeTrailingEmptyFields) 
        throws ContinuationException {
        if (res == null)
            res = new ArrayList<String>();
        if (buf == null)
            buf = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (status) {
                case IN_RAW_ITEM:
                    if (c == ',') {
                        res.add(buf.toString());
                        buf.delete(0, buf.length());
                        status = AFTER_COMMA;
                    } else {
                        buf.append(c);
                    }
                    break;
                case IN_QUOTED_ITEM:
                    if (c == '"') {
                        if (i == line.length() - 1 || line.charAt(i + 1) == ',') {
                            status = IN_RAW_ITEM;
                        } else if (line.charAt(i + 1) == '"') {
                            // escaped sequence for "
                            i++;
                            buf.append(c);
                        } else {
                            throw new IllegalArgumentException("Ill-formatted CSV: " + line);
                        }
                    } else {
                        buf.append(c);
                    }
                    break;
                case AFTER_COMMA:
                    if (c == '"') {
                        status = IN_QUOTED_ITEM;
                    } else if (c == ',') {
                        // comma after comma
                        res.add("");                        
                    } else {
                        status = IN_RAW_ITEM;
                        buf.append(c);
                    }
                    break;
            }
        }
        if (status == IN_QUOTED_ITEM)
            throw(new ContinuationException("In Quoted Item"));
        res.add(buf.toString());
        int arysize = res.size();
        if (arysize == 0)
            return new String[0];
        if (removeTrailingEmptyFields) {
            for (; arysize > 0 && res.get(arysize-1).length() == 0; arysize--)
                ;
            if (arysize == 0)
                return new String[0];
        }
        String[] ret_val = new String[arysize];
        for (int i = 0; i < ret_val.length; i++) {
            ret_val[i] = res.get(i);
        }
        res.clear();
        buf = null;
        status = AFTER_COMMA;
        return ret_val;
    }
}
