package preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import com.mchange.util.impl.LinkedListIntChecklistImpl;

public class LatgeFileHeader {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath="C:\\Users\\Rick\\Desktop\\微博数据\\全部数据\\weibo.xml";
		int lineCoune=100;
//		run(filePath,lineCoune);
		read(filePath, "utf8");
	}
	
	
	
	public static void run(String filePath,int lineCount){
		try {
			File f=new File(filePath);
			BufferedReader bReader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"utf8"));
			String line=bReader.readLine();
			int i=0;
			while(i<lineCount&&line!=null){
				System.out.println(line);
				line=bReader.readLine();
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
	
    /** 
     *  
     * @param filename 目标文件 
     * @param charset 目标文件的编码格式 
     */  
    public static void read(String filename, String charset) {  
  
        RandomAccessFile rf = null;  
        try {  
            rf = new RandomAccessFile(filename, "r");  
            long len = rf.length();  
            long start = rf.getFilePointer();  
            long nextend = start + len - 1;  
            String line;  
            rf.seek(nextend);  
            int c = -1;  
            int lineCount=0;
            while (nextend > start&&lineCount<1000) {  
                c = rf.read();  
                if (c == '\n' || c == '\r') {  
                    line = rf.readLine();  
                    if (line != null) {  
                        System.out.println(new String(line  
                                .getBytes("ISO-8859-1"), charset));  
                    } else {  
                        System.out.println(line);  
                    }  
                    nextend--;  
                }  
                nextend--;  
                rf.seek(nextend);  
                if (nextend == 0) {// 当文件指针退至文件开始处，输出第一行  
                    // System.out.println(rf.readLine());  
                    System.out.println(new String(rf.readLine().getBytes(  
                            "ISO-8859-1"), charset));  
                }  
                lineCount++;
            }  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                if (rf != null)  
                    rf.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    } 

}
