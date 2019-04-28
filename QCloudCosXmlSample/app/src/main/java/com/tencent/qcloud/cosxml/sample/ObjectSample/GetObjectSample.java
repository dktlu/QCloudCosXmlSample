package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.GetObjectRequest;
import com.tencent.cos.xml.model.object.GetObjectResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/6/7.
 * author bradyxiao
 *
 * Get Object 接口请求可以在 COS 的 Bucket 中将一个文件（Object）下载至本地。该操作需要请求者对目标 Object 具有读权限或目标 Object 对所有人都开放了读权限（公有读）。
 *
 */
public class GetObjectSample {
    GetObjectRequest getObjectRequest;
    QServiceCfg qServiceCfg;

    public GetObjectSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
//        http://a111-1251440030.cos.ap-guangzhou.myqcloud.com/20190428005204-631171-log.zip
        String bucket = "a111-1251440030";
        String cosPath = "20190428005204-631171-log.zip";
        String downloadDir = Environment.getExternalStorageDirectory().getPath() + "/demo_cos_download";

        getObjectRequest = new GetObjectRequest(bucket, cosPath, downloadDir, "fwefewf.zip");

        getObjectRequest.setSign(600,null,null);
        getObjectRequest.setRange(1);
        getObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress = "+progress+" max = "+max);
            }
        });
        try {
            GetObjectResult getObjectResult = qServiceCfg.cosXmlService.getObject(getObjectRequest);
            resultHelper.cosXmlResult = getObjectResult;
            Log.w("XIAO","success");
            return resultHelper;
        } catch (CosXmlClientException e) {
            Log.w("XIAO","QCloudException =" + e.getMessage());
            resultHelper.qCloudException = e;
            return resultHelper;
        } catch (CosXmlServiceException e) {
            Log.w("XIAO","QCloudServiceException =" + e.getMessage());
            resultHelper.qCloudServiceException = e;
            return resultHelper;
        }
    }

    /**
     *
     * 采用异步回调操作
     *
     */
    public void startAsync(final Activity activity){
//        http://a111-1251440030.cos.ap-guangzhou.myqcloud.com/20190428005204-631171-log.zip
        String bucket = "a111-1251440030";
        String cosPath = "20190428005204-631171-log.zip";
        String downloadDir = Environment.getExternalStorageDirectory().getPath() + "/demo_cos_download";

        Log.e("fwef", downloadDir);
        getObjectRequest = new GetObjectRequest(bucket, cosPath, downloadDir, "fwefewf.zip");

        getObjectRequest.setSign(600,null,null);
        getObjectRequest.setRange(1);
        getObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress = "+progress+" max = "+max);
            }
        });
        qServiceCfg.cosXmlService.getObjectAsync(getObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                show(activity, stringBuilder.toString());
            }


            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                }else {
                    stringBuilder.append(qcloudServiceException.getMessage());
                }
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                show(activity, stringBuilder.toString());
            }
        });
    }

    private void show(Activity activity, String message){
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("RESULT", message);
        activity.startActivity(intent);
        if (activity instanceof ProgressActivity) {
            activity.finish();
        }
    }
}
