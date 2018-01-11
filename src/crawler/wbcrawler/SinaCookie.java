package crawler.wbcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class SinaCookie {

	public static void main(String[] args) throws Exception {
		String accountFile="resource/crawler/myaccounts.txt";
		String cookieFile="resource/crawler/mysinacookies.txt";
//		run(accountFile,cookieFile);
		System.out.println(WeiboCN.getSinaCookie("17085562954", "yljrl3779"));
	}
	
	public static void run(String accountFile,String cookieFile) {
		int startIndex=0;
		try {
			List<String> accounts=new ArrayList<>();
			BufferedReader accountReader=new BufferedReader(new FileReader(accountFile));
			String line=accountReader.readLine();
			while(line!=null){
				accounts.add(line);
				line=accountReader.readLine();
			}
			accountReader.close();
			BufferedWriter cookieWriter=new BufferedWriter(new FileWriter(cookieFile));
//			System.out.println(WeiboCN.getSinaCookie("17801117504", "123456a"));
			for (int i=startIndex;i<accounts.size();i++) {
				String accountLine=accounts.get(i);
				String[] account=accountLine.split("----");
				try {
					System.out.println(account[0]+" "+account[1]);
					String cookie=WeiboCN.getSinaCookie(account[0], account[1]);
					System.out.println(cookie);
					cookieWriter.write(cookie);
					cookieWriter.newLine();
				} catch (Exception e) {
					System.out.println("-->account failed="+accountLine);
				}
			
			}
			cookieWriter.flush();
			cookieWriter.close();
			System.out.println("Finished");
		
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
