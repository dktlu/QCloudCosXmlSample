package com.tencent.qcloud.cosxml.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.qcloud.cosxml.sample.R;
import com.tencent.qcloud.cosxml.sample.tools.AESUtil;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLDecoder;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    TextView backText;
    TextView contextText;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        backText = (TextView) findViewById(R.id.back);
        contextText = (TextView) findViewById(R.id.content);
        imageView = findViewById(R.id.imageVIew);

        backText.setOnClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            contextText.setText(bundle.getString("RESULT"));
//            Log.e("few", Base64.decode(bundle.getString("RESULT"), Base64.DEFAULT).toString());
//            contextText.setText(new String(Base64.decode(bundle.getString("RESULT").getBytes(), Base64.DEFAULT)));
            String path = Environment.getExternalStorageDirectory().getPath() + "/demo_cos_download/fwefewf.zip";
            String newPath = Environment.getExternalStorageDirectory().getPath() + "/demo_cos_download/fwefewf.png";
            unZip(new File(path), "111", newPath);
//            String pic = "";
//            try {
//                pic = new String(readFile(new File());
//                System.out.print(pic);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.print("stringToBitmap(pic) = " + stringToBitmap(pic) == null);
            Glide.with(this).load(newPath).into(imageView);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.back:
                finish();
                break;
        }
    }

    /**
     * 解压文件
     * File：目标zip文件
     * password：密码，如果没有可以传null
     * path：解压到的目录路径
     */
    public Boolean unZip(File file, String password, String path) {
        Boolean res = false;
        try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.isEncrypted()) {
                if (password != null && !password.isEmpty()) {
                    zipFile.setPassword(password);
                }
            }
            zipFile.extractAll(path);
            res = true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return res;
    }
}
