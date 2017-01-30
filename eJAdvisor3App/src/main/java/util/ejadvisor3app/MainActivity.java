package util.ejadvisor3app;

import java.util.*;
import util.ejadvisor3.EJAdvisor3;
import util.ejadvisor3.EJExample;
import util.ejadvisor3.WordProperty;
import util.resourcemanager.ResourceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.hanasu.Hanasu;

public class MainActivity extends FragmentActivity {
	private EJAdvisor3 ejadv3;
	private String author="Akinori Ito";
	private String version="20170106";
	private String senVersion="";
	private String morphVersion="";
	private String htsvoiceVersion="";
	private String colorEasy      = "#000000";
	private String colorDifficult = "#ff00ff";
	private String colorOutOfList = "#ff0000";
	private ProgressDialog progressDialog;
	private Thread thread;
	private Handler handler;
	private final float defaultFontSize = (float)20.0; // sp
	private final int baseHeightPixels = 728;
	private float fontSize;
	private float scale;
	private final int MIN_SPEECH_RATE = 300;
	private final int MAX_SPEECH_RATE = 600;
	private final int DEFAULT_SPEECH_RATE = 360;
	private int speechRate = DEFAULT_SPEECH_RATE;
	private Boolean analysisDone = false;
	private final String htsVoice = "/htsvoice/tohoku-f01-neutral.htsvoice";
	private Hanasu hanasu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// イベントリスナの設定
		EditText et = (EditText)findViewById(R.id.editTextInputSentence);
		et.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before, int count){
				setAnalysisDone(false);
			}
			public void beforeTextChanged(CharSequence s, int start, int count ,int after){
			}
			public void afterTextChanged(Editable s){
			}
		});
		setAnalysisDone(false);
	    /*
		calcFontSize();
		setFontSize();
		Log.d("init", "fontSize"+Float.toString(fontSize));
		Log.d("init", "scale"+Float.toString(scale));
		*/
		// 進捗ダイアログを出すためのしかけ
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
				case 1:
					break;
				case 0:
					progressDialog.dismiss();
					break;
				}
			}
		};
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("起動処理");
		progressDialog.setMessage("辞書を読み込んでいます");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.show();
		
		// このスレッドが終了した時に，ダイアログが消える
		thread = new Thread(new Runnable(){
			public void run(){
				init();
				handler.sendEmptyMessage(0);
			}
		});
		
		thread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "話速").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, 1, 1, "About").setIcon(android.R.drawable.ic_menu_info_details);
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
			case 0:
				speechRateSettings();
				return true;
			case 1:
				aboutDialog();
				return true;
		}
		return false;
		/*
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
		*/
	}
	
	// アプリケーションの初期設定
    protected void init(){
		String baseDir = getFilesDir().toString();
    	prepareResources();
    	ejadv3 = new EJAdvisor3(baseDir);
		hanasu = new Hanasu(baseDir+htsVoice);
    }
    
    private void calcFontSize(){
    	final DisplayMetrics displayMetrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    	Log.d("DisplayMetrics", "height"+Integer.toString(displayMetrics.heightPixels));
    	scale = (float)displayMetrics.heightPixels / baseHeightPixels;
    	fontSize = defaultFontSize * scale;
    }
    
    private void setFontSize(){
    	EditText edittext = (EditText)findViewById(R.id.editTextInputSentence);
    	edittext.setTextSize(fontSize);
    	Button button = (Button)findViewById(R.id.btnAnalysis);
    	button.setTextSize(fontSize);
    }
    
    // 必要であれば辞書などを端末内部に展開する
    protected String checkResources(String zipFile, String versionText){
        ResourceManager resourcemanager = new ResourceManager(getFilesDir().toString(), getCacheDir().toString(), getResources().getAssets());

        Log.d("ResourceManager", "instance");
        if(resourcemanager.exists(versionText)){
        	String existVersion = resourcemanager.version(versionText);
            Log.d("ResourceManager", versionText+" exist:"+existVersion);
        	String zipVersion   = resourcemanager.version(versionText, zipFile);
        	Log.d("ResourceManager", versionText+" zip:"+zipVersion);
        	if(zipVersion.compareTo(existVersion) > 0){
        		resourcemanager.extract(zipFile);
        	}
        }
        else{
        	resourcemanager.extract(zipFile);
        }

        return resourcemanager.version(versionText);
    }
    
    // 必要なファイルが端末内になければ，展開する
    protected void prepareResources(){
    	morphVersion = checkResources("morph.zip", "morph/version.txt");
        senVersion   = checkResources("sen.zip", "sen/version.txt");
		htsvoiceVersion = checkResources("htsvoice.zip", "htsvoice/version.txt");
    }
    
    // 分析ボタンを押したときの動作
    protected void btnEval_onClick(View view){
    	EditText edittext = (EditText)findViewById(R.id.editTextInputSentence);
    	
    	WordProperty[][] analysisResult = ejadv3.doAnalysis(edittext.getText().toString());
		setAnalysisDone(true);

    	showAnalysisResult(analysisResult);
    }

	protected void setAnalysisDone(Boolean b){
		analysisDone = b;
		set_btnSynthesisState();
	}

	protected void set_btnSynthesisState(){
		Button bt = (Button)findViewById(R.id.btnSynthesis);
		bt.setEnabled(analysisDone);
	}

	//合成ボタンを押したときの動作
	protected void btnSyntheis_onClick(View view){
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch(msg.what){
					case 1:
						break;
					case 0:
						progressDialog.dismiss();
						break;
				}
			}
		};
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("音声合成");
		progressDialog.setMessage("処理しています");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.show();

		// このスレッドが終了した時に，ダイアログが消える
		thread = new Thread(new Runnable(){
			public void run(){
				hanasu.setTokens(ejadv3.getTokens());
				hanasu.doSynthesize(speechRate);
				handler.sendEmptyMessage(0);
			}
		});

		thread.start();


	}

    // 分析結果を表示する（文ごと）
    protected void showAnalysisResult(final WordProperty[][] analysisResult){	
    	ArrayList<CharSequence> htmlSentence = new ArrayList<CharSequence>();
    	for(int curSent=0;curSent < analysisResult.length;curSent++){
    		WordProperty[] w = analysisResult[curSent];
    		String curSentStr = String.format("(%d)", curSent+1);
    		for(int wPos=0;wPos < w.length;wPos++){
    			String color = wordColorFromWordProperty(w[wPos]);
   				curSentStr += String.format("<font color='%s'>%s</font>", color, w[wPos].toString());
    		}
         	//Toast.makeText(this, curSentStr.toString(), Toast.LENGTH_SHORT).show();
    		htmlSentence.add(Html.fromHtml(curSentStr)); 
    		
    	}
    	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, htmlSentence);
    	/*{
    		public View getView(int position, View convertView, ViewGroup parent){
    			TextView textview = (TextView)super.getView(position, convertView, parent);
    			// textview.setTextSize(fontSize);
    			return textview;
    		}
    	};*/
    	ListView listView = (ListView)findViewById(R.id.listViewAnalysisResult);
    	listView.setAdapter(adapter);
    	listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
    		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
    			showWordProperty(analysisResult[position]);
                showEvaluationPoint(analysisResult[position]);
    		}
    	});
    }

    protected String[] splitToBunsetsu(final WordProperty[] w){
        List<String> bunsetsu = new ArrayList<String>();
        String tmpStr = "";

        for (int i = 0; i < w.length; i++) {
            // 文節間に空白を入れる
            if (i > 0 && w[i].is_content_word()) {
                //自立語の場合
                //「する」に対して、直前が名詞-サ変接続の場合には自立語でない
                if (w[i].getBasicString().equals("する") &&
                        w[i - 1].getPOS().equals("名詞-サ変接続")) {
				}
				else if(w[i].getPOS().equals("名詞-数") &&
						w[i - 1].getPOS().equals("名詞-数")) {
				}
				else{
                    bunsetsu.add(tmpStr);
                    tmpStr = "";
                }
            }
            String colorString;
            if(w[i].is_easy()){
                colorString = colorEasy;
            }
            else if(w[i].is_difficult()){
                colorString = colorDifficult;
            }
            else{
                colorString = colorOutOfList;
            }

            tmpStr += "<font color='"+colorString+"'>"+w[i].toString()+"</font>";
        }
        if(!tmpStr.equals(""))
            bunsetsu.add(tmpStr);

        String[] retStr = new String[bunsetsu.size()];
        for(int i=0;i < retStr.length;i++){
            retStr[i] = bunsetsu.get(i);
        }
        return retStr;
    }

    protected void showEvaluationPoint(final WordProperty[] w){
        Boolean wordflag = true;
        String evaluationPoints = "";

        //文の区切り
        String[] bunsetsu = splitToBunsetsu(w);
        String bStr = bunsetsu[0];
        for(int i=1;i < bunsetsu.length;i++){
            bStr += "&nbsp;&nbsp;"+bunsetsu[i];
        }
        //bStr += "<br/>";
        evaluationPoints += bStr;
        double score = ejadv3.estimateScore(w);
        String scoreString = String.format("%.2f", score);
        evaluationPoints += "(score:"+scoreString+")<br/>";

        for (int i = 0; i < w.length; i++) {
            // 単語が簡単かどうかのアドバイス
            if(w[i].is_easy()){
                continue;
            }
            if (w[i].is_difficult()) {
                wordflag = false;
                evaluationPoints += "<font color='"+colorDifficult+"'>" + w[i].toString() +
                        "</font>: 難しい単語です。可能なら簡単な単語に置き換えましょう。<br/>";
            } else {
                wordflag = false;
                evaluationPoints += "<font color='"+colorOutOfList+"'>" + w[i].toString() +
                        "</font>: ほとんど理解してもらえません。可能なら簡単な単語に置き換えてください。<br/>";
            }
        }

        if(wordflag == true){
            evaluationPoints += "難しい単語はありませんでした。<br/>";
        }

        String advice[] = ejadv3.getRecommendations(w);
        for (int i = 0; i < advice.length; i++) {
            evaluationPoints += advice[i] + "<br/>";
        }

        CharSequence cs = Html.fromHtml(evaluationPoints);
        TextView textView = (TextView)findViewById(R.id.evaluationPointText);
        textView.setText(cs);
    }

    // 1文の単語ごとの情報（読み，形態素情報，級など）を表示する
    protected void showWordProperty(final WordProperty [] w){
    	LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutWordProperty);
    	linearLayout.removeAllViews();
  
    	for(int i=0;i < w.length;i++){
    		final int s = i;
    		TextView text = new TextView(this);
    		
    		text.setSingleLine(false);
    		text.setText(WordProperty2Html(w[i]));
    		//text.setTextSize(fontSize);
    		text.setClickable(true);
    		text.setOnClickListener(new View.OnClickListener(){
    			public void onClick(View view){
    				showExampleSentence(w[s]);
    	//			Toast.makeText(getApplicationContext(), w[s].toString(), Toast.LENGTH_SHORT).show();
    			}
    		});
    		linearLayout.addView(text);
    	}
    }
    
    // 単語 w に関連した例文を表示する
    protected void showExampleSentence(WordProperty w){
    	EJExample[] res = ejadv3.exampleSentence(w);
//    	LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearLayoutExampleSentence);
 //   	linearLayout.removeAllViews();
// TextView text = new TextView(this);
    	//text.setTextSize(fontSize);
        TextView text = (TextView)findViewById(R.id.exampleSentence);
		text.setSingleLine(false);
//		text.setMaxLines(3);
		text.setMovementMethod(ScrollingMovementMethod.getInstance());


		if(res == null || res[0] == null) {
	        String str = "<<該当なし>>";
	        text.setText(str);
        }
		else{
			String str="";
			for (int i = 0; i < res.length; i++) {
				str += String.format("%s<br>", res[i].EJ());
			}
			str+="<br>";
			text.setText(Html.fromHtml(str));
		}
		//linearLayout.addView(text);
    }
 
    // 単語 w の情報をHTML形式でレンダリングしたものを返す
    protected CharSequence WordProperty2Html(WordProperty w){
    	String color = wordColorFromWordProperty(w);
    	String str = String.format("<h4><font color='%s'>%s</font></h4><br>", color, w.toString());
    	str += String.format("原形:  %s<br>", w.getBasicString());
    	str += String.format("品詞: %s<br>", w.getPOS());
    	str += String.format("活用形: %s<br>", w.getCform());
    	str += String.format("読み:  %s<br>", w.getPronunciation());
    	str += String.format("級:    <font color='%s'>%d</font><br>", color, w.getGrade()); 
    	
    	return Html.fromHtml(str);
    }
    
    // 単語の級に応じて色を返す
    protected String wordColorFromWordProperty(WordProperty w){
    	String color;
    	if(w.is_easy()){
			color = colorEasy;
		}
		else if(w.is_difficult()){
			color = colorDifficult;
		}
		else{
			color = colorOutOfList;
		}
    	
    	return color;
    }

	protected int speechRateToSeekBarProgress(int sr){
		int vRange = MAX_SPEECH_RATE - MIN_SPEECH_RATE;
		double retVal = (sr - MIN_SPEECH_RATE) * 100.0 / vRange;

		return (int)retVal;
	}

	protected int getSpeechRateFromSeekBar(int sbValue){
		int vRange = MAX_SPEECH_RATE - MIN_SPEECH_RATE;
		double retVal = vRange * sbValue / 100.0 + MIN_SPEECH_RATE;

		return (int)retVal;
	}

    protected void speechRateSettings(){
		SpeechRateDialogFragment srdf = new SpeechRateDialogFragment();

		srdf.show(getSupportFragmentManager(), "Speech Rate");
	}

    protected void aboutDialog(){
    	String message = 
    			String.format("<h3>Author:%s</h3><br>version:%s<br>sendic version:%s<br>morph version:%s<br>"
    					, author, version, senVersion, morphVersion);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
        .setTitle("About")
        .setMessage(Html.fromHtml(message))
        .setPositiveButton("OK", null)
        .setIcon(android.R.drawable.ic_menu_info_details);

        builder.show();
    }

	public class SpeechRateDialogFragment extends DialogFragment {
		public Dialog onCreateDialog(Bundle savedInstanceState){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("話速");
			LayoutInflater inflater = getActivity().getLayoutInflater();
			final View view = inflater.inflate(R.layout.dialog_speechrate,null);

			builder.setView(view)
					.setPositiveButton("OK", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int id){
							//
						}
					});


			SeekBar sb = (SeekBar)view.findViewById(R.id.seekbar_speechrate);
			sb.setProgress(speechRateToSeekBarProgress(speechRate));
			TextView tv = (TextView)view.findViewById(R.id.text_speechrate);
			tv.setText(String.format("%3d[モーラ/分]", speechRate));

			sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				public void onStopTrackingTouch(SeekBar seekBar){
				}
				public void onStartTrackingTouch(SeekBar seekBar){

				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
					speechRate = getSpeechRateFromSeekBar(progress);
					TextView tv = (TextView)view.findViewById(R.id.text_speechrate);
					tv.setText(String.format("%3d[モーラ/分]", speechRate));
					/*
					int pv = speechRateToSeekBarProgress(speechRate);
					Log.d("d","progress:"+progress);
					Log.d("d","speechRate:"+speechRate);
					Log.d("d","pv:"+pv);
					*/
				}
			});
			return builder.create();
		}
	}
}

