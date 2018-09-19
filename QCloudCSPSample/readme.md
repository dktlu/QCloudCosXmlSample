# CSP 使用文档

## 快速接入

### 接入准备

1. SDK 支持 Android 2.2 及以上版本的手机系统；
2. 手机必须要有网络（GPRS、3G 或 WIFI 网络等）；
3. 手机可以没有存储空间，但会使部分功能无法正常工作；
4. 从 [COS 控制台](https://console.cloud.tencent.com/cos4/secret) 获取 APPID、SecretId、SecretKey。

> 关于文章中出现的 SecretId、SecretKey、Bucket 等名称的含义和获取方式请参考：[COS 术语信息](https://cloud.tencent.com/document/product/436/7751)

### 集成 SDK

需要在工程项目中导入下列 jar 包，存放在 libs 文件夹下：

- cos-android-sdk.jar
- qcloud-foundation.jar
- bolts-tasks.jar
- okhttp.jar
- okio.jar

您可以在这里 [COS XML Android SDK-release](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCSPSample/app/libs) 下载所有的 jar 包。

> cos-android-sdk.jar 必须使用 5.4.14 及其以上版本、qcloud-foundation 必须使用 1.5.3 及其以上版本。

### 配置权限

使用该 SDK 需要网络、存储等相关的一些访问权限，可在 AndroidManifest.xml 中增加如下权限声明（Android 5.0 以上还需要动态获取权限）：
```html
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

> 示例 demo 请参考 [QCloudCSPSample](https://github.com/tencentyun/qcloud-sdk-android-samples/tree/master/QCloudCSPSample)

## 快速入门

### 初始化 

在执行任何和 COS 服务相关请求之前，都需要先实例化 CosXmlService 对象，具体可分为如下几步：

#### 初始化配置类

`CosXmlServiceConfig` 是 COS 服务的配置类，您可以使用如下代码来初始化：

```
String appid = "对象存储的服务 APPID";
String region = "存储桶所在的地域";
boolean isHttps = false;

/**
 * 您的服务器对应的主域名，默认为 myqcloud.com，设置后访问地址为：
 * {bucket-name}-{appid}.cos.{cos-region}.{domainSuffix}
 */
String domainSuffix = "your domain suffix"; 

//创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(isHttps)
                .setAppidAndRegion(appid, region) // 如果没有 appid 和 region，请设置为空
                .setDebuggable(true)
                .setDomainSuffix(domainSuffix)  //私有云需要设置主域名，默认为 myqcloud.com
                .builder();
```

#### 初始化授权类

您需要实例化一个COS 服务的授权类，来给请求添加签名来认证您的身份。

##### 通过设置签名字符串进行授权（推荐）

私有云存储暂时不支持用临时密钥进行授权，您必须在服务端计算签名后，返回给客户端使用，

首先您需要实现 `QCloudSigner` 接口

```
public class MyQCloudSigner implements QCloudSigner {

    /**
     * @param request 即为发送到 CSP 服务端的请求，您需要根据这个 HTTP 请求的参数来计算签名，并给其添加 Authorization header
     * @param credentials 空字段，请不要使用
     * @throws QCloudClientException 您可以在处理过程中抛出异常
     */
    @Override
    public void sign(QCloudHttpRequest request, QCloudCredentials credentials) throws QCloudClientException {

        /**
         * 获取计算签名所需字段
         */
        URL url = request.url();
        String method = request.method();
        String host = url.getHost();
        String schema = url.getProtocol();
        String path = url.getPath();
        Map<String, List<String>> headers = request.headers();

        /**
         * 向您自己的服务端请求签名
         */
        String sign = getSignFromYourServer(method, schema, host, path, headers);

        /**
         * 给请求设置 Authorization Header
         */
        request.addHeader("Authorization", sign);
    }

```

然后实例化一个实现了 `QCloudSigner` 接口的对象：

```
QCloudSigner credentialProvider = new MyQCloudSigner();
```
##### 通过永久密钥进行授权

除了通过直接设置签名串来进行授权，您还可以使用永久密钥来初始化授权类，需要指出的是，由于会存在泄漏密钥的风险，我们**强烈不推荐您使用这种方式**，您应该仅仅在安全的环境下临时测试时使用：

```
String secretId = "云 API 密钥 SecretId";
String secretKey ="云 API 密钥 SecretKey";

/**
 * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
 */
QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId,
                secretKey, 300);
```

#### 初始化 COS 服务类

`CosXmlService` 是 COS 服务类，可用来操作各种 COS 服务，当您实例化配置类和授权类后，您可以很方便的实例化一个 COS 服务类，具体代码如下：

````java
CosXmlService cosXmlService = new CosXmlService(context, serviceConfig, credentialProvider);
````

### 上传文件

`UploadService` 是一个通用的上传类，它可以上传不超过 50T 大小的文件，并支持暂停、恢复以及取消上传请求，同时对于超过 2M 的文件会有断点续传功能，我们推荐您使用这种方式来上传文件，更多用法请参考 [通过 UploadService 上传](https://cloud.tencent.com/document/product/436/11238#.3Cspan-id-.3D-.22upload_service.22.3E.E9.80.9A.E8.BF.87-uploadservice-.E4.B8.8A.E4.BC.A0.EF.BC.88.E6.8E.A8.E8.8D.90.EF.BC.89.3C.2Fspan.3E)，上传部分示例代码如下：

```java
UploadService.ResumeData uploadData = new UploadService.ResumeData();
uploadData.bucket = "存储桶名称";
uploadData.cosPath = "[对象键](https://cloud.tencent.com/document/product/436/13324)，即存储到 COS 上的绝对路径"; //格式如 cosPath = "test.txt";
uploadData.srcPath = "本地文件的绝对路径"; // 如 srcPath =Environment.getExternalStorageDirectory().getPath() + "/test.txt";
uploadData.sliceSize = 1024 * 1024; //每个分片的大小
uploadData.uploadId = null; // 若是续传，则uploadId不为空

UploadService uploadService = new UploadService(cosXmlService, uploadData);
uploadService.setProgressListener(new CosXmlProgressListener() {
    @Override
    public void onProgress(long progress, long max) {
        // todo Do something to update progress...
    }
});

/**
 * 开始上传
 */
try {
    CosXmlResult cosXmlResult = uploadService.upload();
} catch (CosXmlClientException e) {
    e.printStackTrace();
} catch (CosXmlServiceException e) {
    e.printStackTrace();
}
```
> 如果您的文件大部分为不超过 20M 的小文件，可以使用 [简单上传接口](https://cloud.tencent.com/document/product/436/11238#.E7.AE.80.E5.8D.95.E4.B8.8A.E4.BC.A0.E6.96.87.E4.BB.B6) 来上传。


### 下载文件

将 COS 上的文件下载到本地。

```java
String bucket = "bucket";
String cosPath = "cosPath";
String savePath = "savePath";

GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, savePath);
getObjectRequest.setProgressListener(new CosXmlProgressListener() {
    @Override
    public void onProgress(long progress, long max) {
        // todo Do something to update progress...
    }
});

//使用同步方法下载
try {
    GetObjectResult getObjectResult = cosXmlService.getObject(getObjectRequest);
} catch (CosXmlClientException e) {
    e.printStackTrace();
} catch (CosXmlServiceException e) {
    e.printStackTrace();
}
```

### 释放客户端

如果不再需要使用 COS 服务，可以调用 `release()` 方法来释放资源:

```java
cosXmlService.release();
```

## 和公有云对比

- 公有云 `domainSuffix` 不可修改，私有云默认和公有云保持一致，为 `myqcloud.com`，但是允许用户自定义；
- 私有云的 appid、region 可以为空；
- 公有云支持临时密钥，私有云不支持；
- 私有云必须使用 cos-android-sdk.jar 5.4.14 及其以上版本，qcloud-foundation 1.5.3 及其以上版本。


> 更多使用接口请参考：[Android SDK 接口文档](https://cloud.tencent.com/document/product/436/11238)