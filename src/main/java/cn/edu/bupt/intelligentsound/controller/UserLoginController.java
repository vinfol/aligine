package cn.edu.bupt.intelligentsound.controller;

import cn.edu.bupt.intelligentsound.entity.User;
import cn.edu.bupt.intelligentsound.service.UserLoginService;
import com.alibaba.fastjson.JSONObject;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.AssertFalse;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

@Controller
@RequestMapping(value = "/auth")
public class UserLoginController {


    private String url;
    private String allState;

    private static String getRedirect_uri(String s) throws UnknownHostException {
        return s;
    }

    @Autowired
    private UserLoginService userloginservice;

    //授权页面，跳转到用户登录页面
    @RequestMapping(value = "/authorize")
    private String getCode(@RequestParam("redirect_uri") String redirect_uri, @RequestParam("client_id") String client_id, @RequestParam("response_type") String response_type, @RequestParam("state") String state, @RequestParam(value = "scope",required = false) String scope,HttpServletResponse response) throws IOException {
        System.out.println("authorize");
        url = redirect_uri;
        allState = state;
        System.out.println(url);
        if(url.charAt(8)=='o'){
            return "userLogin";
        }else{
            return "index";
        }

    }


    //用户登录
    @RequestMapping(value = {"/userlogin"})
    @ResponseBody
    private void userLogin(@RequestParam("username") String username, @RequestParam("password") String password,HttpServletResponse response,HttpServletRequest request) throws IOException, ServletException {
        System.out.println("userlogin");
        if(password.length()<4){
            RequestDispatcher view = request.getRequestDispatcher("reLogin");
            view.forward(request,response);
        }else{
            password = password.substring(4);
            User user = userloginservice.userLogin(username, password);
            if (user != null) {
                if(url.charAt(8)=='o'){
                    response.sendRedirect(getRedirect_uri(url) + "&state=gantch&code="+password);
                }else{
                    response.sendRedirect(getRedirect_uri(url) + "?code="+password+"&state="+allState);
                }
            } else {
                RequestDispatcher view = request.getRequestDispatcher("reLogin");
                view.forward(request,response);
            }
        }
    }

    //用户重新登录
    @RequestMapping(value = "/reLogin")
    private String reLogin(Map<String, Object> map){
        if(url.charAt(8)=='o'){
            System.out.println("reLogin");
            map.put("msg","用户名或密码错误");
            return "userLogin";
        }else{
            map.put("msg","Please enter a correct phone number and password.");
            return "index";
        }
    }

    //获取token
    @RequestMapping(value = {"/token"}, method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    private JSONObject getTokenByCode(@RequestParam(value = "refresh_token", required = false) String refresh_token, @RequestParam(value = "grant_type",required = false) String grant_type, @RequestParam(value = "client_id",required = false) String client_id, @RequestParam(value = "client_secret",required = false) String client_secret, @RequestParam(value = "code", required = false) String code, @RequestParam(value = "redirect_uri", required = false) String redirect_uri, HttpServletRequest request) throws OAuthSystemException {
        System.out.println("getTokenByCode");
        System.out.println(refresh_token);
        System.out.println(code);
        JSONObject object = new JSONObject();
        if(refresh_token==null){
            object.put("access_token", code);
            object.put("refresh_token", code);
            object.put("expires_in", 17600000);
        }else{
            object.put("access_token", refresh_token);
            object.put("refresh_token", refresh_token);
            object.put("expires_in", 17600000);
        }
        return object;
    }

}
