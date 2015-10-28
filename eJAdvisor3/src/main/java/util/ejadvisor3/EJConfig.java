/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;

import util.jplevelanalyzer.GradeTable;
import java.io.IOException;

/**
 *
 * @author aito
 */
public class EJConfig {
    public String base_dir;
    public int n_grade;
    public GradeTable[] grade;
    public String sen_conf;  // configuration file of sen
    //public String LSAdir;
    //public String LSAVocabFile;
    public String easyword;
    private int current_grade;
    public EJConfig(String base_dir,int n) {
        this.base_dir = base_dir;
        n_grade = n;
        grade = new GradeTable[n];
        current_grade = 0;
    }
    public void set_grade(String file,int n) throws IOException {
        grade[current_grade++] = new GradeTable(base_dir+file,n);
    }
}
