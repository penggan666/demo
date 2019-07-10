package com.nba.demo.Controller;

import com.nba.demo.Service.UserService;
import org.springframework.http.HttpRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.AbstractDocument;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
//实现跨域注解
//origin="*"代表所有域名都可访问
//maxAge飞行前响应的缓存持续时间的最大年龄，简单来说就是Cookie的有效期 单位为秒
//若maxAge是负数,则代表为临时Cookie,不会被持久化,Cookie信息保存在浏览器内存中,浏览器关闭Cookie就消失
//@CrossOrigin(origins = "*",maxAge = 3600)
public class MController {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @GetMapping(value = "/test")
    public void test(@RequestParam String Vteam,String Hteam){
        File f=new File("..");

        try{
            String[] args1=new String[]{"D:\\Development_tools\\Anaconda\\python", "F:\\Course_project\\NBA_Design\\back_py\\crawHuPu\\prediction.py",Vteam,Hteam};
            Process proc=Runtime.getRuntime().exec(args1);
            BufferedReader in=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line=null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @GetMapping(value ="/compare")
    public String compare(@RequestParam String Hteam,@RequestParam String Vteam){
        System.out.println("Vteam:"+Vteam+"-"+"Hteam"+Hteam);
        return jdbcTemplate.queryForObject("select rate from winning_rate where home_team = '"+Hteam+"'and guest_team = '"+Vteam+"'",String.class);

    }
    @GetMapping(value = "/getTeam_data")
    public List getIeam_data(){
        return jdbcTemplate.queryForList("select * from team_data order by score desc ");
    }

    @GetMapping(value = "/getGame_conditon")
    @CrossOrigin
    public Map<String, List> getGame_conditon(){
        Map condition = new HashMap();
        condition.put("east",jdbcTemplate.queryForList("select * from game_conditon where eorw = 'east' order by rank asc"));
        condition.put("west",jdbcTemplate.queryForList("select * from game_conditon where eorw = 'west' order by rank asc")) ;
        return condition;
    }
    @GetMapping(value = "/getPlayerData")
    @CrossOrigin
    public List getPlayerData(@RequestParam String playername){
        if (playername.isEmpty())
            return jdbcTemplate.queryForList("select * from PlayerData order by a  ");
        else
            return jdbcTemplate.queryForList("select * from PlayerData where s like ? order by a",new Object[]{"%"+playername+"%"});
    }
    @GetMapping(value = "/getNews")
    public List getNews(){
        return jdbcTemplate.queryForList("select * from News order by news_date desc");

    }
    private static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String getTimeString(){
        return simpleDateFormat.format(new Date());
    }
    @GetMapping(value = "/upload_advice")
    public boolean upload_advice(@RequestParam String content, HttpServletRequest request,HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        try {
            if(user_id!=null){
                jdbcTemplate.update("insert into user_log(content, user_id, log_date)"+ "values (?,?,?)",content,user_id,getTimeString());
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }
    @GetMapping(value = "/comment")
    public boolean comment(@RequestParam String content,@RequestParam int news_id,HttpServletRequest request,HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        try{
            if(user_id!=null){
                jdbcTemplate.update("insert into _comment(news_id, user_id, content,time) values(?,?,?,?)",news_id,user_id,content, getTimeString());
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return false;

    }
    @GetMapping(value="/logout")
    public void logout(HttpServletRequest request){
        HttpSession session=request.getSession(false);
        if(session!=null){
            session.invalidate();
            System.out.println("session清除成功");
        }
    }

    @GetMapping(value = "/updateTeam_data")
    public String updateTeam_data(){
        try {
            String[] args1=new String[]{"D:\\Development_tools\\Anaconda\\python", "F:\\Course_project\\NBA_Design\\back_py\\crawHuPu\\crawTeam.py"};
            Process proc=Runtime.getRuntime().exec(args1);
            proc.waitFor();
            return "更新成功";
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @GetMapping(value="/player")
    public List<Map<String,Object>> getPlayer(@RequestParam("Names") String name) {
        String sql="select * from Player where team like ?";
        return jdbcTemplate.queryForList(sql,new String[]{"%"+name+"%"});
    }
    @GetMapping(value = "/baseinfor")
    public List<Map<String,Object>> getBase(@RequestParam("Names") String name){
        String sql="select * from team_base where t_name like ?";
        return jdbcTemplate.queryForList(sql,new String[]{"%"+name+"%"});
    }
    @GetMapping(value ="/score")
    public List<Map<String,Object>> getScore(@RequestParam("Names") String name)
    {
        String sql="select * from TeamScore where name like ?";
        return jdbcTemplate.queryForList(sql,new String[]{"%"+name+"%"});
    }

    @GetMapping(value = "/schedule")
    public List<Map<String, Object>> getSchedule()
    {
        String sql="select * from Schedule";
        return jdbcTemplate.queryForList(sql);
    }

//    @GetMapping(value = "/stat/{teamname1}/vs/{teamname2}/table")
//    public List gettable(@PathVariable("teamname1") String teamname1, @PathVariable("teamname2") String teamname2){
//        return jdbcTemplate.queryForList("select gamedate,score,rebound,swish,ftshoot,steal\n" +
//                "from gamehistory\n" +
//                "where (score like '%"+teamname2+"' and score like '"+teamname1+"%') ");
//
//    }
    @GetMapping(value="/stat_table")
    public List gettable(@RequestParam String teamname1, @RequestParam String teamname2){
        return jdbcTemplate.queryForList("select gamedate,score,rebound,swish,ftshoot,steal\n" +
                "from gamehistory\n" +
                "where (score like '%"+teamname2+"' and score like '"+teamname1+"%') ");

    }

    @GetMapping(value = "/vs_eighttimes")
    public Map<String, List<String>> getscore(@RequestParam String teamname1,@RequestParam String teamname2) {
        List temp= jdbcTemplate.queryForList( "select gamedate,homescore,guestscore from gamestat where (guestname = '"+teamname2+"' and homename = '"+teamname1+"') order by cast(gamedate as date) desc limit 8" );
        Map<String,List<String>> datescore = new HashMap<>();
        ArrayList<String> playdate = new ArrayList<>();
        ArrayList<String> homeplayscore = new ArrayList<>();
        ArrayList<String> guestplaysocre = new ArrayList<>();
        for(int i = temp.size() - 1;i>=0;i--) {
            Map maptemp = (Map) temp.get(i);
            String date = String.valueOf(maptemp.get("gamedate"));
            String homescore = String.valueOf(maptemp.get("homescore"));
            String guestscore = String.valueOf(maptemp.get("guestscore"));
            playdate.add(date);
            homeplayscore.add(homescore);
            guestplaysocre.add(guestscore);
        }
        datescore.put("date",playdate);
        datescore.put("home",homeplayscore);
        datescore.put("guest",guestplaysocre);
        System.out.println(datescore);
        return datescore;
    }
    /*
    * request的headers带
        'uid': username,
        'Cookie': "JSESSIONID="+sid
    * */
    @GetMapping(value = "/getComment")
    public List get_comment(@RequestParam String news_id, HttpServletRequest request, HttpServletResponse response){
        String user_id=UserService.getUsernameBySession(request,response);
        List<Map<String,Object>> list=jdbcTemplate.queryForList("select * from _comment where news_id = '"+news_id+"'");
        for(Map<String,Object> e:list){
            int com_id=(int)e.get("com_id");
            Long count=(Long)jdbcTemplate.queryForMap("select Count(*) as c from ZZANG where com_id=?",com_id).get("c");
            Long already;
            if(user_id!=null){
                already=(Long)jdbcTemplate.queryForMap("select COUNT(*) as c from ZZANG where com_id=? AND user_id=?",com_id,user_id).get("c");
            }
            else already=new Long(-1);
            e.put("count",count);
            e.put("already",already);
        }
        return list;
    }
    @GetMapping(value = "/getIDnews/{ID}")
    public Map get_id_news(@PathVariable("ID") String ID){
        return jdbcTemplate.queryForMap("select title, content from news where news_id = '"+ID+"'");
    }
    @GetMapping(value = "/save_comment")
    public String save_comment(@RequestParam String content, @RequestParam String user_id, @RequestParam int news_id){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        jdbcTemplate.update("insert into _comment(news_id, user_id, content,time) " +
                "values(?,?,?,?)",news_id,user_id,content, df.format(new Date()));
        return "成功";
    }
    @GetMapping(value = "/stat_radar") //team1对应甲队，team2对应乙队
    public Map<String,ArrayList<String>> getstat(@RequestParam String team1,@RequestParam String team2){
        List info = jdbcTemplate.queryForList("select teamname,avg(cast(homescore as INTEGER)) avgpoint, avg (cast (rebound as INTEGER)) avgrebound, avg(cast(swish as INTEGER)) avgswish,avg(cast(triswish as INTEGER)) avgtriswish\n" +
                ", avg(cast(steal as INTEGER)) avgsteal , avg(cast(ftshoot as INTEGER)) avgftshoot from gamestat where (guestname = '"+team2+"' and homename = '"+team1+"') or (homename = '"+team2+"' and guestname = '"+team1+"')\n" +
                " group by teamname");
        Map<String,ArrayList<String>> avginfo = new HashMap<>();
        for(int i = 0;i<info.size();i++){
            Map temp = (Map)info.get(i);
            ArrayList<String> statinfo = new ArrayList<>();
            String teamname = String.valueOf(i);
            String avgpoint =  temp.get("avgpoint").toString();
            statinfo.add(avgpoint);
            String avgrebound = temp.get("avgrebound").toString();
            statinfo.add(avgrebound);
            String avgtri = temp.get("avgtriswish").toString();
            statinfo.add(avgtri);
            String avgswish = temp.get("avgswish").toString();
            statinfo.add(avgswish);
            String avgftshoot = temp.get("avgftshoot").toString();
            statinfo.add(avgftshoot);
            String  avgsteal =  temp.get("avgsteal").toString();
            statinfo.add(avgsteal);
            avginfo.put(teamname,statinfo);
        }
        ArrayList<String> teamlist = new ArrayList<>();
        teamlist.add(team1);
        teamlist.add(team2);
        avginfo.put("2",teamlist);
        System.out.println(avginfo);
        return avginfo;
    }



}
