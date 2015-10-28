package util.jplevelanalyzer;
/*
 * POS.java
 *
 * Created on 2008/02/13, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author aito
 */
//
// 品詞情報を扱うクラス
//

public class POS {
    String posClass[];
    public POS(String s) {
	posClass = s.split("-");
    }
    public boolean match(POS p) {
	//System.out.print("POS match("+toString()+","+p.toString()+") = ");
	for (int i = 0; i < posClass.length; i++) {
	    if (posClass[i].equals("*") ||
		p.posClass[i].equals("*"))
		continue;
	    if (!posClass[i].equals(p.posClass[i])) {
		//		System.out.println("false");
		return false;
	    }
	}
	//	System.out.println("true");
	return true;
    }
    @Override
    public String toString() {
	String s = posClass[0];
	for (int i = 1; i < posClass.length; i++)
	    s += "-"+posClass[i];
	return s;
    }
}