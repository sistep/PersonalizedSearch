/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.wbcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import dbutil.DbHelper;
import dbutil.MySQLConnManager;
import dbutil.MySQLDBHelper;
import model.UserModel;
import model.WeiboModel;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 该登录算法适用时间: 2017-6-2 —— ? 
 * 利用WebCollector和获取的cookie爬取新浪微博并抽取数据
 * 
 * @author hu
 */
public class WeiboCrawler extends BreadthCrawler {

	/*自定义全局变量*/
	static String accountFile="./resource/crawler/myaccounts.txt";
	int threadsNum=1;
	int executeInterval=2000;
	int accountId=0;
	/*全局变量*/
	String cookie;
	static Connection connection;

	public WeiboCrawler(String crawlPath) throws Exception {
		super(crawlPath, false);
		/* 获取新浪微博的cookie，账号密码以明文形式传输，请使用小号 */
		BufferedReader bReader=new BufferedReader(new FileReader(accountFile));
		List<String> accounts=new ArrayList<>();
		String line=bReader.readLine();
		while (line!=null&&line!="") {
			accounts.add(line);
			line=bReader.readLine();
		}
		if(accountId>=accounts.size()){
			accountId=0;
		}
		String[] account=accounts.get(accountId).split("----");
		String username=account[0];
		String password=account[1];
		System.out.println(username+"----"+password);
//		cookie = WeiboCN.getSinaCookie(username, password);
		cookie="_T_WM=2d162a5cec2e62193c4844c40f8a98bf; SUHB=0TPVeWD8gm70iA; SCF=AoK1IqrgzKt-gSO_mM25v4W8a-GsByiEy5zZp387-17QYjRPHYaHBtKXcT4Pv6dVs4zHCPoTB9nLqDRmS7TbS24.; SUB=_2A253EwuTDeRhGeBK61YW-CzFzTSIHXVU_5XbrDV6PUJbkdANLRGikW1NR_kGXAaGiXA6khxK6bbfaCeoKWTUB9l0; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WW4NEH05B3mSiX2Qog0-UCX5JpX5K-hUgL.FoqXehBN1hz4Son2dJLoI7fiC-LBBNiyUcpLMbXE; SSOLoginState=1511488453";
		//设置线程数
		setThreads(threadsNum);
		//设置每个线程的爬取间隔
		getConf().setExecuteInterval(executeInterval);
	}
	
	

	
	@Override
	public Page getResponse(CrawlDatum crawlDatum) throws Exception {
	
		HttpRequest request = new HttpRequest(crawlDatum);
		request.setCookie(cookie);
			
		return request.responsePage();
	}
	
	
	
	public static void main(String[] args) throws Exception {
//		crawlWeiboByTags();
//		crawlTagByID();
//		crawlWeiboByKeywords();
		crawlRumor();
	}
	
	private static void crawlRumor() throws Exception {
		WeiboCrawler crawler = new WeiboCrawler("crawler_rumor");
		connection = MySQLConnManager.creatConnection();
		String dbName="weibo_rumor";
		DbHelper.setDbName(dbName);
		String rumorFile="./resource/crawler/rumorURL.txt";
		BufferedReader rumorUrlReader=new BufferedReader(new FileReader(rumorFile));
		String url=rumorUrlReader.readLine();
		String userID="";
		String weiboID="";
		while (url!=null&&url!="") {
//			http://weibo.cn/5479567811/D1RizsO3L
			userID=url.substring(url.indexOf("cn/")+3, url.lastIndexOf("/"));
			weiboID=userID+"-"+url.substring(url.lastIndexOf("/")+1);
			crawler.addSeed(new CrawlDatum(url)
					.meta("userID",userID)
					.meta("weiboID",weiboID)
					.meta("type","rumor"));
			url=rumorUrlReader.readLine();
		}
		
        crawler.setResumable(true);
		crawler.start(2);
	}
	
	
	/**
	 * 按标签爬取微博
	 * @throws Exception
	 */
	private static void crawlWeiboByTags() throws Exception{
		WeiboCrawler crawler = new WeiboCrawler("crawler_weiboByTags_2");
		connection = MySQLConnManager.creatConnection();
		String weibodbName="weibo_tag";
		DbHelper.setDbName(weibodbName);
		String userdbName="user_m";
        int pageCount=100;
        setSeedTag(crawler, userdbName, pageCount);
        crawler.setResumable(true);
		crawler.start(2);
	}
	
