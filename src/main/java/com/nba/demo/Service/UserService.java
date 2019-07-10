package com.nba.demo.Service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
public class UserService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    //登录
    public boolean superUserloginin(String username, String password, HttpServletRequest request,HttpServletResponse response){
        Map map=jdbcTemplate.queryForMap("select permission from _User where user_id = '"+username+"'and Upw='"+password+"'");
        Integer permission=(Integer)map.get("permission");
        if(permission!=null){
            HttpSession session=request.getSession(true);
            session.setAttribute("uid",username);
            session.setAttribute("permission",permission);
            response.setHeader("sid",session.getId());
            response.setHeader("permission",permission.toString());
            System.out.println(username+":login");
            return true;
        }
        return false;
    }

    public static String getUsernameBySession(HttpServletRequest request,HttpServletResponse response){
        HttpSession session=request.getSession(false);
        String username=request.getHeader("uid");
        if(session!=null&&username!=null){
            if(username.equals((String)session.getAttribute("uid"))){
                session.setMaxInactiveInterval(1800);
                response.setHeader("isAlive","true");
                return username;
            }
        }
        response.setHeader("isAlive","false");
        return null;
    }
    //判断注册信息的可用性
    public int canSign(String username){
        String sql="select count(*) from _User where user_id="+"'"+username+"'";
        int count=jdbcTemplate.queryForObject(sql,Integer.class);
        if(count!=0)return -2;
        return 1;
    }
    //注册
    public void signin(String username,String password,String tele,String age,String sex,String support_team){
        String sql = "insert into _User  values(?,?,?,?,?,?)";
        jdbcTemplate.update(sql,new Object[]{username,password,tele,age,sex,support_team});
    }

}
