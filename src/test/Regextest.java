package test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regextest {

	public static void main(String[] args) {
		listtest();
		
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
	
	public static void listtest(){
		String word="新闻";
		String sword="新闻";
		if(word==sword){
			System.out.println("first");
		}
		if(word.equals(sword)){
			System.out.println("second");
		}
	}
	
	
	

}
