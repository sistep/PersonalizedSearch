package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopWords {

	public static void main(String[] args) {
		

	}
	
	public static void run(){
		try {
			Set<String> stopWords=new HashSet<>();
			String line;
			File file;
			BufferedReader bReader;
			file=new File("resource/stopwords/stopwords1.txt");
			bReader=new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			line=bReader.readLine().trim();
			
			while(line!=null&&line!=""){
				stopWords.add(line);
				line=bReader.readLine();
			}
			bReader.close();
			file=new File("resource/stopwords/stopwords2.txt");
			bReader=new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			line=bReader.readLine().trim();
			
			while(line!=null&&line!=""){
				stopWords.add(line);
				line=bReader.readLine();
			}
			bReader.close();
			file=new File("resource/stopwords/stopwords3.txt");
			bReader=new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			line=bReader.readLine().trim();
			
			while(line!=null&&line!=""){
				stopWords.add(line);
				line=bReader.readLine();
			}
			bReader.close();
			
			BufferedWriter bWriter=new BufferedWriter(new FileWriter("resource/stopwords/stopwords.txt"));
			for (String word : stopWords) {
				bWriter.write(word);
				bWriter.newLine();
			}
			bWriter.flush();
			bWriter.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
