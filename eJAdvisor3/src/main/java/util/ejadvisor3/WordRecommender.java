/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util.ejadvisor3;

/**
 *
 * @author aito
 */
public interface WordRecommender {
    public String[] getSimilarWord(String qword, int n);
    public String[] getSimilarWord(WordProperty qw, int n);
}
