package util.ejadvisor3;

import java.io.*;
import java.util.regex.*;

class ScoreEstimator {
    private double[] weight;
    private String regex_kanji;
    private String regex_hiragana;
    private String regex_katakana;
    private Pattern pattern_kanji;
    private Pattern pattern_hiragana;
    private Pattern pattern_katakana;
    public static final int NDIM = 25;

    public ScoreEstimator(String filename){
	readWeight(filename);

	regex_kanji   = "^[一-龠]+$";
	pattern_kanji = Pattern.compile(regex_kanji);

	regex_hiragana   = "^[[ぁ-ゞ]|ー]+$";
	pattern_hiragana = Pattern.compile(regex_hiragana);

	regex_katakana   = "^[[ァ-ヾ]|ー]+$";
	pattern_katakana = Pattern.compile(regex_katakana);
    }

    public void readWeight(String filename){
	weight = new double[NDIM+1];
	try{
	    File file = new File(filename);
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    for(int i=0;i < NDIM+1;i++){
		weight[i] = Double.valueOf(br.readLine());
	    }
	    br.close();
	}catch(IOException e){
	}
    }
    
    public double calcScore(double[] vector){
	double score = weight[0];
	for(int i=0;i < vector.length;i++){
	    score += weight[i+1] * vector[i];
	}
	return score;
    }

    public double estimateScore(WordProperty[] w){
	double[] vector = calcFeatureVector(w);
	double score = calcScore(vector);

	return score;
    }
    
    public void printVector(double[] vector){
	for(int i=0;i < vector.length;i++){
	    System.out.printf("%.3f ", vector[i]);
	}
	System.out.println("");
    }

    public double[] calcFeatureVector(WordProperty[] w){
	double[] vector = new double[NDIM];
	int count = 0;
	
	vector[count++] = getCharacterCount(w);
	vector[count++] = getWordCount(w);
	vector[count++] = getNounCount(w);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getVerbCount(w);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getAverageLevel(w);
	vector[count++] = getLevelNWordCount(w, 0);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getLevelNWordCount(w, 1);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getLevelNWordCount(w, 2);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getLevelNWordCount(w, 3);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getLevelNWordCount(w, 4);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getLoanWordCount(w);
	vector[count++] = vector[count-2] / vector[1];
	vector[count++] = getKanjiCount(w);
	vector[count++] = vector[count-2] / vector[0];
	vector[count++] = getHiraganaCount(w);
	vector[count++] = vector[count-2] / vector[0];
	vector[count++] = getKatakanaCount(w);
	vector[count++] = vector[count-2] / vector[0];

	return vector;
    }

    public int getCharacterCount(WordProperty w){
	if(w.getPOS().startsWith("記号")){
	    return 0;
	}
	String str = w.toString();
	return str.length();
    }

    public int getCharacterCount(WordProperty[] w){
	int count=0;

	for(int i=0;i < w.length;i++){
	    count += getCharacterCount(w[i]);
	}

	return count;
    }

    public int getWordCount(WordProperty[] w){
	int count = 0;
	for(int i=0;i < w.length;i++){
	    if(w[i].getPOS().startsWith("記号")){
		continue;
	    }
	    count+=1;
	}
	return count;
    }

    /*
    public int getBunsetsuCount(WordProperty[] w){
    }
    public int getBunsetsuCount(WordProperty w){
    }
    */

    public boolean isNoun(WordProperty w){
	String pos = w.getPOS();
	if(pos.startsWith("名詞"))
	    return true;
	else
	    return false;
    }

    public int getNounCount(WordProperty[] w){
	int count = 0;

	for(int i=0;i < w.length;i++){
	    if(isNoun(w[i]))
		count++;
	}
	
	return count;
    }

    public boolean isVerb(WordProperty w){
	String pos = w.getPOS();
	if(pos.startsWith("動詞"))
	    return true;
	else
	    return false;
    }

    public int getVerbCount(WordProperty[] w){
	int count = 0;

	for(int i=0;i < w.length;i++){
	    if(isVerb(w[i]))
	       count++;
	}

	return count;
    }

    public double getAverageLevel(WordProperty[] w){
	int[] count = new int[5];
	int sum=0;
	int total=0;

	for(int i=0;i < count.length;i++){
	    count[i] = getLevelNWordCount(w, i);
	    sum += i * count[i];
	    total += count[i];
	}

	return (double)sum / (double)total;
    }
    
    public int getLevelNWordCount(WordProperty[] w, int level){
	int count = 0;

	for(int i=0;i < w.length;i++){
	    if(w[i].getGrade() == level)
		count++;
	}

	return count;
    }

    public boolean isLoanWord(WordProperty w){
	String str = w.toString();
	Matcher m  = pattern_katakana.matcher(str);

	if(m.find())
	    return true;
	else
	    return false;
    }

    public int getLoanWordCount(WordProperty[] w){
	int count = 0;

	for(int i=0;i < w.length;i++){
	    if(isLoanWord(w[i]))
	       count++;
	}

	return count;
    }

    public int getKanjiCount(WordProperty w){
	String str = w.toString();
	int count = 0;

	for(int i=0;i < str.length();i++){
	    Matcher m = pattern_kanji.matcher(str.substring(i, i+1));
	    if(m.find())
		count++;
	}

	return count;
    }

    public int getKanjiCount(WordProperty[] w){
	int count = 0;
	
	for(int i=0;i < w.length;i++){
	    count += getKanjiCount(w[i]);
	}

	return count;
    }

    public int getHiraganaCount(WordProperty w){
	String str = w.toString();
	int count = 0;

	for(int i=0;i < str.length();i++){
	    Matcher m = pattern_hiragana.matcher(str.substring(i, i+1));
	    if(m.find())
		count++;
	}

	return count;
    }

    public int getHiraganaCount(WordProperty[] w){
	int count = 0;
	for(int i=0;i < w.length;i++){
	    count += getHiraganaCount(w[i]);
	}

	return count;
    }

    public int getKatakanaCount(WordProperty w){
	String str = w.toString();
	int count = 0;

	for(int i=0;i < str.length();i++){
	    Matcher m = pattern_katakana.matcher(str.substring(i, i+1));
	    if(m.find())
		count++;
	}

	return count;
    }

    public int getKatakanaCount(WordProperty[] w){
	int count = 0;
	for(int i=0;i < w.length;i++){
	    count += getKatakanaCount(w[i]);
	}

	return count;
    }

}