	/**
	 * 按关键词爬取微博
	 * @throws Exception
	 */
	private static void crawlWeiboByKeywords() throws Exception{
		WeiboCrawler crawler = new WeiboCrawler("crawler_weibo_by_keywords");
		connection = MySQLConnManager.creatConnection();
		String dbName="weibo_k";
		DbHelper.setDbName(dbName);
		String filePath="./resource/crawler/热点事件.txt";
        int pageCount=100;
        setSeedKW(crawler, filePath, pageCount);
        crawler.setResumable(true);
		crawler.start(2);
	}
	
	/**
	 * 按ID更新tag
	 * @throws Exception
	 */
	private static void crawlTagByID() throws Exception{
		String dbName="user";
		WeiboCrawler crawler = new WeiboCrawler("crawler_updateTagByID_"+dbName);
		connection = MySQLConnManager.creatConnection();
		
		DbHelper.setDbName(dbName);
		setSeedUserTag(dbName, crawler);
        crawler.setResumable(true);
		crawler.start(2);
	}
	
	

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
		case "profile":{/*根据用户id爬取微博*/
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
		case "keyword":{/*根据关键词或标签爬取微博*/
			System.out.println("-->type: keyword");
			String keyword=page.meta("keyword");
			Elements weibos = page.select("div.c[id]");
			for (Element weibo : weibos) {
				Elements urls = weibo.select("div>div>a");// .first().attr("href");
				String weiboId = null;
				for (Element urlst : urls) {
					String urlstr = urlst.attr("href");
					if (urlstr.contains("attitude")) {
						weiboId = urlstr.substring(urlstr.indexOf("attitude/") + 9, urlstr.indexOf("/add"));
					}
					if(urlstr.contains("repost")){
						userID=urlstr.substring(urlstr.indexOf("uid=")+4,urlstr.lastIndexOf('&'));
					}
					
				}
				String content = weibo.select("div>div>span[class='ctt']").text(); // 微博内容
				
				String interInfors = weibo.select("div>div>a[href]").text(); // 点赞、转发、评论
				if (interInfors.contains("赞")) {
					/*删掉文本末尾特殊字符*/
					content=content.substring(0,content.length()-2).trim();
					
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
					if(content.charAt(0)==':'){
						content=content.substring(1);
					}
					weiboModel.setContent(content);
					weiboModel.setLikes(like);
					weiboModel.setTransfers(transfer);
					weiboModel.setComments(comment);
					weiboModel.setTime(time);
					weiboModel.setPlatform(platform);
					weiboModel.setType(keyword);
//					MySQLDBHelper.inserWeiboInfor(connection, weiboModel);
//					System.out.println(weiboModel.toString());
					DbHelper.insertWeiboInfo(connection, weiboModel);
				}
			}
			break;
		}
		case "tag":{/*根据用户id更新用户tag数据*/
			String tags = page.select("div.c").get(3).select("a").text();
			UserModel userModel = new UserModel();
			userModel.setId(userID);
			userModel.setTags(tags);
//			System.out.println("-->"+tags);
			DbHelper.updateTagInfo(connection, userModel);
			break;
		}
		case "rumor":{/*爬取谣言微博*/
			System.out.println("-->type: rumor");
			WeiboModel aWeibo=new WeiboModel();
			Element rumorWeibo = page.select("div.c[id=M_]").first();
			String content=rumorWeibo.select("div>span[class='ctt']").text();
			if(content.charAt(0)==':'){
				content=content.substring(1);
			}
			aWeibo.setContent(content);
			aWeibo.setUserid(page.meta("userID"));
			aWeibo.setId(page.meta("weiboID"));
			
			String pubInfors=rumorWeibo.select("div>span[class='ct']").text();
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
			
			
			
			aWeibo.setTime(time);
			aWeibo.setPlatform(platform);
			
			aWeibo.setType("post");
			Element weiboInfo=rumorWeibo.nextElementSibling().nextElementSibling();
			String[] infos=weiboInfo.text().split("  ");
			String like="0";
			if(infos[0].contains("[")){
				like=infos[0].substring(infos[0].indexOf('[')+1, infos[0].indexOf(']'));
			}
			String transfer="0";
			if(infos[1].contains("[")){
				transfer=infos[1].substring(infos[1].indexOf('[')+1, infos[1].indexOf(']'));
			}
			String comment="0";
			if(infos[2].contains("[")){
				comment=infos[2].substring(infos[2].indexOf('[')+1, infos[2].indexOf(']'));
			}
			aWeibo.setLikes(like);
			aWeibo.setTransfers(transfer);
			aWeibo.setComments(comment);
//			System.out.println(aWeibo.toString());
			DbHelper.insertWeiboInfo(connection, aWeibo);
			
			break;
		}

		default:
			break;
		}
        
