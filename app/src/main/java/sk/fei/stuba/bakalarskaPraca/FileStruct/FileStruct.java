package sk.fei.stuba.bakalarskaPraca.FileStruct;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileStruct {
    private String fileAbsolutPath;
    private File file;
    private Uri fileUri;

    //create file//
    public FileStruct(String packageName, String packageChildName,String fileName, String fileExt){
        new FileStruct(packageName,packageChildName);
        this.file = new File(Environment.getExternalStorageDirectory()+"/"+packageName+"/"+packageChildName+"/",fileName+fileExt);
        this.fileUri = Uri.fromFile(this.file);
        this.fileAbsolutPath = this.file.getAbsolutePath();
    }


    public FileStruct(String fileFromPath){
        this.fileAbsolutPath = fileFromPath;
        this.file = new File(this.fileAbsolutPath);
        this.fileUri = Uri.fromFile(this.file);
    }

    //create folder//
    public FileStruct(String packageName, String packageChildName){
        this.file = new File(Environment.getExternalStorageDirectory()+"/"+packageName+"/",packageChildName);
        createFolder();
        this.fileUri = Uri.fromFile(this.file);
        this.fileAbsolutPath = this.file.getAbsolutePath();
    }
    public String getFileAbsolutPath() {
        return fileAbsolutPath;
    }

    public Uri getUriFile() {
        return fileUri;
    }

    public File getFile(){
        return this.file;
    }

    static public String createFileName() throws IOException {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "VIDEO_" + timestamp + "_";
    }

    public void createFolder(){
        this.file.mkdirs();

    }

    public void deleteDirectory(){
        this.file.delete();
    }
}
