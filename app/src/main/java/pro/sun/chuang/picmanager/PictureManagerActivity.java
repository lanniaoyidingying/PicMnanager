package pro.sun.chuang.picmanager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureManagerActivity extends AppCompatActivity {
    //https://www.cnblogs.com/plokmju/p/Android_SystemCamera.html

    private static final String TAG = "main";

    private Button btn_PhotoDefault, btn_PhotoExist,btn_PhotoNewdir,btn_DriOK,btn_continue;
    private ImageView iv_CameraImg;
    private EditText etFileName;

    private static String FILE_PATH =Environment.getExternalStorageDirectory() + File.separator;
    private String mFilePath = FILE_PATH + "default";
    private static String mDefaultFilePath = FILE_PATH + "default";
    private Intent intent;
    private File mFile;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noTitle();
        setContentView(R.layout.activity_picture_manager);
        mFilePath = SPUtils.getInstance(this).getString("filePath",FILE_PATH + "default");
        initView();
        checkPermission();
    }

    private void noTitle(){
        if(Build.VERSION.SDK_INT>= 23){
            this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        }else{
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }
    private void initView() {
        btn_PhotoDefault = (Button) findViewById(R.id.btn_PhotoDefault);
        btn_PhotoExist = (Button) findViewById(R.id.btn_PhotoExist);
        btn_PhotoNewdir = (Button)findViewById(R.id.btn_PhotoNewdir);
        btn_DriOK = (Button)findViewById(R.id.btn_DriOK);
        btn_continue = (Button)findViewById(R.id.btn_continue);

        iv_CameraImg = (ImageView) findViewById(R.id.iv_CameraImg);
        iv_CameraImg.setImageResource(R.mipmap.ic_launcher);
        etFileName = (EditText)findViewById(R.id.et_fileName);
        btn_PhotoDefault.setOnClickListener(click);
        btn_PhotoExist.setOnClickListener(click);
        btn_PhotoNewdir.setOnClickListener(click);
        btn_DriOK.setOnClickListener(click);
        btn_continue.setOnClickListener(click);
    }

    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 指定相机拍摄照片保存地址
                case R.id.btn_PhotoDefault:
                    mFilePath = mDefaultFilePath;
                    takePhoto();
                    break;
                // 不指定相机拍摄照片保存地址
                case R.id.btn_PhotoExist:
                    takePhoto();
                    break;
                case R.id.btn_PhotoNewdir:
                    if(etFileName.getVisibility() == View.GONE){
                        etFileName.setVisibility(View.VISIBLE);
                    }
                    if(btn_DriOK.getVisibility() == View.GONE){
                        btn_DriOK.setVisibility(View.VISIBLE);
                    }
                    break;
                    case R.id.btn_DriOK:
                        if(btn_DriOK.getVisibility() ==View.VISIBLE){
                            btn_DriOK.setVisibility(View.GONE);
                        }
                        if(etFileName.getVisibility() ==View.VISIBLE){
                            etFileName.setVisibility(View.GONE);
                        }
                        takePhoto();
                    break;case R.id.btn_continue:
                        if(btn_continue.getVisibility() ==View.VISIBLE){
                            btn_continue.setVisibility(View.GONE);
                        }
                        takePhoto();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
           if(null!=mFile){
               notifyGallery(mUri);
               iv_CameraImg.setImageURI(mUri);
           }else{
               Log.i(TAG, "系统相机拍照完成，resultCode=" + resultCode);
           }
            if(btn_continue.getVisibility()!=View.VISIBLE){
                btn_continue.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                int size = grantResults.length;
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"权限不足不能拍照",Toast.LENGTH_SHORT).show();
                    }
                break;
            default:
                break;
        }
    }

    private void takePhoto() {
            intent = new Intent();
            // 指定开启系统相机的Action
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            // 根据文件地址创建文件
            mFilePath = getFileName();
            mFile= new File(mFilePath);
            // 把文件地址转换成Uri格式
            mUri = Uri.fromFile(mFile);
            // 设置系统相机拍摄照片完成后图片文件的存放地址
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, 0);
    }
    private void makeDirs(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdirs();
        }
    }
    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCameraPermission = ContextCompat.checkSelfPermission(PictureManagerActivity.this, Manifest.permission.CAMERA);
            int checkReadExtraStore = ContextCompat.checkSelfPermission(PictureManagerActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int checkWriteExtraStore = ContextCompat.checkSelfPermission(PictureManagerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(checkCameraPermission != PackageManager.PERMISSION_GRANTED||checkReadExtraStore!=PackageManager.PERMISSION_GRANTED||checkWriteExtraStore!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(PictureManagerActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                return;
            }
        }
    }

    private String getFilePath(){
        String dirs = etFileName.getText().toString().trim();
        if(!TextUtils.isEmpty(dirs)){
          //  mFilePath = FILE_PATH +"default";
        //}else{
            mFilePath = FILE_PATH + dirs;
        }
        Toast.makeText(this,mFilePath,Toast.LENGTH_SHORT).show();
        makeDirs(mFilePath);
        SPUtils.getInstance(this).put("filePath",mFilePath);
        return mFilePath;
    }

    //为照片命名
    private String getFileName(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("'/IMG'_yyyyMMdd_HHmmss'.jpg'");//获取当前时间，进一步转化为字符串
        String str = format.format(date);
        String filePath = getFilePath()+str;
        Log.e(TAG, "getFileName: "+filePath );
        return filePath;
    }

    //通知图库更新保证图片能够在图库中找到
    private void notifyGallery(Uri uri){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
    }
}





















