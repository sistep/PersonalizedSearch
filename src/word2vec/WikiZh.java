package word2vec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

public class WikiZh {

	public static void main(String[] args) {
//		readFileHeader("resource/word2vec/wiki.zh.vector", 200);;
		String str="{\"P349\":[{\"id";
		System.out.println(str);
		String regex="\"P\\d*?\"";
		Pattern prn=Pattern.compile(regex);
		Matcher mat=prn.matcher(str);
		if(mat.find()){
			System.out.println(mat.group(0));
		}
	}
	
	public static void readFileHeader(String fileName,int lineCount){
		try {
			BufferedReader bReader=new BufferedReader(new FileReader(fileName));
			int i=0;
			while(i<lineCount){
				String line=bReader.readLine();
				System.out.println(line);
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void readchwiki(){
		try {
			BufferedReader bReader=new BufferedReader(new FileReader("resource/zhwiki-latest-pages-articles.xml"));
			BufferedWriter bWriter=new BufferedWriter(new FileWriter("resource/zhwiki.txt"));
			
			String line=bReader.readLine();
			for(int i=1;i<=10000;i++){
				bWriter.write(line);
				bWriter.newLine();
				line=bReader.readLine();
			}
			bWriter.flush();
			bWriter.close();
			bReader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
