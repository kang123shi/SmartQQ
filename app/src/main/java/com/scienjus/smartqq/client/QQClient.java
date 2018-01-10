package com.scienjus.smartqq.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.constant.ApiURL;
import com.scienjus.smartqq.model.Discuss;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.Font;
import com.scienjus.smartqq.model.Friend;
import com.scienjus.smartqq.model.Group;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.Message;
import com.scienjus.smartqq.model.UserInfo;

import net.dongliu.requests.exception.RequestException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by mags on 2017/5/27.
 */

public class QQClient {
    private String qrsig;
    OkHttpClient okHttpClient = null;
    List<okhttp3.Cookie> cookieAll = null;
    //消息id，这个好像可以随便设置，所以设成全局的
    private static long MESSAGE_ID = 43690001;

    private String vfwebqq;
    private long uin;
    private String psessionid;
    //客户端id，固定的
    private static final long Client_ID = 53999199;
    boolean isLogoutRequest = false;

    private QQClient(){
        okHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<okhttp3.Cookie> cookies) {
                if(cookieAll == null){
                    cookieAll = new ArrayList<okhttp3.Cookie>();
                }
                for(okhttp3.Cookie cookie : cookies){
                    boolean isExist = false;
                    for(int i=0;i<cookieAll.size();i++){
                        okhttp3.Cookie cookieSave = cookieAll.get(i);
                        if(cookieSave.equals(cookie)){
                            cookieAll.set(i, cookie);
                            isExist = true;
                        }
                    }
                    if(!isExist){
                        cookieAll.add(cookie);
                    }
                }

                for(okhttp3.Cookie cookie : cookies){
                    logE(cookie.name()+":"+cookie.value());
                    if (TextUtils.equals(cookie.name(), "qrsig")) {
                        qrsig = cookie.value();
                    }
                    if (TextUtils.equals(cookie.name(), "ptwebqq")) {
                        ptwebqq = cookie.value();
                        logE("ptwebqq::::"+ptwebqq);
                    }
                }

            }

