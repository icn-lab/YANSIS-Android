/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hanasu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author nagano
 */
public class Label {
    	ArrayList<LabelItem> items;
	
	Label(File file){
		items = new ArrayList<LabelItem>();
		
		try{
			FileReader     fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while(true){
				String line = br.readLine();
				if(line == null)
					break;
				LabelItem item = new LabelItem(line);
				items.add(item);			
			}
			
			br.close();
			fr.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	Label(String[] label){
		items = new ArrayList<LabelItem>();
		
		for(int i=0;i < label.length;i++){
			//System.err.printf("line:[%s]\n",label[i]);
			LabelItem item = new LabelItem(label[i]);
			items.add(item);			
		}			
	}
	
	public String toString(){
		String retStr = "";
		
		int itemsSize = items.size();

		String str = toLabelString(null, items.get(0), items.get(1));
		retStr += str + "\n";
		
		for(int i=1;i < itemsSize-1;i++){
			str = toLabelString(items.get(i-1), items.get(i), items.get(i+1));
			retStr += str + "\n";
		}
		
		str = toLabelString(items.get(itemsSize-2), items.get(itemsSize-1), null);
		retStr += str + "\n";
		
		return retStr;
	}
	
	public void save(File file){
		try{
			FileWriter fw     = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(this.toString());
			/*
			for(LabelItem item:items){
				String str = item.toString();
				bw.write(str);
				bw.newLine();
			}
			*/			
			bw.close();
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String toLabelString(LabelItem pre, LabelItem cur, LabelItem succ){
		if(pre == null){
			pre = new LabelItem("0 0 xx^x-x+xx=xx/A:xx+xx+xx/B:xx-xx_xx/C:xx_xx+xx/D:xx+xx_xx/E:xx_xx!xx_xx-xx/F:xx_xx#xx_xx|xx_xx/G:4_3%xx_xx-xx/H:xx_xx/I:xx-xx+xx&xx-xx|xx+xx/J:xx_xx/K:xx+xx-xx");
		}
		if(succ == null){
			succ = new LabelItem("0 0 xx^x-x+xx=xx/A:xx+xx+xx/B:xx-xx_xx/C:xx_xx+xx/D:xx+xx_xx/E:xx_xx!xx_xx-xx/F:xx_xx#xx_xx|xx_xx/G:4_3%xx_xx-xx/H:xx_xx/I:xx-xx+xx&xx-xx|xx+xx/J:xx_xx/K:xx+xx-xx");
		}
		
		// time
		String timeStr = cur.startTime +" "+ cur.endTime;
		// phoneme
		String pStr = phonemeLabel(pre.phoneme, cur.phoneme, succ.phoneme);
		// qf0
		String f0Str = f0Label(pre.f0, cur.f0, succ.f0);
		
		return timeStr+" "+pStr+f0Str+cur.rest;
	}
	
	public String phonemeLabel(String pre, String cur, String succ){
		return "xx^"+pre+"-"+cur+"+"+succ+"=xx";
	}
	
	public String f0Label(String pre, String cur, String succ){
		return "/A:"+pre+"+"+cur+"+"+succ;
	}
	
	/*
	public void operateDuration(double ratio){
		int stime = 0;
		for(LabelItem item:items){
			double duration = item.getDuration();
			if(item.isMoraPhone())
				duration = duration * ratio + 0.5;
			
			item.setStartTime(stime);
			item.setEndTime(stime + (int)duration);
			stime = stime + (int)duration;
		}
	}
	*/
	
	public int getMoraSpeed(){
		return (int)(getMoraCount() * 1e+07 * 60 / getTotalDuration() + 0.5);
	}

	public int getMoraCount(){
		int count = 0;
		
		for(LabelItem item:items){
			if(item.isMoraPhone())
				count += 1;
		}

		return count;
	}
	
	public int getTotalMoraDuration(){
		int duration = 0;

		for(LabelItem item:items){
			if(item.isMoraPhone())
				duration += item.getDuration();
		}
		
		return duration;
	}
	
	public int getTotalDuration(){
		int duration = 0;
		
		for(LabelItem item:items){
			if(item.isExceptDuration() == false)
				duration += item.getDuration();
		}
		
		return duration;
	}
	
	public int getTotalNonMoraDuration(){
		return getTotalDuration() - getTotalMoraDuration();
	}	
}