package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regextest {

	public static void main(String[] args) {
		String url="http://weibo.cn/5479567811/D1RizsO3L";
		String	userID=url.substring(url.indexOf("cn/")+3, url.lastIndexOf("/"));
		System.out.println(userID);
		String weiboID=userID+"-"+url.substring(url.lastIndexOf("/")+1);
		System.out.println(weiboID);
		
	}
	
	public static String getNNandNR(String line){
		String regex="\\(N[N|R] .*?\\)";
		Pattern ptn=Pattern.compile(regex);
		Matcher matcher=ptn.matcher(line);
		String names="";
		while(matcher.find()){
			String str=matcher.group();
			str=str.substring(str.indexOf(' ')+1,str.lastIndexOf(')'));
			names+=str;
			
		}
		return names;
	}
	
	

}
