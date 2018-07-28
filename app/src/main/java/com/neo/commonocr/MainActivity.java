package com.neo.commonocr;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hex.mocr.EngineType;
import com.hex.mocr.FormType;
import com.hex.mocr.HexMOcr;
import com.hex.mocr.ui.bankcard.BankCardInfo;
import com.hex.mocr.ui.idcard.IdCardInfo;
import com.hex.mocr.util.IOUtils;
import com.neo.commonocr.BankCard.BankCardActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String TAG="MainActivity" ;
    private static final int RESULT_FROM_CAMERA=1;
    private static final int RESULT_FROM_IMAGE_LIB=2;
    private static final int RESULT_FROM_BANKCARD=3;
    private static final int RESULT_FROM_IDCARD=4;

    private HexMOcr mOcr;
    private String idCardFilePath;
    private String workImagePath;
    private ImageView imageView;
    private TextView textView;

    protected String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals("mounted");
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return null==sdDir ? "" : sdDir.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    private void init()   {
        //初始化识别引擎
        mOcr=new HexMOcr(this);
        mOcr.loadEngine(EngineType.Engine_IdCard);
        mOcr.loadEngine(EngineType.Engine_BankCard);

        String tmpFile =  this.getSDPath()  + "/AndroidWT/idcard2.jpg";
        mOcr.setResultImgSavePath(EngineType.Engine_IdCard,tmpFile);
        workImagePath=getSDPath() + "/HexMOcrSample";
        IOUtils.makeDirs(workImagePath);
        idCardFilePath= workImagePath + "/idcard.jpg";

        this.initControls();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOcr.unloadAllEngine();
    }

    private Bitmap loadImageFromFile(String filePath){
        FileInputStream inputStream=null;
        try {
            inputStream = new FileInputStream(filePath);
            Bitmap image= BitmapFactory.decodeStream(inputStream);
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            IOUtils.closeQuiet(inputStream);
        }
        return null;
    }

    private void initControls(){
        this.imageView = (ImageView)findViewById(R.id.imgView);
        this.textView = (TextView)findViewById(R.id.txtView);

        Button btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                Uri imageUri = Uri.fromFile(new File(idCardFilePath));
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent, RESULT_FROM_CAMERA);
            }
        });

        Button btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED){

                    //0723增加这部分 外层的动态权限申请，之前就是这里面的代码
                    Intent  intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    try {
                        startActivityForResult(Intent.createChooser(intent, "请选择文件"), RESULT_FROM_IMAGE_LIB);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, perms, 1);
                }
            }
        });


        Button btnFrontCameraCapture=(Button)findViewById(R.id.btnFrontCameraCapture);
        btnFrontCameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] perms = new String[]{Manifest.permission.CAMERA};
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED){
                    //0723增加这部分 外层的动态权限申请，之前就是这里面的代码
                    if(!mOcr.isCameraAvailiable()) {
                        Toast.makeText(MainActivity.this, "当前相机权限未打开或者被其它应用程序占用。请检查！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent=new Intent(MainActivity.this, IdCardActivity.class);
                    intent.putExtra("CardType", FormType.IdCard2_Front.ordinal());

                    startActivityForResult(intent, RESULT_FROM_IDCARD);

                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, perms, 2);
                }
            }
        });

        Button btnBackCameraCapture=(Button)findViewById(R.id.btnBackCameraCapture);
        btnBackCameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mOcr.isCameraAvailiable()) {
                    Toast.makeText(MainActivity.this, "当前相机权限未打开或者被其它应用程序占用。请检查！", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent=new Intent(MainActivity.this, IdCardActivity.class);
                intent.putExtra("CardType", FormType.IdCard2_Back.ordinal());

                startActivityForResult(intent, RESULT_FROM_IDCARD);
            }
        });


        Button btnBankCard = (Button) findViewById(R.id.btnBankCard);
        btnBankCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, BankCardActivity.class);
                startActivityForResult(intent, RESULT_FROM_BANKCARD);
            }
        });
    }


    /**
     * 缩放图片
     * @param bitmap
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    private Bitmap scaleImage(Bitmap bitmap, int maxWidth, int maxHeight){
        int w = maxWidth;
        int h = maxHeight;
        if (bitmap.getHeight()>bitmap.getWidth()){
            w = maxHeight;
            h = maxWidth;
        }

        float sW= (float)w / bitmap.getWidth();
        float sH =(float)h / bitmap.getHeight();

        float scale= Math.max(sW,sH);

        Matrix matrix = new Matrix();   //矩阵，用于图片比例缩放
        matrix.postScale(scale, scale);

        //缩放后的BitMap
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return newBmp;
    }









    private void setIdCardFromFileName(){


        Bitmap bm = BitmapFactory.decodeFile(idCardFilePath);
        imageView.setImageBitmap(bm);//不会变形

        Map<String,Object> ocrRecult=new HashMap<String, Object>();
        int result= mOcr.readFromFile(idCardFilePath, FormType.IdCard2, ocrRecult);
        this.showOcrResult(result, ocrRecult);
    }

    private void setIdCardImage(Bitmap bitmap){
        if (null == bitmap){
            return;
        }

//        if (bitmap.getWidth()>1600 || bitmap.getHeight()>1200){
//            //缩放图片
//            Bitmap newImage= scaleImage(bitmap,1600,1200);
//            bitmap.recycle();
//            bitmap=newImage;
//        }

        imageView.setImageBitmap(bitmap);
        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(idCardFilePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);// 把数据写入文件

            //存储识别结果
            Map<String,Object> ocrRecult=new HashMap<String, Object>();
            int result= mOcr.readFromFile(idCardFilePath, FormType.IdCard2, ocrRecult);
            this.showOcrResult(result, ocrRecult);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuiet(outputStream);
        }
    }


    private void setIdCardImageFromImageLib(Intent data) {
        Uri uri = data.getData();
        Log.v(TAG, uri.toString());
        ContentResolver cr = this.getContentResolver();
        idCardFilePath = getPath(getApplicationContext(), uri);
        setIdCardFromFileName();
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 显示识别结果
     * @param result
     * @param ocrRecult
     */
    private void showOcrResult(int result, Map<String, Object> ocrRecult) {
        StringBuilder text=new StringBuilder();
        text.append("识别结果返回:" + result + "\n");

        for (String key :ocrRecult.keySet()){
            text.append(key).append(" = ").append(ocrRecult.get(key)).append("\n");
        }
        textView.setText(text.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            case RESULT_FROM_CAMERA:
                setIdCardImage(loadImageFromFile(idCardFilePath));

                break;
            case RESULT_FROM_IMAGE_LIB:
                setIdCardImageFromImageLib(data);
                break;
            case RESULT_FROM_BANKCARD:
                showBankCardInfo(data);
                break;
            case RESULT_FROM_IDCARD:
                showIdCardInfo(data);
                break;
            default:
                break;
        }
    }

    private void showBankCardInfo(Intent data) {
        Bundle bundle = data.getExtras();

        BankCardInfo bankCardInfo= (BankCardInfo) bundle.get("ocrResult");
        //获得卡号的图片
        Bitmap resultImage = (Bitmap)bundle.get("image");
        //获得整张银行卡图片
        byte[] imageData = bundle.getByteArray("cardImageData");
        Bitmap cardImage = BitmapFactory.decodeByteArray(imageData,0,imageData.length);


        imageView.setImageBitmap(cardImage);

        StringBuilder text=new StringBuilder("银行卡识别结果:\n");
        text.append("CardNo:").append(bankCardInfo.getCardNo()).append("\n");
        text.append("BankCode:").append(bankCardInfo.getBankCode()).append("\n");
        text.append("BankName:").append(bankCardInfo.getBankName()).append("\n");
        text.append("CardName:").append(bankCardInfo.getCardName()).append("\n");
        text.append("CardType:").append(bankCardInfo.getCardType()).append("\n");

        textView.setText(text.toString());
    }


    private void showIdCardInfo(Intent data) {
        Bundle bundle = data.getExtras();

        IdCardInfo idCardInfo= (IdCardInfo) bundle.get("ocrResult");
        String image = idCardInfo.getCardImage();
        if (null != image && !"".equals(image)){
            imageView.setImageBitmap(this.loadImageFromFile(image));
        }else{
            byte[] imageData=data.getByteArrayExtra("imageData");
            if (null != imageData){
                Bitmap cardImage = BitmapFactory.decodeByteArray(imageData,0,imageData.length);
                imageView.setImageBitmap(cardImage);
            }
        }

        this.showOcrResult(idCardInfo.getFormType().getCode(),idCardInfo.getCardInfo());
    }






    //动态权限申请结果，暂时使用，后面会修改
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0){ //安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED){ //这个是权限拒绝
                            String s = permissions[i];
                            Toast.makeText(this,s+"权限被拒绝了",Toast.LENGTH_SHORT).show();
                        }else{ //授权成功了
                            //do Something


                            Intent  intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            try {
                                startActivityForResult(Intent.createChooser(intent, "请选择文件"), RESULT_FROM_IMAGE_LIB);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
                break;

            case 2:
                if (grantResults.length > 0){ //安全写法，如果小于0，肯定会出错了
                    for (int i = 0; i < grantResults.length; i++) {

                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED){ //这个是权限拒绝
                            String s = permissions[i];
                            Toast.makeText(this,s+"权限被拒绝了",Toast.LENGTH_SHORT).show();
                        }else{ //授权成功了
                            //do Something

                            if(!mOcr.isCameraAvailiable()) {
                                Toast.makeText(MainActivity.this, "当前相机权限未打开或者被其它应用程序占用。请检查！", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent=new Intent(MainActivity.this, IdCardActivity.class);
                            intent.putExtra("CardType", FormType.IdCard2_Front.ordinal());

                            startActivityForResult(intent, RESULT_FROM_IDCARD);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
