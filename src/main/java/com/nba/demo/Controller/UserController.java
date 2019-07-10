package com.nba.demo.Controller;

import com.nba.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "*",maxAge = 3600)
public class UserController {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;

    @PostMapping(value = "/loginin")
    public boolean loginin(@RequestParam String username, @RequestParam String password, HttpServletRequest request,HttpServletResponse response){
        return userService.superUserloginin(username,password,request,response);//包含联系电话
    }
    @PostMapping(value = "/signin")
    public Map signin( @RequestParam String username, @RequestParam String password, @RequestParam String tele,@RequestParam String age,@RequestParam String sex,@RequestParam String support_team){
        Map map=new HashMap();

        int res=userService.canSign(username);
        switch (res){
            case -2:map.put("info",-2);break;
            case 1:userService.signin(username,password,tele,age,sex,support_team);map.put("info",1);break;
            default:map.put("info","未知错误");
        }
        return map;//包含身份证,联系电话
    }
    @GetMapping(value = "/getInfo")
    public Map getInfo(HttpServletRequest request,HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        if(user_id!=null){
            return jdbcTemplate.queryForMap("select utel,age,gender,support_team from _user where user_id=\'"+user_id+"\'");
        }
        return null;
    }
    @PostMapping(value = "/updateInfo")
    public Boolean updateTele(@RequestParam String utel,@RequestParam String age,@RequestParam String gender,@RequestParam String support_team, HttpServletRequest request,HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        if(user_id!=null){
            jdbcTemplate.update("update _user set utel=?,age=?,gender=?,support_team=? where user_id =?",utel,age,gender,support_team,user_id);
            return true;
        }
        return false;
    }
    @GetMapping(value = "/upZZANG")
    public int upZZANG(@RequestParam int com_id,HttpServletRequest request,HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        if(user_id==null)
            return -1;
        try {
            return jdbcTemplate.update("insert into ZZANG values (?,?)", user_id, com_id);
        }catch (Exception e){
            e.printStackTrace();
            return 3;
        }
    }

}
