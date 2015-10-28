package util.jplevelanalyzer;
/*
 * GradeTable.java
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
import java.io.*;

public class GradeTable extends VocabGrade{
    public int grade;
    public GradeTable(String file, int grade)  throws IOException {
	super(file);
	this.grade = grade;
    }
}
