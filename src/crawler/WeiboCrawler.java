/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler;

import dbutil.MySQLConnManager;
import dbutil.MySQLDBHelper;
import model.UserModel;
import model.WeiboModel;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 利用WebCollector和获取的cookie爬取新浪微博并抽取数据
 * @author 
 */
public class WeiboCrawler extends BreadthCrawler {
	static Connection connection;
	
    String cookie;
    int cookieId = 0;
    int cookieCurrNum=0; 
    public static ArrayList<String> cookies = new ArrayList<String>();
    
    int proxyId = 0;
    int proxyCurrNum=0; 
    int proxyCircle=0; 
    public static ArrayList<String> hostname = new ArrayList<String>();
    public static ArrayList<Integer> port = new ArrayList<Integer>();
    
    public static void loadProxy() {
    	BufferedReader read=null;    	    
    	try {
    		URL realurl=new URL("http://127.0.0.1:8000/?types=0&count=200&country=%E5%9B%BD%E5%86%85");
    		URLConnection connection=realurl.openConnection();
	        connection.setRequestProperty("accept", "*/*");
	        connection.setRequestProperty("connection", "Keep-Alive");
	        connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
	        connection.connect();
	        read = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
	        String line;//循环读取
	        while ((line = read.readLine()) != null) {
	        	String[] str = line.split("],");
	        	for (int i = 0; i < str.length; i++) {
	        		String[] temp = str[i].split(",");
					if(temp.length ==3){
						hostname.add(temp[0].substring(temp[0].indexOf("\"")+1, temp[0].indexOf("\"",5)));
						port.add(Integer.parseInt(temp[1].trim()));
					}
				}
	        }
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}finally{
    	    if(read!=null){//关闭流
	    	    try {
	    	    	read.close();
	    	    } catch (IOException e) {
	    	    	e.printStackTrace();
	    	    }
		    }
		}    	

	}
    