            @Override
            public List<okhttp3.Cookie> loadForRequest(HttpUrl url) {
                if(cookieAll == null){
                    cookieAll = new ArrayList<okhttp3.Cookie>();
                }
                return cookieAll;
            }
        }).build();
    }
    private static QQClient qqClient;
    public synchronized static QQClient getInstance(){
        if(qqClient == null){
            qqClient = new QQClient();
        }
        return qqClient;
    }

    //登录流程2：校验二维码
    public void checkVCode(final CheckErCodeListener listener) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logD("等待扫描二维码");
        logD("第一个参数"+ qrsig);
        Request request = get(ApiURL.VERIFY_QR_CODE, hash33(qrsig));

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                checkVCode(listener);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                //扫描前（未失效）：
                if (result.contains("未失效")) {
                    if(listener != null){
                        listener.checking("二维码状态:未失效");
                    }
                    checkVCode(listener);
                }else if (result.contains("已失效")) {
                    //扫描前（已失效）：
                    if(listener != null){
                        listener.fail(-100, "二维码已失效，请重新登陆获取二维码");
                    }
                }else if (result.contains("认证中")) {
                    //扫描后，认证前：
                    if(listener != null){
                        listener.checking("二维码状态:认证中");
                    }
                    checkVCode(listener);
                }else if (result.contains("成功")) {
                    //认证后：
                    for (String content : result.split("','")) {
                        if (content.startsWith("http")) {
                            logD("正在登录，请稍后");
                            getPtwebqq(content, listener);
                            if(listener != null){
                                listener.checking("正在登录，请稍后");
                            }
                        }
                    }
                }else{
                    checkVCode(listener);
                }
                logE(result);
            }
        });
    }

    public void saveInErCode(Bitmap bitmap){
//        String sdcarePath = Environment.getExternalStorageDirectory().getPath();
//        File f = new File(sdcarePath+"/qqmsg/pic/");
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//        f = new File(sdcarePath + "/qqmsg/pic/",new Date().getTime() + ".jpg");
//        if (f.exists())
//            f.delete();
//        try {
//            FileOutputStream out = new FileOutputStream(f);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //保存到相册
//        try {
//            MediaStore.Images.Media.insertImage(MyApplication.getInstance().getContentResolver(), f.getPath(), "title", "description");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        // 最后通知图库更新
//        MyApplication.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getPath())));
////        callback.invoke("file://"+f.getPath());
//        try {
//            Intent intent = MyApplication.getInstance().getPackageManager().getLaunchIntentForPackage("com.tencent.mobileqq");
//            MyApplication.getInstance().startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//            //若无法正常跳转，在此进行错误处理
////            Toast.makeText( MyApplication.getInstance(), "无法跳转到QQ，请检查您是否安装了QQ！", Toast.LENGTH_SHORT).show();
//        }
    }

    //登录流程1：获取二维码
    public void getQRCode(final Listener listener) {
        logD("开始获取二维码");
        //本地存储二维码图片
        Request request = new Request.Builder()
                .url("https://ssl.ptlogin2.qq.com/ptqrshow?appid=501004106&e=0&l=M&s=5&d=72&v=4&t=0.1")
                .addHeader("User-Agent", ApiURL.USER_AGENT)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取二维码失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try{
                    InputStream is = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    saveInErCode(bitmap);
                    if(listener != null){
                        listener.success(bitmap);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取二维码失败");
                    }
                }
            }
        });
    }

    //鉴权参数
    private String ptwebqq;

    //登录流程3：获取ptwebqq
    public void getPtwebqq(String url, final Listener listener) {
        logD("开始获取ptwebqq");
        Request request = get(ApiURL.GET_PTWEBQQ, url);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "开始获取ptwebqq失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String result = response.body().string();
                logE(result);
                getVfwebqq(listener);
            }
        });
    }

    //登录流程4：获取vfwebqq
    private void getVfwebqq(final Listener listener) {
        logD("开始获取vfwebqq");
        Request request = get(ApiURL.GET_VFWEBQQ, ptwebqq);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "开始获取vfwebqq失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    vfwebqq = getJsonObjectResult(response).getString("vfwebqq");
                    logE("完成流程4"+",vfwebqq:"+vfwebqq);
                    getUinAndPsessionid(listener);
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "开始获取vfwebqq失败");
                    }
                }
            }
        });
    }

    //登录流程5：获取uin和psessionid
    private void getUinAndPsessionid(final Listener listener) {
        logD("开始获取uin和psessionid");

        JSONObject r = new JSONObject();
        r.put("ptwebqq", ptwebqq);
        r.put("clientid", Client_ID);
        r.put("psessionid", "");
        r.put("status", "online");

        Request request = post(ApiURL.GET_UIN_AND_PSESSIONID, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "开始获取uin和psessionid失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    JSONObject obj = getJsonObjectResult(response);
                    psessionid = obj.getString("psessionid");
                    uin = obj.getLongValue("uin");
                    logE("完成流程5"+",psessionid:"+psessionid);
                    logE("完成流程5"+",uin:"+uin);
                    if(listener != null){
                        listener.success("登录成功");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "开始获取uin和psessionid失败");
                    }
                }
            }
        });
    }

    public void getAccount(final Listener listener){
        logD("getAccount");
        Request request = get(ApiURL.GET_ACCOUNT_INFO);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取用户信息失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    if(listener != null){
                        UserInfo userInfo = JSON.parseObject(getJsonObjectResult(response).toJSONString(), UserInfo.class);
                        listener.success(userInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取用户信息失败");
                    }
                }
            }
        });
    }

    //hash加密方法
    private String hash() {
        logE("uin="+uin+","+"ptwebqq="+ptwebqq);
        return hash(uin, ptwebqq);
    }

    //hash加密方法
    private static String hash(long x, String K) {
        int[] N = new int[4];
        for (int T = 0; T < K.length(); T++) {
            N[T % 4] ^= K.charAt(T);
        }
        String[] U = {"EC", "OK"};
        long[] V = new long[4];
        V[0] = x >> 24 & 255 ^ U[0].charAt(0);
        V[1] = x >> 16 & 255 ^ U[0].charAt(1);
        V[2] = x >> 8 & 255 ^ U[1].charAt(0);
        V[3] = x & 255 ^ U[1].charAt(1);

        long[] U1 = new long[8];

        for (int T = 0; T < 8; T++) {
            U1[T] = T % 2 == 0 ? N[T >> 1] : V[T >> 1];
        }

        String[] N1 = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String V1 = "";
        for (long aU1 : U1) {
            V1 += N1[(int) ((aU1 >> 4) & 15)];
            V1 += N1[(int) (aU1 & 15)];
        }
        logE("V1="+V1);
        return V1;
    }

    public void getGroupList(final Listener listener) {
        logD("开始获取群列表");

        JSONObject r = new JSONObject();
        r.put("vfwebqq", vfwebqq);
        r.put("hash", hash());

        Request request = post(ApiURL.GET_GROUP_LIST, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取群列表失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    JSONObject result = getJsonObjectResult(response);
                    List<Group> datas = JSON.parseArray(result.getJSONArray("gnamelist").toJSONString(), Group.class);
                    if(listener != null){
                        listener.success(datas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取群列表失败");
                    }
                }
            }
        });
    }

    public void getDiscussList(final Listener listener) {
        logD("开始获取讨论组列表");

        Request request = get(ApiURL.GET_DISCUSS_LIST, psessionid, vfwebqq);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取获取讨论组失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    List<Discuss> datas = JSON.parseArray(getJsonObjectResult(response).getJSONArray("dnamelist").toJSONString(), Discuss.class);
                    if(listener != null){
                        listener.success(datas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取获取讨论组失败");
                    }
                }
            }
        });
    }

    public void getFriendList(final Listener listener) {
        logD("开始获取好友列表");
        JSONObject r = new JSONObject();
        r.put("vfwebqq", vfwebqq);
        r.put("hash", hash());

        Request request = post(ApiURL.GET_FRIEND_LIST, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取好友列表失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    List<Friend> datas = new ArrayList<>(parseFriendMap(getJsonObjectResult(response)).values());
                    if(listener != null){
                        listener.success(datas);
                        Log.d("QQMessageActivity","获取到好友信息");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取好友列表失败");
                        Log.d("QQMessageActivity","获取好友列表失败"+e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 发送群消息
     *
     * @param groupId 群id
     * @param msg     消息内容
     */
    public void sendMessageToGroup(final long groupId,final String msg) {
        logD("开始发送群消息");

        JSONObject r = new JSONObject();
        r.put("group_uin", groupId);
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))));  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573);
        r.put("clientid", Client_ID);
        r.put("msg_id", MESSAGE_ID++);
        r.put("psessionid", psessionid);

        Request request = post(ApiURL.SEND_MESSAGE_TO_GROUP, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logE("发送信息失败,重试中");
                sendMessageToGroup(groupId, msg);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() != 200) {
                    logE("发送信息失败,重试中");
                    sendMessageToGroup(groupId, msg);
                }
            }
        });
    }

    /**
     * 发送讨论组消息
     *
     * @param discussId 讨论组id
     * @param msg       消息内容
     */
    public void sendMessageToDiscuss(final long discussId,final String msg) {
        logD("开始发送讨论组消息");

        JSONObject r = new JSONObject();
        r.put("did", discussId);
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))));  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573);
        r.put("clientid", Client_ID);
        r.put("msg_id", MESSAGE_ID++);
        r.put("psessionid", psessionid);

        Request request = post(ApiURL.SEND_MESSAGE_TO_DISCUSS, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logE("发送信息失败,重试中");
                sendMessageToGroup(discussId, msg);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() != 200) {
                    logE("发送信息失败,重试中");
                    sendMessageToGroup(discussId, msg);
                }
            }
        });
    }

    /**
     * 发送消息
     *
     * @param friendId 好友id
     * @param msg      消息内容
     */
    public void sendMessageToFriend(final long friendId,final String msg) {
        logD("开始发送消息");

        JSONObject r = new JSONObject();
        r.put("to", friendId);
        r.put("content", JSON.toJSONString(Arrays.asList(msg, Arrays.asList("font", Font.DEFAULT_FONT))));  //注意这里虽然格式是Json，但是实际是String
        r.put("face", 573);
        r.put("clientid", Client_ID);
        r.put("msg_id", MESSAGE_ID++);
        r.put("psessionid", psessionid);

        Request request = post(ApiURL.SEND_MESSAGE_TO_FRIEND, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logE("发送信息失败,重试中");
                sendMessageToGroup(friendId, msg);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() != 200) {
                    logE("发送信息失败,重试中");
                    sendMessageToGroup(friendId, msg);
                }
            }
        });
    }


    public void pollMessage(final MessageCallbackNew callback) {
        logD("开始接收消息");
        JSONObject r = new JSONObject();
        r.put("ptwebqq", ptwebqq);
        r.put("clientid", Client_ID);
        r.put("psessionid", psessionid);
        r.put("key", "");
        Request request = post(ApiURL.POLL_MESSAGE, r);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null)
                callback.fail(-100, "");
                callback.end();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    JSONArray array = getJsonArrayResult(response);
                    for (int i = 0; array != null && i < array.size(); i++) {
                        JSONObject message = array.getJSONObject(i);
                        String type = message.getString("poll_type");
                        if ("message".equals(type)) {
                            callback.onMessage(new Message(message.getJSONObject("value")));
                        } else if ("group_message".equals(type)) {
                            callback.onGroupMessage(new GroupMessage(message.getJSONObject("value")));
                        } else if ("discu_message".equals(type)) {
                            callback.onDiscussMessage(new DiscussMessage(message.getJSONObject("value")));
                        }else{
                            if(callback != null)
                                callback.fail(-100, "");
                        }
                    }
                    callback.end();
                }catch (RequestException e){
                    String msg = e.getMessage();
                    if(!TextUtils.isEmpty(msg) && msg.contains("[103]")){
                        callback.fail(103, msg);
                    }else{
                        callback.fail(-100, "");
                    }
                    callback.end();
                    e.printStackTrace();
                } catch (Exception e) {
                    callback.fail(-100, "");
                    callback.end();
                    e.printStackTrace();
                }
            }
        });
    }

    public void getFriendInfo(long friendId, final Listener listener) {
        logD("开始获取好友信息");
        Request request = get(ApiURL.GET_FRIEND_INFO, friendId, vfwebqq, psessionid);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(listener != null){
                    listener.fail(-100, "获取好友信息失败");
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    UserInfo userInfo = JSON.parseObject(getJsonObjectResult(response).toJSONString(), UserInfo.class);
                    if(listener != null){
                        listener.success(userInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(listener != null){
                        listener.fail(-100, "获取好友信息失败");
                    }
                }
            }
        });
    }

    //将json解析为好友列表
    private static Map<Long, Friend> parseFriendMap(JSONObject result) {
        Map<Long, Friend> friendMap = new HashMap<>();
        JSONArray info = result.getJSONArray("info");
        for (int i = 0; info != null && i < info.size(); i++) {
            JSONObject item = info.getJSONObject(i);
            Friend friend = new Friend();
            friend.setUserId(item.getLongValue("uin"));
            friend.setNickname(item.getString("nick"));
            friendMap.put(friend.getUserId(), friend);
        }
        JSONArray marknames = result.getJSONArray("marknames");
        for (int i = 0; marknames != null && i < marknames.size(); i++) {
            JSONObject item = marknames.getJSONObject(i);
            friendMap.get(item.getLongValue("uin")).setMarkname(item.getString("markname"));
        }
        JSONArray vipinfo = result.getJSONArray("vipinfo");
        for (int i = 0; vipinfo != null && i < vipinfo.size(); i++) {
            JSONObject item = vipinfo.getJSONObject(i);
            Friend friend = friendMap.get(item.getLongValue("u"));
            friend.setVip(item.getIntValue("is_vip") == 1);
            friend.setVipLevel(item.getIntValue("vip_level"));
        }
        return friendMap;
    }

    public void clear(){
        cookieAll = null;
    }

    public void logout(final Listener listener){
        logD("退出");
        cookieAll = null;
        listener.success("");
//        List<Cookie> needRemove = new ArrayList<>();
//        for (Cookie cookieSave:cookieAll) {
//            if(TextUtils.equals(cookieSave.name(),"ptwebqq") || TextUtils.equals(cookieSave.name(),"skey")){
//                needRemove.add(cookieSave);
//            }
//        }
//        for(Cookie cookie: needRemove){
//            cookieAll.remove(cookie);
//        }
//        okhttp3.Cookie.Builder builder = new Cookie.Builder();
//        builder.name("ptwebqq");
//        builder.domain("qq.com");
//        builder.value("");
//        okhttp3.Cookie cookie1 = builder.build();
//
//        okhttp3.Cookie.Builder builder2 = new Cookie.Builder();
//        builder2.name("skey");
//        builder2.domain("qq.com");
//        builder2.value("");
//        okhttp3.Cookie cookie2 = builder2.build();
//        cookieAll.add(cookie1);
//        cookieAll.add(cookie2);
//        Request request = get(ApiURL.LOGOUT);
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//                if(listener != null){
//                    listener.fail(-100, "退出失败");
//                }
//            }
//
//            @Override
//            public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                String result = response.body().string();
//                cookieAll = null;
//                logE("退出"+result);
//                if(listener != null){
//                    listener.success("");
//                }
//            }
//        });
    }

    //获取返回json的result字段（JSONObject类型）
    private static JSONObject getJsonObjectResult(okhttp3.Response response) throws Exception {
        return getResponseJson(response).getJSONObject("result");
    }

    //获取返回json的result字段（JSONArray类型）
    private static JSONArray getJsonArrayResult(okhttp3.Response response) throws Exception {
        return getResponseJson(response).getJSONArray("result");
    }

    //检验Json返回结果
    private static JSONObject getResponseJson(okhttp3.Response response) throws Exception {
        if (response.code() != 200) {
            throw new RequestException(String.format("请求失败，Http返回码[%d]", response.code()));
        }
        String result = response.body().string();
        logE(result);
        JSONObject json = JSON.parseObject(result);
        Integer retCode = json.getInteger("retcode");
        if (retCode == null) {
            throw new RequestException(String.format("请求失败，Api返回异常", retCode));
        } else if (retCode != 0) {
            switch (retCode) {
                case 103: {
                    logE("请求失败，Api返回码[103]。你需要进入http://w.qq.com，检查是否能正常接收消息。如果可以的话点击[设置]->[退出登录]后查看是否恢复正常");
                    throw new RequestException(String.format("请求失败，Api返回码[%d]", retCode));
                }
                case 100100: {
                    logD("请求失败，Api返回码[100100]");
                    throw new RequestException(String.format("请求失败，Api返回码[%d]", retCode));
                }
                default: {
                    throw new RequestException(String.format("请求失败，Api返回码[%d]", retCode));
                }
            }
        }
        return json;
    }

    //发送get请求
    private Request get(ApiURL url, Object... params) {
        Request.Builder builder = new Request.Builder()
                .addHeader("User-Agent", ApiURL.USER_AGENT)
                .url(url.buildUrl(params));
        if (!TextUtils.isEmpty(url.getReferer())) {
            builder.addHeader("Referer", url.getReferer());
        }
        return builder.build();
    }

    //发送post请求
    private Request post(ApiURL url, JSONObject r) {
        RequestBody formBody = new FormBody.Builder().add("r", r.toJSONString()).build();
        Request.Builder builder = new Request.Builder()
                .url(url.getUrl())
                .head()
                .addHeader("User-Agent", ApiURL.USER_AGENT)
                .addHeader("Referer", url.getReferer())
                .addHeader("Origin", url.getOrigin())
                .post(formBody);
        return builder.build();
    }

    //用于生成ptqrtoken的哈希函数
    private static int hash33(String s) {
        int e = 0, n = s.length();
        for (int i = 0; n > i; ++i)
            e += (e << 5) + s.charAt(i);
        return 2147483647 & e;
    }

    public static void logE(String log){
        Log.e("SmartQQClient", log+"");
    }
    public static void logD(String log){
        Log.d("SmartQQClient", log+"");
    }

    public interface Listener{
        void success(Object object);
        void fail(int code, String msg);
    }

    public interface CheckErCodeListener extends Listener{
        void checking(String status);
    }

    public interface MessageCallbackNew extends MessageCallback{
        void fail(int code, String msg);
        void end();
    }
}
