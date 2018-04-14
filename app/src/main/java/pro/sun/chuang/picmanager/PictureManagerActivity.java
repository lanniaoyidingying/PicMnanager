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
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureManagerActivity extends AppCompatActivity {
    //https://www.cnblogs.com/plokmju/p/Android_SystemCamera.html
    //Android保存图片到系统图库  http://stormzhang.com/android/2014/07/24/android-save-image-to-gallery/Android保存图片到系统图库
    //https://www.cnblogs.com/kingwild/articles/5422329.html
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
    private boolean mIsdefault = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noTitle();
        setContentView(R.layout.activity_picture_manager);
        mFilePath = SPUtils.getInstance(this).getString("filePath",FILE_PATH + "default");
        initView();
        checkPermission();
    }

    //不显示标题栏
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

    //选择拍照的模式
    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 指定相机拍摄照片保存地址
                case R.id.btn_PhotoDefault:
                    mIsdefault = true;
                    viewGone(btn_DriOK);
                    viewGone(etFileName);
                    takePhoto();
                    break;
                // 不指定相机拍摄照片保存地址
                case R.id.btn_PhotoExist:
                    mIsdefault = false;
                    viewGone(btn_DriOK);
                    viewGone(etFileName);
                    takePhoto();
                    break;
                case R.id.btn_PhotoNewdir:
                    mIsdefault = false;
                    showView(btn_DriOK);
                    showView(etFileName);
                    break;
                case R.id.btn_DriOK:
                    if (TextUtils.isEmpty(etFileName.getText().toString().trim())){
                        Toast.makeText(PictureManagerActivity.this,"亲,你还没有文件夹名哦",Toast.LENGTH_SHORT).show();
                    }else{
                        viewGone(btn_DriOK);
                        viewGone(etFileName);
                        takePhoto();
                    }
                    break;
                    case R.id.btn_continue:
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
    private void viewGone(View view){
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }
    private void showView(View view){
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
           if(null!=mFile){
               /*try {
                   MediaStore.Images.Media.insertImage(this.getContentResolver(),
                           mFile.getAbsolutePath(), mFilePath, null);
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               }*/
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

    //权限检查返回结果
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

    //进行拍照
    private void takePhoto() {
            intent = new Intent();
            // 指定开启系统相机的Action
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            // 根据文件地址创建文件
            mFilePath = getFileName();
            mFile= new File(mFilePath);
            // 把文件地址转换成Uri格式

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mUri = FileProvider.getUriForFile(this, "pro.sun.chuang.picmanager.fileprovider", mFile);
            //添加权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
       } else{
            mUri = Uri.fromFile(mFile);
       }
            // 设置系统相机拍摄照片完成后图片文件的存放地址
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, 0);
    }

    //对不存在的文件夹进行新建
    private void makeDirs(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdir();
        }
    }
    //检查apk是否拥有相关权限
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

    //获取将要保存照片的文件夹路径
    private String getFilePath(){
        String dirs = etFileName.getText().toString().trim();
        if(mIsdefault){//默认文件夹
            mFilePath = mDefaultFilePath;
        }else if(!TextUtils.isEmpty(dirs)){//自定义文件夹
            mFilePath = FILE_PATH + dirs;
        } else{//已经存在的文价夹
          mFilePath = SPUtils.getInstance(this).getString("filePath");
        }
        Toast.makeText(this,mFilePath,Toast.LENGTH_SHORT).show();
        File pFile = new File(mFilePath);
        if(!pFile.exists()){
            makeDirs(mFilePath);
        }
        SPUtils.getInstance(this).put("filePath",mFilePath);
        return mFilePath;
    }

    //此为照片的名字
    private String getFileName(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("'/IMG'_yyyyMMdd_HHmmss'.jpg'");//获取当前时间，进一步转化为字符串
        String str = format.format(date);
        String fileName = getFilePath()+str;
        Log.e(TAG, "getFileName: "+fileName );
        return fileName;
    }

    //通知图库更新保证图片能够在图库中找到
    private void notifyGallery(Uri uri){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
    }
}





















