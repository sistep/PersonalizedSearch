package word2vec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nlpir.Nlpir;

public class WikiSeg {

	public static void main(String[] args) {
		run();
		System.out.println("ok");
	}
	
	public static void run(){
		try {
			BufferedReader bReader=new BufferedReader(new FileReader("resource/wiki.zh.text.jian.utf8"));
			BufferedWriter bWriter =new BufferedWriter(new FileWriter("resource/wiki.zh.text.jian.utf8.seg"));
			String line=bReader.readLine();
			int i=0;
			while(line!=null&&line!=""){
				String segLine=Nlpir.NLPIR_ParagraphProcess(line, 0);
				bWriter.write(segLine);
				bWriter.newLine();
				line=bReader.readLine();
				i++;
				if(i%10000==0){
					System.out.println("-->"+i);
				}
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