    public static void loadCookies(){
   	 try {
	        FileReader reader = new FileReader("./resource/crawler/sinacookies.txt");
	        BufferedReader br = new BufferedReader(reader);	       
	        String str = null;	       
	        while((str = br.readLine()) != null) {	             
				cookies.add(str);
	        }	       
	        br.close();
	        reader.close();
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
   }
    
    public WeiboCrawler(String crawlPath, boolean autoParse) throws Exception {
        super(crawlPath, autoParse);  
        cookie = cookies.get(cookieId);
    }

    @SuppressWarnings("deprecation")
	@Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
    	cookieCurrNum ++;
    	proxyCurrNum ++;
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setCookie(cookie);        
        if(proxyCurrNum == 5 && proxyCircle < 5){
        	proxyCurrNum = 0;
        	if(++proxyId < hostname.size()){
        		request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId),port.get(proxyId))));  
			}else{				
				proxyId = 0;
				proxyCircle ++;
				request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId),port.get(proxyId)))); 
			}
        }else if(proxyCurrNum == 5 && proxyCircle >= 5){
        	loadProxy();
			proxyId = 0;
			proxyCircle = 0;
			request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId),port.get(proxyId))));
        } 
        if(cookieCurrNum == 20){
        	cookieCurrNum=0;
            if(++cookieId < cookies.size()){
    			cookie = cookies.get(cookieId);
    		}else{
    			cookieId = 0;
    			cookie = cookies.get(cookieId);
    		}        	
        }
        boolean proxySucced = false;
        do {
	        try {
	        	request.getResponse();
	        	proxySucced = true;
			} catch (Exception e) {
				// TODO: handle exception
				if (e.toString().contains("connect timed out") || 
					e.toString().contains("Unexpected end of file from server") || 
					e.toString().contains("Server returned HTTP response code") ||
					e.toString().contains("Read timed out")) {
					if(++proxyId < hostname.size()){
						request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId),port.get(proxyId))));
					}else{
						proxyId = 0;
						proxyCircle ++;
						request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId),port.get(proxyId))));
					}
				}
				else{
					if(++cookieId < cookies.size()){
						cookie = cookies.get(cookieId);
					}else{
						cookieId = 0;
						cookie = cookies.get(cookieId);
					}
					request.setCookie(cookie);
				}
			}
        } while (!proxySucced);
        return request.getResponse();
    }
    
	@SuppressWarnings("deprecation")
	@Override
    public void visit(Page page, CrawlDatums next) { 
		String userID = page.meta("userID");    	
        String url = page.url();
        String type=page.meta("type");
        System.out.println("-->url:"+url);
        switch (type) {
		case "id":{
			/*爬取关注者*/
	        if (url.contains("follow")) {
	        	Elements follows = page.select("table");
		        for (Element follow : follows) {
		        	
		        	String followeduserid=follow.select("table > tbody > tr > td").get(1).select("a[href^=https://weibo.cn/attention]").attr("href")
		        			.substring(35,45);
		        	MySQLDBHelper.insertUserID("userid", followeduserid, connection);
		        	
		        }	
	        }
	        /*爬取粉丝*/
	        else if (url.contains("fans")) {
	        	Elements fans = page.select("table");
		        for (Element fan : fans) {
		        	String fanUrl = fan.select("table > tbody > tr > td > a").first().attr("href");     	
		        }
	        }
			break;
		}
		case "profile":{
			System.out.println("-->profile:");
			Elements weibos = page.select("div.c");
			for (Element weibo : weibos) {
				Elements urls = weibo.select("div>div>a");// .first().attr("href");
				String weiboId = null;
				for (Element urlst : urls) {
					String urlstr = urlst.attr("href");
					if (urlstr.contains("attitude")) {
						weiboId = urlstr.substring(urlstr.indexOf("attitude/") + 9, urlstr.indexOf("/add"));
						break;
					}
				}
				String content = weibo.select("div>div>span[class='ctt']").text(); // 微博内容
				
				String interInfors = weibo.select("div>div>a[href]").text(); // 点赞、转发、评论
				if (interInfors.contains("赞")) {
					content=content.substring(0,content.length()-4).trim();
					
					String[] interInforsList = interInfors.substring(interInfors.indexOf("赞["), interInfors.length())
							.split(" ");
					String like = interInforsList[0].substring(interInforsList[0].indexOf("[") + 1,
							interInforsList[0].indexOf(']'));
					String transfer = interInforsList[1].substring(interInforsList[1].indexOf("[") + 1,
							interInforsList[1].indexOf(']'));
					String comment = interInforsList[2].substring(interInforsList[2].indexOf("[") + 1,
							interInforsList[2].indexOf(']'));
					String pubInfors = weibo.select("div>div>span[class='ct']").text(); // 时间和使用工具（手机或平台）
					String time = "";
					String platform = "";
					if (pubInfors.contains("来自")) {
						time = pubInfors.substring(0, pubInfors.indexOf("来自"));
						platform = pubInfors.substring(pubInfors.indexOf("来自") + 2, pubInfors.length());
					} else {
						time = pubInfors.trim();
					}
					if (time.contains("月")) {
						Date d1 = null;
						try {
							d1 = new SimpleDateFormat("MM月dd日 HH:mm").parse(time);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // 定义起始日期
						SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
						SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
						SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
						SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
						String month = sdfMonth.format(d1);
						String day = sdfDay.format(d1);
						String hour = sdfHour.format(d1);
						String minute = sdfMinute.format(d1);
						time = "2017-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
					}
					if (time.contains("秒前")) {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.SECOND, -Integer.parseInt(time.substring(0, time.indexOf("秒前"))));
						time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
					}
					if (time.contains("分钟前")) {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.MINUTE, -Integer.parseInt(time.substring(0, time.indexOf("分钟前"))));
						time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
					}
					if (time.contains("今天")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String today = sdf.format(new Date());
						time = today + " " + time.substring(time.trim().indexOf("今天") + 3, time.length() - 1) + ":"
								+ "00";
					}
					WeiboModel weiboModel = new WeiboModel();
					weiboModel.setId(userID + "-" + weiboId);
					weiboModel.setUserid(userID);
					weiboModel.setContent(content);
					weiboModel.setLikes(like);
					weiboModel.setTransfers(transfer);
					weiboModel.setComments(comment);
					weiboModel.setTime(time);
					weiboModel.setPlatform(platform);
//					MySQLDBHelper.inserWeiboInfor(connection, weiboModel);
				}
			}
			break;
		}
		case "keyword":{
			System.out.println("-->type: keyword");
			Elements weibos = page.select("div.c");
			for (Element weibo : weibos) {
				Elements urls = weibo.select("div>div>a");// .first().attr("href");
				String weiboId = null;
				for (Element urlst : urls) {
					String urlstr = urlst.attr("href");
					if (urlstr.contains("attitude")) {
						weiboId = urlstr.substring(urlstr.indexOf("attitude/") + 9, urlstr.indexOf("/add"));
						break;
					}
				}
				String content = weibo.select("div>div>span[class='ctt']").text(); // 微博内容
				
				String interInfors = weibo.select("div>div>a[href]").text(); // 点赞、转发、评论
				if (interInfors.contains("赞")) {
					content=content.substring(0,content.length()-4).trim();
					
					String[] interInforsList = interInfors.substring(interInfors.indexOf("赞["), interInfors.length())
							.split(" ");
					String like = interInforsList[0].substring(interInforsList[0].indexOf("[") + 1,
							interInforsList[0].indexOf(']'));
					String transfer = interInforsList[1].substring(interInforsList[1].indexOf("[") + 1,
							interInforsList[1].indexOf(']'));
					String comment = interInforsList[2].substring(interInforsList[2].indexOf("[") + 1,
							interInforsList[2].indexOf(']'));
					String pubInfors = weibo.select("div>div>span[class='ct']").text(); // 时间和使用工具（手机或平台）
					String time = "";
					String platform = "";
					if (pubInfors.contains("来自")) {
						time = pubInfors.substring(0, pubInfors.indexOf("来自"));
						platform = pubInfors.substring(pubInfors.indexOf("来自") + 2, pubInfors.length());
					} else {
						time = pubInfors.trim();
					}
					if (time.contains("月")) {
						Date d1 = null;
						try {
							d1 = new SimpleDateFormat("MM月dd日 HH:mm").parse(time);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // 定义起始日期
						SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
						SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
						SimpleDateFormat sdfHour = new SimpleDateFormat("HH");
						SimpleDateFormat sdfMinute = new SimpleDateFormat("mm");
						String month = sdfMonth.format(d1);
						String day = sdfDay.format(d1);
						String hour = sdfHour.format(d1);
						String minute = sdfMinute.format(d1);
						time = "2017-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
					}
					if (time.contains("秒前")) {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.SECOND, -Integer.parseInt(time.substring(0, time.indexOf("秒前"))));
						time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
					}
					if (time.contains("分钟前")) {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.MINUTE, -Integer.parseInt(time.substring(0, time.indexOf("分钟前"))));
						time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
					}
					if (time.contains("今天")) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String today = sdf.format(new Date());
						time = today + " " + time.substring(time.trim().indexOf("今天") + 3, time.length() - 1) + ":"
								+ "00";
					}
					WeiboModel weiboModel = new WeiboModel();
					weiboModel.setId(userID + "-" + weiboId);
					weiboModel.setUserid(userID);
					weiboModel.setContent(content);
					weiboModel.setLikes(like);
					weiboModel.setTransfers(transfer);
					weiboModel.setComments(comment);
					weiboModel.setTime(time);
					weiboModel.setPlatform(platform);
//					MySQLDBHelper.inserWeiboInfor(connection, weiboModel);
					System.out.println(weiboModel.toString());
				}
			}
			break;
		}

		default:
			break;
		}
        
        if(true){
        	return;
        }
        
        
        
       
  
        /*爬取转发者*/
        else if (url.contains("repost")) {
        	Elements reposts = page.select("div.c");
        	String weiboId = page.meta("weiboID");
        	for (int i = 2; i < reposts.size(); i++) {
	        	String repostUsers = reposts.select("div.c").get(i).select("a").get(0).text();
	        	if(!repostUsers.contains("返回") && !repostUsers.contains("查看更多热门")){
		        	WeiboModel weiboModel = new WeiboModel();
		            weiboModel.setId(userID + "-" + weiboId);
		            weiboModel.setRepostusers(repostUsers);
		            MySQLDBHelper.inserWeiboRepostUsers(connection, weiboModel);
	        	}
			}	
        }
        /*爬取评论者*/
        else if (url.contains("comment")) {
        	Elements comments = page.select("div.c");
        	String weiboId = page.meta("weiboID");
        	for (int i = 2; i < comments.size(); i++) {
        		String commentUsers = comments.select("div.c").get(i).select("a").get(0).text();
        		if(!commentUsers.contains("返回") && !commentUsers.contains("查看更多热门")){
    	        	WeiboModel weiboModel = new WeiboModel();
    	            weiboModel.setId(userID + "-" + weiboId);
    	            weiboModel.setCommentusers(commentUsers);
    	            MySQLDBHelper.inserWeiboCommentUsers(connection, weiboModel); 	
        		}
			}	
        }
        /*爬取关注者*/
        else if (url.contains("follow")) {
        	Elements follows = page.select("table");
	        for (Element follow : follows) {
	        	String followUrl = follow.select("table > tbody > tr > td > a").first().attr("href");
	        	next.add(new CrawlDatum(followUrl).meta("childUserID", userID));	        	
	        }	
        }
        /*爬取粉丝*/
        else if (url.contains("fans")) {
        	Elements fans = page.select("table");
	        for (Element fan : fans) {
	        	String fanUrl = fan.select("table > tbody > tr > td > a").first().attr("href");     	
	        	next.add(new CrawlDatum(fanUrl).meta("parentUserID", userID));
	        }
        }
        /*爬取基本信息*/
        else if (url.contains("info")) {
        	String infors = page.select("div.c").get(2).text();
        	String[] inforsList = infors.split(" ");
        	String nickName = "";
        	String birthday = "";
        	String gender = "";
        	String marriage = "";
        	String address = "";
        	String signature = "";
        	String profile = "";
        	String homeUrl = "http://weibo.cn/"+ userID;
        	if(infors.contains("昵称:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("昵称:")) {
						nickName = inforsList[i].substring(inforsList[i].indexOf("昵称:")+3);
						break;
					}					
				}        		
        	}
        	if(infors.contains("生日:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("生日:")) {
						birthday = inforsList[i].substring(inforsList[i].indexOf("生日:")+3);
						break;
					}					
				}        		
        	}
        	if(infors.contains("性别:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("性别:")) {
						gender = inforsList[i].substring(inforsList[i].indexOf("性别:")+3);
						break;
					}					
				}        		
        	}
        	if(infors.contains("感情状况:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("感情状况:")) {
						marriage = inforsList[i].substring(inforsList[i].indexOf("感情状况:")+5);
						break;
					}					
				}        		
        	}
        	if(infors.contains("地区:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("地区:")) {
						address = inforsList[i].substring(inforsList[i].indexOf("地区:")+3);
						if(++i < inforsList.length && !inforsList[i].contains("：") && !inforsList[i].contains(":"))
							address += " " + inforsList[i];
						break;
					}					
				}        		
        	}
        	if(infors.contains("认证信息：")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("认证信息：")) {
						signature = inforsList[i].substring(inforsList[i].indexOf("认证信息：")+5);
						break;
					}				
				}        		
        	}
        	if(infors.contains("简介:")){
        		for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("简介:")) {
						profile = inforsList[i].substring(inforsList[i].indexOf("简介:")+3);
						break;
					}					
				}        		
        	}        	
        	UserModel userModel = new UserModel();
        	userModel.setId(userID);
        	userModel.setNickname(nickName);
        	userModel.setBirthday(birthday);
        	userModel.setGender(gender);
        	userModel.setMarriage(marriage);
        	userModel.setAddress(address);
        	userModel.setSignature(signature);
        	userModel.setProfile(profile);
        	userModel.setHomeUrl(homeUrl);
        	MySQLDBHelper.inserUserBasicInfor(connection, userModel);        	
        }
        /*爬取个人标签*/
        else if (url.contains("tags")) {
        	String tags = page.select("div.c").get(2).select("a").text();
        	UserModel userModel = new UserModel();
        	userModel.setId(userID);
        	userModel.setTags(tags);
        	MySQLDBHelper.inserUserTagsInfor(connection, userModel);
        }
        /*爬取ID和用户信息页*/
        else {  
        	//用户信息页链接
        	String weiboUserId = page.select("div.tip2").get(0).select("a").get(0).attr("href").split("/")[1];
        	//微博数目
        	String weiboNumStr = page.select("div.tip2").get(0).select("span.tc").text();
        	String weiboNum = weiboNumStr.substring(weiboNumStr.indexOf('[')+1, weiboNumStr.indexOf(']'));
        	//关注数目
        	String followsNumStr = page.select("div.tip2").get(0).select("a").get(0).text();
        	String followsNum = followsNumStr.substring(followsNumStr.indexOf('[')+1, followsNumStr.indexOf(']'));
        	//粉丝数目
        	String fansNumStr = page.select("div.tip2").get(0).select("a").get(1).text();
        	String fansNum = fansNumStr.substring(fansNumStr.indexOf('[')+1, fansNumStr.indexOf(']'));
	      	
        	//添加用户信息链接至爬虫池
	        next.add(new CrawlDatum("http://weibo.cn/"+ weiboUserId + "/info").meta("userID", weiboUserId));	        
	        //添加用户标签
	        next.add(new CrawlDatum("http://weibo.cn/account/privacy/tags/?uid=" + weiboUserId).meta("userID", weiboUserId));
	        //添加微博、粉丝、关注链接至爬虫池
	        for (int i = 1; i <= 80; i++) {
	            next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId +"/profile?filter=1&page=" + i).meta("userID", weiboUserId));
	        }	        
	        for (int i = 1; i <= 30; i++) {
	            next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId +"/follow?page=" + i).meta("userID", weiboUserId));
	            next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId +"/fans?page=" + i).meta("userID", weiboUserId));
	        }
        	UserModel userModel = new UserModel();
        	userModel.setId(weiboUserId);
        	userModel.setWeiboNum(weiboNum);
        	userModel.setFansNum(fansNum);
        	userModel.setFollowsNum(followsNum);
        	MySQLDBHelper.inserUserNumInfor(connection, userModel);
        	
