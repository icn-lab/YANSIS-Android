/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hanasu;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nagano
 */
public class LabelItem {
    int startTime, endTime;
    String phoneme, f0, rest;
	
    final static String[] moraPhones = {"a","i","u","e","o","cl","N"};
    final static String[] exceptDuration = {"pau", "sil"};
    final static String  regexTimeAndLabel   ="^(\\d+)\\s+(\\d+)\\s+(\\S+)$";
    final static Pattern patternTimeAndLabel = Pattern.compile(regexTimeAndLabel);
    final static String  regexPhoneme   = "^\\w+\\^\\w+-(\\w+)\\+";
    final static Pattern patternPhoneme = Pattern.compile(regexPhoneme);
    final static String  regexF0 = "^A:.+\\+(.+)\\+";
    final static Pattern patternF0 = Pattern.compile(regexF0);
	
    LabelItem(){
	startTime  = 0;
	endTime    = 0;
	phoneme   = null;
	rest      = null;
    }
	
    LabelItem(String string){		
	Matcher m = patternTimeAndLabel.matcher(string);
	String label = null;
		
	if(m.find()){
		startTime = Integer.parseInt(m.group(1));
		endTime   = Integer.parseInt(m.group(2));
		label     = m.group(3);
	}
	else{
		System.err.printf("label error:%s\n", string);
		System.exit(1);
	}
		
	phoneme = null;
	f0      = null;
		
	if(label != null){
            Matcher mm = patternPhoneme.matcher(label);
            if(mm.find()){
                phoneme = mm.group(1);
            }
            String[] part = label.split("/");
            Matcher mmm = patternF0.matcher(part[1]);

            if(mmm.find()){
              	f0 = mmm.group(1);
            }
			
            rest = "";
            for(int i=2;i < part.length;i++){
		rest += "/" + part[i]; 
            }        
        }
    }

    public int toMS(int value){
	return value / 10000;
    }
	
    public void setStartTime(int stime){
	startTime = stime;
    }
	
    public int getStartTime(){
	return startTime;
    }
	
    public void setEndTime(int etime){
	endTime = etime;
    }

    public int getEndTime(){
	return endTime;
    }
	
    public int getStartTimeMS(){
	return toMS(startTime);
    }
	
    public int getendTimeMS(){
	return toMS(endTime);
    }
	
    public int getDuration(){
	return endTime - startTime;
    }
	
    public String getPhoneme(){
	return phoneme;
    }
	
    public String getF0(){
	return f0;
    }
	
    public int getF0Value(){
	return Integer.valueOf(f0);
    }
	
    public String toString(){
	return String.format("%d %d %s %s", startTime, endTime, f0, rest);
    }
	
    public Boolean isMoraPhone(){
	Boolean flag = false;
	for(int i=0;i < moraPhones.length;i++)
		if(moraPhones[i].equals(phoneme)){
			flag = true;
			break;
		}
		
	return flag;
    }
	
    public Boolean isExceptDuration(){
	Boolean flag = false;
	for(int i=0;i < exceptDuration.length;i++){
            if(exceptDuration[i].equals(phoneme)){
		flag = true;
		break;
            }
	}
		
	return flag;
    }
}