       return;
    
        
        
	}
	
	
	/**
	 * 给爬虫添加爬取的页面
	 * https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E4%B9%94%E5%B8%83%E6%96%AF&page=100
	 * @param crawler
	 * @param filePath
	 * @param pageCount
	 */
	private static void  setSeedTag(WeiboCrawler crawler,String dbName, int pageCount) {
		
		if(pageCount<=0||pageCount>100){
			pageCount=100;
		}
		Connection tagConn=MySQLConnManager.creatConnection();
		String sql="select tags from "+dbName;
		try {
			PreparedStatement pstmt=tagConn.prepareStatement(sql);
			pstmt.setFetchSize(500);
			ResultSet rsTag=DbHelper.ExecuteQuery(pstmt, sql);
			String[] tags;
			String url;
			while(rsTag.next()){
				tags=rsTag.getNString("tags").split(" ");
				for(String tag : tags){
					if(tag!=""){
						for(int i=1;i<=pageCount;i++){
							
							url="https://weibo.cn/search/mblog?hideSearchFrame=&keyword="
									+tag+"&page="+i;
							crawler.addSeed(new CrawlDatum(url)
									.meta("userID",tag)
									.meta("type","keyword")
									.meta("pageNum", i)
									.meta("keyword",tag));
						}
					}
				}
			}
			rsTag.close();
			pstmt.close();
			tagConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * 添加网页。按用户id更新tag信息
	 * @param dbName
	 * @param crawler
	 */
	private static void setSeedUserTag(String dbName,WeiboCrawler crawler) {
		String sql="select id from "+dbName;
		try {
			Connection userIDConn=MySQLConnManager.creatConnection();
			PreparedStatement pstmt=userIDConn.prepareStatement(sql);
			pstmt.setFetchSize(500);
			ResultSet rs=DbHelper.ExecuteQuery(pstmt, sql);
			while(rs.next()){
				String userID=rs.getString("id");
				String url="https://weibo.cn/account/privacy/tags/?uid="+userID;
				crawler.addSeed(new CrawlDatum(url)
						.meta("userID",userID)
						.meta("type","tag"));
			}
			rs.close();
			pstmt.close();
			userIDConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 给爬虫添加爬取的页面
	 * https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E4%B9%94%E5%B8%83%E6%96%AF&page=100
	 * @param crawler
	 * @param filePath
	 * @param pageCount
	 */
	private static void  setSeedKW(WeiboCrawler crawler,String filePath, int pageCount) {
		List<String> keywords=getKeywordsFromFile(filePath);
		System.out.println("-->number of keywords: "+keywords.size());
		if(pageCount<=0||pageCount>100){
			pageCount=100;
		}
		int count=0;
		for (String word : keywords) {
			for(int i=1;i<=pageCount;i++){
				count++;
				String url="https://weibo.cn/search/mblog?hideSearchFrame=&keyword="
						+word+"&page="+i;
//				System.out.println(url);
				crawler.addSeed(new CrawlDatum(url)
						.meta("userID",word)
						.meta("type","keyword")
						.meta("pageNum", i)
						.meta("keyword",word));
			}
		}
		System.out.println("-->added "+count+" urls");
	}
	
	/**
	 * 从关键词文件中获取所有关键词
	 * 
	 * @param filePath
	 * @return 关键词列表
	 */
	private static List<String> getKeywordsFromFile(String filePath){
		List<String> keywordUrls=new ArrayList<>();
		try {
			BufferedReader kwReader=new BufferedReader(new FileReader(filePath));
			String line;
			line=kwReader.readLine();

			while(line!=null&&line!=""){
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
	
	
	
	

}