//        	String childUserID = page.getMetaData("childUserID");
//        	if(childUserID != null){
//        		UserModel userFansModel = new UserModel();
//        		userFansModel.setId(userID);
//        		userFansModel.setFans(childUserID);;
//        		MySQLDBHelper.inserUserFansInfor(connection, userFansModel);
//        		
//        		UserModel userFallowModel = new UserModel();
//        		userFallowModel.setId(childUserID);
//        		userFallowModel.setFans(userID);
//        		MySQLDBHelper.inserUserFansInfor(connection, userFallowModel);
//        	}        	
//        	String parentUserID = page.getMetaData("parentUserID");
//        	if(parentUserID !=null){
//        		UserModel userFallowModel = new UserModel();
//        		userFallowModel.setId(userID);
//        		userFallowModel.setFans(parentUserID);
//        		MySQLDBHelper.inserUserFansInfor(connection, userFallowModel);
//        		
//        		UserModel userFansModel = new UserModel();
//        		userFansModel.setId(parentUserID);
//        		userFansModel.setFans(userID);
//        		MySQLDBHelper.inserUserFansInfor(connection, userFansModel);
//        	}
		}
        
        
    }
	
	/**
	 * 从用户表中获取所有用户的tag页面url
	 * @param dbName
	 * @return
	 */
	private static List<String> getTagUrls(String dbName) {
		List<String> tagUrls=new ArrayList<>();
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 从关键词文件中获取所有关键词的搜索页面
	 * https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E4%B9%94%E5%B8%83%E6%96%AF&page=100
	 * @param filePath
	 * @return https://weibo.cn/search/mblog?hideSearchFrame=&keyword=keyword 后还需跟页码
	 */
	private static List<String> getKeywordUrls(String filePath){
		List<String> keywordUrls=new ArrayList<>();
		try {
			BufferedReader kwReader=new BufferedReader(new FileReader(filePath));
			String line;
			line=kwReader.readLine();
//			https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E4%B9%94%E5%B8%83%E6%96%AF&page=100
			String urlPre="https://weibo.cn/search/mblog?hideSearchFrame=&keyword=";
			while(line!=null&&line!=""){
				line=urlPre+line;
				keywordUrls.add(line);
				line=kwReader.readLine();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return keywordUrls;
	}
	
	/**
	 * 给爬虫添加爬取的页面
	 * @param crawler
	 * @param filePath
	 * @param pageNum
	 */
	private static void  setSeedKW(WeiboCrawler crawler,String filePath, int pageNum) {
		List<String> urls=getKeywordUrls(filePath);
		if(pageNum<=0||pageNum>100){
			pageNum=100;
		}
		for (String urlPre : urls) {
			for(int i=1;i<=pageNum;i++){
				System.out.println(urlPre+"&page="+i);
				crawler.addSeed(new CrawlDatum(urlPre+"&page="+i)
						.meta("userID","keywordCrawler")
						.meta("tpye","keyword"));
			}
		}
		
	}
	
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
    	loadProxy();
    	loadCookies();
    	connection = MySQLConnManager.creatConnection();
        WeiboCrawler crawler = new WeiboCrawler("weibo_crawler", false);
        crawler.setThreads(1);
        String filePath="./resource/crawler/热点事件 - 副本.txt";
        int pageNum=2;
        setSeedKW(crawler, filePath, pageNum);
        crawler.setResumable(false);
        crawler.setExecuteInterval(500);
        crawler.start(2); 
    }

}