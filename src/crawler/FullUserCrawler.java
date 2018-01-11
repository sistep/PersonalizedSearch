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
 * 爬取少量用户的完整信息
 */
public class FullUserCrawler extends BreadthCrawler {

	static Connection connection;

	String cookie;
	String cookieFile="resource/sinacookies-3.txt";
	int cookieId = 0;
	int cookieCurrNum = 0;
	public static ArrayList<String> cookies = new ArrayList<String>();

	int proxyId = 0;
	int proxyCurrNum = 0;
	int proxyCircle = 0;
	public static ArrayList<String> hostname = new ArrayList<String>();
	public static ArrayList<Integer> port = new ArrayList<Integer>();

	public void loadProxy() {
		BufferedReader read = null;
		try {
			URL realurl = new URL("http://127.0.0.1:8000/?types=0&count=200&country=%E5%9B%BD%E5%86%85");
			URLConnection connection = realurl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			read = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;// 循环读取
			while ((line = read.readLine()) != null) {
				String[] str = line.split("],");
				for (int i = 0; i < str.length; i++) {
					String[] temp = str[i].split(",");
					if (temp.length == 3) {
						hostname.add(temp[0].substring(temp[0].indexOf("\"") + 1, temp[0].indexOf("\"", 5)));
						port.add(Integer.parseInt(temp[1].trim()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (read != null) {// 关闭流
				try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// try {
		// FileReader reader = new FileReader("proxy.txt");
		// BufferedReader br = new BufferedReader(reader);
		// String str = null;
		// while((str = br.readLine()) != null) {
		// String[] temp = str.split(":");
		// if(temp.length ==2){
		// hostname.add(temp[0]);
		// port.add(Integer.parseInt(temp[1]));
		// }
		// }
		// br.close();
		// reader.close();
		// }
		// catch(FileNotFoundException e) {
		// e.printStackTrace();
		// }
		// catch(IOException e) {
		// e.printStackTrace();
		// }
	}

	public void loadCookies() {
		try {
			FileReader reader = new FileReader(cookieFile);
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null) {
				cookies.add(str);
			}
			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FullUserCrawler(String crawlPath, boolean autoParse) throws Exception {
		super(crawlPath, autoParse);
		loadCookies();
		cookie = cookies.get(cookieId);
	}
	
	

	@Override
	public Page getResponse(CrawlDatum crawlDatum) throws Exception {
		/*更换cookie*/
		if(requestCount.incrementAndGet()>=100){
			requestCount.set(0);
			if(cookieCount.incrementAndGet()>=cookies.size()){
				cookieCount.set(0);
			}
			cookie=cookies.get(cookieCount.get());
		}
		HttpRequest request = new HttpRequest(crawlDatum);
		request.setCookie(cookie);
		

		if (proxyCurrNum.get() == 5 && proxyCircle.get() < 5) {
			proxyCurrNum.set(0);
			;
			if (proxyId.incrementAndGet() < hostname.size()) {
				request.setProxy(new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(hostname.get(proxyId.get()), port.get(proxyId.get()))));
			} else {
				proxyId.set(0);
				proxyCircle.incrementAndGet();
				request.setProxy(new Proxy(Proxy.Type.HTTP,
						new InetSocketAddress(hostname.get(proxyId.get()), port.get(proxyId.get()))));
			}
		} else if (proxyCurrNum.get() == 5 && proxyCircle.get() >= 5) {
			loadProxy();
			proxyId.set(0);
			
			proxyCircle.set(0);
			
			request.setProxy(new Proxy(Proxy.Type.HTTP,
					new InetSocketAddress(hostname.get(proxyId.get()), port.get(proxyId.get()))));
		}

		Page page = null;
		boolean proxySucced = false;
        do {
	        try {
	        	page=request.responsePage();
	        	proxySucced = true;
			} catch (Exception e) {
				
				if (e.toString().contains("connect timed out") || 
					e.toString().contains("Unexpected end of file from server") || 
					e.toString().contains("Server returned HTTP response code") ||
					e.toString().contains("Read timed out")) {
					if(proxyId.incrementAndGet() < hostname.size()){
						request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId.get()),port.get(proxyId.get()))));
					}else{
						proxyId.set(0);;
						proxyCircle.incrementAndGet();
						request.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId.get()),port.get(proxyId.get()))));
					}
				}
				else{/*更换cookie*/
					if(cookieCount.incrementAndGet() < cookies.size()){
						cookie = cookies.get(cookieCount.get());
					}else{
						cookieCount.set(0);
						cookie = cookies.get(cookieCount.get());
					}
					request.setCookie(cookie);
				}
			}
        } while (!proxySucced);
		
		
		return page;
	}
	
	

	@SuppressWarnings("deprecation")
	@Override
	public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
		cookieCurrNum++;
		proxyCurrNum++;
		HttpRequest request = new HttpRequest(crawlDatum);
		request.setCookie(cookie);
		if (proxyCurrNum == 5 && proxyCircle < 5) {
			proxyCurrNum = 0;
			if (++proxyId < hostname.size()) {
				request.setProxy(
						new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId), port.get(proxyId))));
			} else {
				proxyId = 0;
				proxyCircle++;
				request.setProxy(
						new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId), port.get(proxyId))));
			}
		} else if (proxyCurrNum == 5 && proxyCircle >= 5) {
			loadProxy();
			proxyId = 0;
			proxyCircle = 0;
			request.setProxy(
					new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname.get(proxyId), port.get(proxyId))));
		}
		if (cookieCurrNum == 20) {
			cookieCurrNum = 0;
			if (++cookieId < cookies.size()) {
				cookie = cookies.get(cookieId);
			} else {
				cookieId = 0;
				cookie = cookies.get(cookieId);
			}
		}
		boolean proxySucced = false;
		do {
			try {
				request.response();
				proxySucced = true;
			} catch (Exception e) {
				// TODO: handle exception
				if (e.toString().contains("connect timed out")
						|| e.toString().contains("Unexpected end of file from server")
						|| e.toString().contains("Server returned HTTP response code")
						|| e.toString().contains("Read timed out")) {
					if (++proxyId < hostname.size()) {
						request.setProxy(new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(hostname.get(proxyId), port.get(proxyId))));
					} else {
						proxyId = 0;
						proxyCircle++;
						request.setProxy(new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(hostname.get(proxyId), port.get(proxyId))));
					}
				} else {
					if (++cookieId < cookies.size()) {
						cookie = cookies.get(cookieId);
					} else {
						cookieId = 0;
						cookie = cookies.get(cookieId);
					}
					request.setCookie(cookie);
				}
			}
		} while (!proxySucced);
		return request.response();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void visit(Page page, CrawlDatums next) {
		String userID = page.meta("userID");
		String url = page.url();
		/* 爬取微博内容 */
		if (url.contains("profile")) {
			String type="post";
			if(url.contains("filter=2")){
				type="repost";
			}
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
					String[] interInforsList = interInfors.substring(interInfors.indexOf("赞["), interInfors.length())
							.split(" ");
					String like = interInforsList[0].substring(interInforsList[0].indexOf("[") + 1,
							interInforsList[0].indexOf(']'));
					String transfer = interInforsList[1].substring(interInforsList[1].indexOf("[") + 1,
							interInforsList[1].indexOf(']'));
					// if(Integer.parseInt(transfer) > 0){
					// for (int i = 1; i <= Integer.parseInt(transfer) / 9 + 1;
					// i++) {
					// next.add(new CrawlDatum("http://weibo.cn/repost/" +
					// weiboId + "?uid=" + userID + "&page=" +
					// i).putMetaData("weiboID", weiboId).putMetaData("userID",
					// userID));
					// }
					// }
					String comment = interInforsList[2].substring(interInforsList[2].indexOf("[") + 1,
							interInforsList[2].indexOf(']'));
					// if(Integer.parseInt(comment) > 0){
					// for (int i = 1; i <= Integer.parseInt(comment) / 10 + 1;
					// i++) {
					// next.add(new CrawlDatum("http://weibo.cn/comment/" +
					// weiboId + "?uid=" + userID + "&page=" +
					// i).putMetaData("weiboID", weiboId).putMetaData("userID",
					// userID));
					// }
					// }
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
					weiboModel.setType(type);
					MySQLDBHelper.insertWeiboInfo(connection, weiboModel);
				}
			}
		}
		/* 爬取转发者 */
		else if (url.contains("repost")) {
			Elements reposts = page.select("div.c");
			String weiboId = page.meta("weiboID");
			for (int i = 2; i < reposts.size(); i++) {
				String repostUsers = reposts.select("div.c").get(i).select("a").get(0).text();
				if (!repostUsers.contains("返回") && !repostUsers.contains("查看更多热门")) {
					WeiboModel weiboModel = new WeiboModel();
					weiboModel.setId(userID + "-" + weiboId);
					weiboModel.setRepostusers(repostUsers);
					MySQLDBHelper.inserWeiboRepostUsers(connection, weiboModel);
				}
			}
		}
		/* 爬取评论者 */
		else if (url.contains("comment")) {
			Elements comments = page.select("div.c");
			String weiboId = page.meta("weiboID");
			for (int i = 2; i < comments.size(); i++) {
				String commentUsers = comments.select("div.c").get(i).select("a").get(0).text();
				if (!commentUsers.contains("返回") && !commentUsers.contains("查看更多热门")) {
					WeiboModel weiboModel = new WeiboModel();
					weiboModel.setId(userID + "-" + weiboId);
					weiboModel.setCommentusers(commentUsers);
					MySQLDBHelper.inserWeiboCommentUsers(connection, weiboModel);
				}
			}
		}
		/* 爬取关注者 */
		else if (url.contains("follow")) {
//			Elements follows = page.select("table");
//			for (Element follow : follows) {
//				String idUrl = follow.select("table > tbody > tr > td").get(2).select("a").attr("href");
//				//next.add(new CrawlDatum(followUrl).putMetaData("childUserID", userID));
//				System.out.println("-->idurl:"+idUrl);
//				String id=idUrl.substring(idUrl.indexOf('=')+1,idUrl.indexOf('&'));
//				System.out.println("-->id:"+id);
//				
//			}
			Elements idurls=page.select("a[href^=http://weibo.cn/attention/add]");
			for (Element idUrl : idurls) {
				String idUrlStr=idUrl.attr("href");
				String id=idUrlStr.substring(idUrlStr.indexOf('=')+1,idUrlStr.indexOf('&'));
				
				 UserModel user = new UserModel();
				 user.setId(userID);
				 user.setFollows(id);
				 MySQLDBHelper.inserUserFollowsInfor(connection, user);
			}
		}
		/* 爬取粉丝 */
		else if (url.contains("fans")) {
			Elements fans = page.select("table");
			for (Element fan : fans) {
				String fanUrl = fan.select("table > tbody > tr > td > a").first().attr("href");
				next.add(new CrawlDatum(fanUrl).meta("parentUserID", userID));
			}
		}
		/* 爬取基本信息 */
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
			String homeUrl = "http://weibo.cn/" + userID;
			if (infors.contains("昵称:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("昵称:")) {
						nickName = inforsList[i].substring(inforsList[i].indexOf("昵称:") + 3);
						break;
					}
				}
			}
			if (infors.contains("生日:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("生日:")) {
						birthday = inforsList[i].substring(inforsList[i].indexOf("生日:") + 3);
						break;
					}
				}
			}
			if (infors.contains("性别:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("性别:")) {
						gender = inforsList[i].substring(inforsList[i].indexOf("性别:") + 3);
						break;
					}
				}
			}
			if (infors.contains("感情状况:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("感情状况:")) {
						marriage = inforsList[i].substring(inforsList[i].indexOf("感情状况:") + 5);
						break;
					}
				}
			}
			if (infors.contains("地区:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("地区:")) {
						address = inforsList[i].substring(inforsList[i].indexOf("地区:") + 3);
						if (++i < inforsList.length && !inforsList[i].contains("：") && !inforsList[i].contains(":"))
							address += " " + inforsList[i];
						break;
					}
				}
			}
			if (infors.contains("认证信息：")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("认证信息：")) {
						signature = inforsList[i].substring(inforsList[i].indexOf("认证信息：") + 5);
						break;
					}
				}
			}
			if (infors.contains("简介:")) {
				for (int i = 0; i < inforsList.length; i++) {
					if (inforsList[i].contains("简介:")) {
						profile = inforsList[i].substring(inforsList[i].indexOf("简介:") + 3);
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
		/* 爬取个人标签 */
		else if (url.contains("tags")) {
			String tags = page.select("div.c").get(2).select("a").text();
			UserModel userModel = new UserModel();
			userModel.setId(userID);
			userModel.setTags(tags);
			MySQLDBHelper.inserUserTagsInfor(connection, userModel);
		}
		/* 爬取ID和用户信息页 */
		else {
			// 用户信息页链接
			String weiboUserId = page.select("div.tip2").get(0).select("a").get(0).attr("href").split("/")[1];
			// 微博数目
			String weiboNumStr = page.select("div.tip2").get(0).select("span.tc").text();
			String weiboNum = weiboNumStr.substring(weiboNumStr.indexOf('[') + 1, weiboNumStr.indexOf(']'));
			// 关注数目
			String followsNumStr = page.select("div.tip2").get(0).select("a").get(0).text();
			String followsNum = followsNumStr.substring(followsNumStr.indexOf('[') + 1, followsNumStr.indexOf(']'));
			// 粉丝数目
			String fansNumStr = page.select("div.tip2").get(0).select("a").get(1).text();
			String fansNum = fansNumStr.substring(fansNumStr.indexOf('[') + 1, fansNumStr.indexOf(']'));
//
			// 添加用户信息链接至爬虫池
			next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId + "/info").meta("userID", weiboUserId));
			// 添加用户标签
			next.add(new CrawlDatum("http://weibo.cn/account/privacy/tags/?uid=" + weiboUserId).meta("userID",
					weiboUserId));
			// 添加原创微博内容链接
//			for (int i = 1; i <= 80; i++) {
//				next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId + "/profile?filter=1&page=" + i)
//						.putMetaData("userID", weiboUserId)
//						.meta("type","post"));
//			}
			//添加转发微博内容链接
//			for (int i = 1; i <= 80; i++) {
//				next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId + "/profile?filter=2&page=" + i)
//						.putMetaData("userID", weiboUserId)
//						.meta("type","repost"));
//			}
			//添加关注页
//			for (int i = 1; i <= 30; i++) {
//				next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId + "/follow?page=" + i).putMetaData("userID",
//						weiboUserId));
////				next.add(new CrawlDatum("http://weibo.cn/" + weiboUserId + "/fans?page=" + i).putMetaData("userID",
////						weiboUserId));
//			}
			UserModel userModel = new UserModel();
			userModel.setId(weiboUserId);
			userModel.setWeiboNum(weiboNum);
			userModel.setFansNum(fansNum);
			userModel.setFollowsNum(followsNum);
			MySQLDBHelper.inserUserNumInfor(connection, userModel);

			// String childUserID = page.getMetaData("childUserID");
			// if(childUserID != null){
			// UserModel userFansModel = new UserModel();
			// userFansModel.setId(userID);
			// userFansModel.setFans(childUserID);;
			// MySQLDBHelper.inserUserFansInfor(connection, userFansModel);
			//
			// UserModel userFallowModel = new UserModel();
			// userFallowModel.setId(childUserID);
			// userFallowModel.setFans(userID);
			// MySQLDBHelper.inserUserFansInfor(connection, userFallowModel);
			// }
			// String parentUserID = page.getMetaData("parentUserID");
			// if(parentUserID !=null){
			// UserModel userFallowModel = new UserModel();
			// userFallowModel.setId(userID);
			// userFallowModel.setFans(parentUserID);
			// MySQLDBHelper.inserUserFansInfor(connection, userFallowModel);
			//
			// UserModel userFansModel = new UserModel();
			// userFansModel.setId(parentUserID);
			// userFansModel.setFans(userID);
			// MySQLDBHelper.inserUserFansInfor(connection, userFansModel);
			// }
		}
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		connection = MySQLConnManager.creatConnection();
		FullUserCrawler crawler = new FullUserCrawler("full user craweler", false);
		crawler.loadProxy();
		crawler.setThreads(2);
		List<String> seedId = new ArrayList<>();
		BufferedReader bReader = new BufferedReader(new FileReader("resource/followsuserid.txt"));
		String aid = bReader.readLine();
		while (aid != null) {
			seedId.add(aid);
			aid = bReader.readLine();
		}
		bReader.close();

		for (int i = seedId.size()-1; i>=3000; i--) {
			
			//添加主页
			crawler.addSeed(new CrawlDatum("http://weibo.cn/" +seedId.get(i)).meta("userID", seedId.get(i)));
			// 添加用户信息链接至爬虫池
			 crawler.addSeed(new CrawlDatum("http://weibo.cn/"+ seedId.get(i) + "/info").meta("userID",seedId.get(i)));
			// 添加用户标签
			 crawler.addSeed(new CrawlDatum("http://weibo.cn/account/privacy/tags/?uid="
			 +seedId.get(i)).meta("userID",seedId.get(i)));
			
			// 添加原创微博
//			for (int j = 1; j <= 80; j++) {
//				crawler.addSeed(new CrawlDatum("http://weibo.cn/" + seedId.get(i) + "/profile?filter=1&page=" + j)
//						.putMetaData("userID", seedId.get(i)));
//			}
		}
		// crawler.addSeed(new
		// CrawlDatum("http://weibo.cn/1756807885/info").putMetaData("userID",
		// "1756807885"));
//		crawler.addSeed(new CrawlDatum("http://weibo.cn/2711387065/follow").meta("userID","2711387065"));
		crawler.setResumable(true);
		crawler.setExecuteInterval(1000);
		crawler.start(2);
	}

}
