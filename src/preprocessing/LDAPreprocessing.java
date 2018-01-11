package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import bomreader.*;
import nlpir.Nlpir;


public class LDAPreprocessing {

	public static void main(String[] args) {
		LDAPreprocessing ladp=new LDAPreprocessing();
		ladp.loadDicts();
		ladp.run();
		//ladp.processFile("resource/lda/usermodel/1015625032/weibo-35585.txt", "resource/lda/usermodel/1015625032/weiboContent");
		System.out.println("ok");
//		String string=" 看 图 说话  《 渔夫 》 （ 拙 和 ） # 平 水韵 # : “ 蓑 风 笠 雨 在 江湖 ， 不 问 凡 尘 问 野 凫 。 柳 系 扁舟 成 酒 客 ， 鸬鹚 一 放 做 渔夫 。 ” [哈哈] : “ 鸬鹚 展翅 捕鱼 忙 ， 细雨 霏霏 弱 柳 长 。 乍 觉 深 春 寒意 冷 ， 清晨 湖 上 景 茫茫 。 ” （ 图片 源自 老友 ， 致谢 ！ ） ";
//		string=string.replaceAll("\uFEFF", "");
//		String[] words=string.split(" ");
//		for (String string2 : words) {
//			System.out.println("->"+string2+"<-");
//		}

	}
	public Set<String> stopWords=new HashSet<>();
	public Set<String> emojis=new HashSet<>();
	
	public void loadDicts(){
		BufferedReader bReader;
		File stopwordsfile=new File("resource/stopwords/stopwords.txt");
		File sinaemoji=new File("resource/stopwords/emotions.txt");
		
		String line;
		try {
			bReader=new BufferedReader(new InputStreamReader(new FileInputStream(sinaemoji),"utf-8"));
			line=bReader.readLine().trim();
			while(line!=null&&line!=""){
				emojis.add(line);
				line=bReader.readLine();
			}
			bReader.close();
			
			bReader=new BufferedReader(new InputStreamReader(new FileInputStream(stopwordsfile),"utf-8"));
			line=bReader.readLine().trim();
			while(line!=null&&line!=""){
				stopWords.add(line);
				line=bReader.readLine();
			}
			bReader.close();
			System.out.println("-->词典加载完成");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run(){
		String folderPath="resource/lda/usermodel";
		File folder=new File(folderPath);
		if(!folder.exists()){
			return;
		}
		File[] files=folder.listFiles();
		for (File userFile : files) {
			File[] weibofiles=userFile.listFiles();
			for (File weibofile : weibofiles) {
				if(weibofile.getName().startsWith("weibo.dat")){
					processFile(weibofile.getPath(), userFile.getPath()+"/wbContent.txt");
				}
			}
			
		}
		//System.out.println("ok");
	}
	
	
	
	public void processFile(String inFile,String outFile){
		File weibofile=new File(inFile);
		File outfile=new File(outFile);
		BufferedReader bReader;
		String line;
		try {
			FileInputStream fis = new FileInputStream(weibofile);  
			UnicodeReader ur = new UnicodeReader(fis, "utf-8");  
			//BufferedReader br = new BufferedReader(ur);  
			
			bReader = new BufferedReader(ur);
			String filename=weibofile.getName();
			String lineCount=filename.substring(filename.indexOf("-")+1,filename.lastIndexOf("."));
			BufferedWriter bWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile)));
			bWriter.write(lineCount);
			bWriter.newLine();
			line=bReader.readLine();
			while(line!=null&&line!=""){
				
				bWriter.write(delStopWords(line.trim()).trim());
				bWriter.newLine();
				line=bReader.readLine();
			}
			bReader.close();
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
	
	
	
	public String delStopWords(String line){
		//line=Nlpir.NLPIR_ParagraphProcess(line, 0);
		String[] words=line.split(" ");
		String outstr="";
		boolean del=false;
		for(int i=0;i<words.length;i++){
			if(words[i]==""||words[i]==null||words[i]==" "){
				continue;
			}
			del=false;
			if(emojis.contains(words[i])){
				del=true;
			}else if(stopWords.contains(words[i])){
				del=true;
			}
			if(!del){
					outstr+=words[i]+" ";
			}
		}
		
		
		return outstr;
	}

}
