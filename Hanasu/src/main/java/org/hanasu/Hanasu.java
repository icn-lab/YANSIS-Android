/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hanasu;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.Gyutan.Gyutan;
import net.java.sen.Token;

/**
 *
 * @author nagano
 */
public class Hanasu {
    Gyutan gyutan;
    String[] feature;
    double ratio = 1.0;

    public Hanasu(String htsVoice) {
        // initialize Gyutan;
        gyutan = new Gyutan();
        gyutan.initializeEngine(htsVoice);
    }

    public void doSynthesize(int speed) {
        if (feature == null) {
            return;
        }

        gyutan.make_label(feature);

        int sp1 = synthesizeAndCalcMoraSpeed(ratio);
        int diff = sp1 - speed;
        int cnt = 0;

        if (gyutan.availableEngine()) {
            while (cnt < 20) {
                if (diff == 0) {
                    break;
                }

                double ratio2 = ratio - 0.001 * diff;
                int sp2 = synthesizeAndCalcMoraSpeed(ratio2);
                int diff2 = sp2 - speed;
                //System.err.printf("speed:%d sp1:%d, sp2:%d, ratio:%f, ratio2:%f, diff=%d, diff2=%d\n", speed, sp1, sp2, ratio, ratio2, diff, diff2);

                if (Math.abs(diff) < Math.abs(diff2)) {
                    break;
                }

                diff = diff2;
                ratio = ratio2;
                sp1 = sp2;
                cnt++;
            }

            gyutan.set_audio_buff_size(48000*60*3);
            gyutan.set_speed(ratio);
            gyutan.synthesis(null, null);
        }
    }

    public int synthesizeAndCalcMoraSpeed(double ratio) {
        gyutan.set_speed(ratio);
        gyutan.set_audio_buff_size(0);
        gyutan.synthesis(null, null);

        String[] strings = gyutan.get_label(true);
        Label label = new Label(strings);

        return label.getMoraSpeed();
    }
    /*
     public void setHTSVoice() {
     JFileChooser fileChooser = new JFileChooser(htsVoiceDir);
     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

     // add .htsvoice extension filter
     FileFilter filter = new FileNameExtensionFilter("htsvoice file", "htsvoice");
     fileChooser.addChoosableFileFilter(filter);

     while (true) {
     int selected = fileChooser.showOpenDialog(frame);
     if (selected == JFileChooser.APPROVE_OPTION) {
     htsVoice = fileChooser.getSelectedFile().toString();
     gyutan.initializeEngine(htsVoice);
     if (gyutan.availableEngine()) {
     property.setProperty("htsVoice", htsVoice);
     break;
     }
     } else {
     break;
     }
     }

     setSynthesizeButtonState();
     }
     */
/*
    public void saveWAV(String filename) {
        try {
            gyutan.save_riff(new FileOutputStream(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
    /*
     public void saveWAV() {
     JFileChooser fileChooser = new JFileChooser();
     fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
     int selected = fileChooser.showSaveDialog(frame);
     try {
     if (selected == JFileChooser.APPROVE_OPTION) {
     gyutan.save_riff(new FileOutputStream(fileChooser.getSelectedFile()));
     }
     } catch (Exception e) {
     e.printStackTrace();
     }
     }
     */

    public void setTokens(Token[] toks) {
        feature = gyutan.tokenToString(toks);

         for(int i=0;i < feature.length;i++){
         Log.d("d", String.format("%d:%s\n", i, feature[i]));
         }

    }
}
