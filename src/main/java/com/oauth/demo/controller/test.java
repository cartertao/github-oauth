package com.oauth.demo.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
@RestController
public class test {
    String client_id = "f56d083408657b536a18";   //客户端id
    String client_secret = "886cba8f71af5666b0fcefe1689149285d8af63e";   //客户端秘钥
    String redirect_uri = "http://47.106.88.217:8080/b";      //服务端跳转携带code授权码的url
    String response_type = "code";    //如果是请求code为code,如果是令牌则为token
    String code= null;  //请求授权码
    String state = "state";
    String codeUrl = "https://github.com/login/oauth/authorize?"; //获取授权码的url
    String tokenUrl = "https://github.com/login/oauth/access_token";    //获取token的url
    String userUrl ="https://api.github.com/user?"; //获取用户信息url
    @RequestMapping("/a")
    public void a(HttpServletRequest request , HttpServletResponse response)throws Exception{
        //拼接授权的url
        String url =codeUrl
                +"response_type="+response_type
                +"&state="+state
                +"&client_id="+client_id
                +"&redirect_uri="+redirect_uri;
        JSONObject json = new JSONObject();
        json.put("url",url);
        PrintWriter out = response.getWriter();
        out.print(json);
    }

    //获取授权码后，服务端回调url
    @RequestMapping("/b")
    public String b(HttpServletRequest request , HttpServletResponse response)throws Exception{
        //获取授权码
        code = request.getParameter("code");
        //设置发送post请求token的url
        String getTokenUrl = tokenUrl;
        //设置请求参数 name1=value1&name2=value2
        String param = "client_id="+client_id
                +"&client_secret="+client_secret
                +"&code="+code
                +"&redirect_uri="+redirect_uri
                +"&state="+state;
        //获取服务返回的token
        String jsonStr = this.post(getTokenUrl,param);
        JSONObject json = JSON.parseObject(jsonStr);
        String access_token = json.get("access_token").toString();

        //通过token获取用户信息
        String getUserUrl = userUrl + "access_token=" + access_token;
        String userInfo = this.get(getUserUrl,null);
        return "a?return="+userInfo;
    }

    public String post(String urls ,String param)throws Exception{
        //创建url对象
        URL url = new URL(urls);
        //打开一个链接
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        //设置属性
        connection.setRequestProperty("accept","application/json");
        connection.setRequestProperty("connection", "Keep-Alive");
        //post需要设置
        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setConnectTimeout(15000);//链接超时
        connection.setReadTimeout(60000);//读取远程数据超时
        //获取输出流
        PrintWriter  out = new PrintWriter(connection.getOutputStream());
        //发送请求参数  name1=value1&name2=value2
        out.print(param);
        out.flush();
        String result = null;
        if(connection.getResponseCode() == 200) {
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sbf = new StringBuffer();
            String re = null;
            while ((re = br.readLine()) != null) {
                sbf.append(re);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        }
        return result;
    }

    public String get(String urls ,String param)throws Exception{
        URL url = new URL(urls);
        //打开一个链接
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);//链接超时
        connection.setReadTimeout(60000);//读取远程数据超时
        //发送请求
        connection.connect();
        String result = null;
        if(connection.getResponseCode() == 200){
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuffer sbf = new StringBuffer();
            String re = null;
            while ((re = br.readLine()) != null){
                sbf.append(re);
                sbf.append("\r\n");
            }
            result = sbf.toString();
        }
        return result;
    }
}