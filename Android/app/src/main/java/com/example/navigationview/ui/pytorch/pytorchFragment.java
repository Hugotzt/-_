package com.example.navigationview.ui.pytorch;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.navigationview.DBUtil;
import com.example.navigationview.R;

import com.linchaolong.android.imagepicker.ImagePicker;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class pytorchFragment extends Fragment {
    Module module = null;
    Bitmap bitmap = null;
    byte[] photobytes;
    String pick = null;
    private ImageView imageView;
    private TextView textView;
    //picture
    private ImagePicker imagePicker = new ImagePicker();


    public static pytorchFragment newInstance() {
        return new pytorchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.pytorch_fragment, container, false);
        // 加载模型
        try {
            // 模型加载函数需要自定义
            module = LiteModuleLoader.load(assetFilePath(getActivity(), "mtest4.0.pt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取控件
        imageView = view.findViewById(R.id.image);
        textView = view.findViewById(R.id.text);
        Button selectImgBtn = view.findViewById(R.id.select_img_btn);
        Button openCamera = view.findViewById(R.id.open_camera);

        // 打开相机
        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("设置图片")
                        .setItems(new String[] { "从相册中选取图片", "拍照" }, new DialogInterface.OnClickListener() {

                            @Override public void onClick(DialogInterface dialog, int which) {
                                // 回调
                                ImagePicker.Callback callback = new ImagePicker.Callback() {
                                    @Override public void onPickImage(Uri imageUri) {
                                    }
                                    @Override public void onCropImage(Uri imageUri) {
                                        // imageUri转bitmap
                                        bitmap = ImageSizeCompress(imageUri);
                                        // 加载图片显示
                                        imageView.setImageBitmap(bitmap);
                                        // 保存图路径
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                                        //savedb
                                        photobytes = stream.toByteArray();

                                        // 将图片转化为张量 输入模型
                                        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                                                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

                                        // 启动模型 开始预测
                                        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

                                        // 将张量内容获取为java浮点数组
                                        final float[] scores = outputTensor.getDataAsFloatArray();

                                        // 搜索得分最大的索引
                                        float maxScore = -Float.MAX_VALUE;
                                        int maxScoreIdx = -1;
                                        for (int i = 0; i < scores.length; i++) {
                                            if (scores[i] > maxScore) {
                                                maxScore = scores[i];
                                                maxScoreIdx = i;
                                            }
                                        }
                                        // 根据所有找到对于物品名
                                        String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];
                                        // 将预测结果展示
                                        TextView textView = view.findViewById(R.id.text);
                                        textView.setText(className);
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                        Date curDate =  new Date(System.currentTimeMillis());
                                        String  txtpublishertime  =  formatter.format(curDate);
                                        final HashMap map=new HashMap<String,Object>();
                                        map.put("place",pick);
                                        map.put("label",className);
                                        map.put("publishertime",txtpublishertime);
                                        map.put("picture",photobytes);
                                        // 开启线程 增加内容
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                DBUtil.insert("insect",map);
                                            }
                                        }).start();
                                        Toast.makeText(getActivity(), "识别成功，可转置记录查看！", Toast.LENGTH_LONG).show();
                                    }
                                };
                                if (which == 0) {
                                    // 从相册中选取图片
                                    imagePicker.startGallery(pytorchFragment.this, callback);
                                    pick = "相册";
                                } else {
                                    // 拍照
                                    imagePicker.startCamera(pytorchFragment.this, callback);
                                    pick = "拍照";
                                }
                            }
                        })
                        .show()
                        .getWindow()
                        .setGravity(Gravity.BOTTOM);
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开实时拍摄识别页面
                // 页面关系设置
                NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                Bundle bundle=new Bundle();
                // 跳转页面
                navController.navigate(R.id.nav_camera,bundle);
            }
        });
        return view;
    }
    private Bitmap ImageSizeCompress(Uri uri){
        InputStream Stream = null;
        InputStream inputStream = null;
        try {
            //根据uri获取图片的流
            inputStream = getContext().getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options的in系列的设置了，injustdecodebouond只解析图片的大小，而不加载到内存中去
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream,null,options);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            // 获取图片的宽高
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            // heightPixels就是要压缩后的图片高度，宽度也一样
            int a = (int) Math.ceil((outHeight/(float)heightPixels));
            int b = (int) Math.ceil(outWidth/(float)widthPixels);
            // 比例计算,一般是图片比较大的情况下进行压缩
            int max = Math.max(a, b);
            if(max > 1){
                options.inSampleSize = max;
            }
            // 解析到内存中去
            options.inJustDecodeBounds = false;
            // 根据uri重新获取流，inputstream在解析中发生改变了
            Stream = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(Stream, null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(Stream != null){
                    Stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  null;
    }
    public static String assetFilePath(Context context, String assetName) throws IOException {
        // 创建file对象
        File file = new File(context.getFilesDir(), assetName);
        // 判断文件是否存在
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
