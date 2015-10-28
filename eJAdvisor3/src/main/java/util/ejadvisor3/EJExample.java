/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;
/**
 * NJとEJの対訳例のクラス
 * @author aito
 */
public class EJExample {
    String nj;
    String ej;
    public EJExample(String nj, String ej) {
        this.nj = nj;
        this.ej = ej;
    }
    public boolean containsNJ(String key) {
        return nj.contains(key);
    }
    public boolean containsEJ(String key) {
        return ej.contains(key);
    }
    public String NJ() {
        return nj;
    }
    public String EJ() {
        return ej;
    }
}
