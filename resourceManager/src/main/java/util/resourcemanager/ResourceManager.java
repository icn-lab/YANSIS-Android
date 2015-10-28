package util.resourcemanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.res.AssetManager;
import android.util.Log;

/*
 * for Resource Management
 */
public class ResourceManager {
	private String       appDir;
	private String       cacheDir;
	private AssetManager am;
	
	public ResourceManager(){}
	public ResourceManager(String appdir, String cachedir, AssetManager amr){
		appDir   = appdir;
		cacheDir = cachedir;
		am       = amr;
	}
	
	public boolean exists(String filename){
		String filepath = appDir+"/"+filename;
		File file = new File(filepath);
		return file.exists();
	}
		
	public boolean exists(String filename, String zipfile){
		boolean retval = false;
		try{
			InputStream    is  = am.open(zipfile);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry       ze;
		
			while((ze = zis.getNextEntry())!= null){
				if(filename.equals( ze.getName() )){
					retval = true;
					break;
				}
				zis.closeEntry();
			}
			zis.close();
			is.close();
		}catch(Exception e){
			Log.e("ResourceManger",e.toString());
		}
		
		return retval;
	}
	
	public String version(String filename){	
		String infile = appDir+"/"+filename;
		
		return readString(infile);
	}
	
	public String version(String filename, String zipfile){
	    String version="";
	    
		if(extract(filename, zipfile, cacheDir)){
			String infile = cacheDir+"/"+filename;
		
			version = readString(infile);
		}
		
		return version;
	}
	
	public String readString(String filename){
		String string="";
		
		File file = new File(filename);
		try{
			Scanner scn = new Scanner(file, "UTF8");
			string = scn.next();
			scn.close();
		}catch(Exception e){
			Log.e("ResourceManger", e.toString());
		}
		
		return string;
	}
	
	public boolean extract(String zipfile){
		boolean retval = true;
		try{
			InputStream    is  = am.open(zipfile);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry       ze;
			while((ze = zis.getNextEntry()) != null) {
                Log.d("extract", ze.getName());
                if (ze.isDirectory()) {
                    File file = new File(appDir + "/" + ze.getName());
                    file.mkdirs();
                }
				else{
					String outfile = appDir+"/"+ze.getName();
					File file = new File(outfile);
					BufferedInputStream  bis = new BufferedInputStream(zis);
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
					byte[] buffer = new byte[1024];
					int len = 0;
					while((len = bis.read(buffer)) != -1){
						bos.write(buffer, 0, len);
					}
					bos.close();
				}
				zis.closeEntry();
			}
			zis.close();
            is.close();
		}catch(Exception e){
			Log.e("ResourceManger", e.toString());
			retval = false;
		}
		
		return retval;
	}
	
	public boolean extract(String filename, String zipfile, String path){
		boolean retval = false;
		try{
			InputStream    is  = am.open(zipfile);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry       ze;
			while((ze = zis.getNextEntry()) != null){
				if(filename.equals( ze.getName() )){			
					retval = true;
					break;
				}
			}
			if(retval){
				String outfile = path+"/"+filename;
				File newfile = new File(outfile);
				if(!newfile.getParentFile().exists()){
					newfile.getParentFile().mkdirs();
				}
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newfile));
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len=zis.read(buffer)) != -1){
					bos.write(buffer, 0, len);
				}
				bos.close();
				zis.close();
				is.close();
			}
		}catch(Exception e){
			Log.e("ResourceManger", e.toString());
		}
		
		return retval;
	}
	
}