package index.lucene.demo;

import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.demo.SearchFiles;

public class LuceneDemo {

	public static void main(String[] args) {
//		String[] arg0={"-docs","C:\\Users\\Rick\\Desktop\\lucene","-index","C:\\Users\\Rick\\Desktop\\lucene\\test"};
//		IndexFiles.main(arg0);
		String[] arg0={"-index","C:\\Users\\Rick\\Desktop\\lucene\\test"};
		try {
			SearchFiles.main(arg0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
