package index.lucene.demo;


import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.document.Field.TermVector;     
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.queryParser.QueryParser;  
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

//import org.wltea.analyzer.lucene.IKSimilarity; 

import model.UserModel;

public class LuceneSql {

	private static Connection conn = null;     
    private static Statement stmt = null;     
    private static  ResultSet rs = null;     
    private String searchDir = "F:\\Test\\Index";     
    private static File indexFile = null;     
    private static IndexSearcher searcher = null;     
    private static Analyzer analyzer = null;     
    /** 索引页面缓冲 */    
    private int maxBufferedDocs = 500;     
    /**   
    * 获取数据库数据   
    * @return ResultSet   
    * @throws Exception   
    */    
    public List<UserModel> getResult(String queryStr) throws Exception {     
        List<UserModel> result = null;     
        conn = JdbcUtil.getConnection();   
       
        if(conn == null) {     
            throw new Exception("数据库连接失败！");     
        }     
        String sql = "select id, homeurl from user_tmp";     
        try {     
            stmt = conn.createStatement();     
            rs = stmt.executeQuery(sql);     
//            this.createIndex(rs);   //给数据库创建索引,此处执行一次，不要每次运行都创建索引，以后数据有更新可以后台调用更新索引     
            TopDocs topDocs = this.search(queryStr);     
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;     
            result = this.addHits2List(scoreDocs);     
        } catch(Exception e) {     
            e.printStackTrace();     
            throw new Exception("数据库查询sql出错！ sql : " + sql);     
        } finally {     
            if(rs != null) rs.close();     
            if(stmt != null) stmt.close();     
            if(conn != null) conn.close();     
        }              
        return result;     
    }     
  
/**   
* 为数据库检索数据创建索引   
* @param rs   
* @throws Exception   
*/    
    private void createIndex(ResultSet rs) throws Exception {     
        Directory directory = null;     
        IndexWriter indexWriter = null;     
         
        try {     
            indexFile = new File(searchDir);     
            if(!indexFile.exists()) {     
                indexFile.mkdir();     
            }     
            directory = FSDirectory.open(indexFile.toPath());     
//            analyzer = new StandardAnalyzer();   
            analyzer=new IKAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig( analyzer);
            config.setMaxBufferedDocs(maxBufferedDocs);
            indexWriter = new IndexWriter(directory,config);     
            Document doc = null;     
            while(rs.next()) {     
                doc = new Document();     
//                Field id = new Field("id", String.valueOf(rs.getInt("id")), Field.Store.YES, Field.Index.NOT_ANALYZED, TermVector.NO);     
//                Field username = new Field("nickname", rs.getString("nickname") == null ? "" : rs.getString("nickname"), Field.Store.YES,Field.Index.ANALYZED, TermVector.NO);     
                
//                Field id = new Field("id", String.valueOf(rs.getInt("id")));     
//                Field username = new Field("nickname", rs.getString("nickname") == null ? "" : rs.getString("nickname")); 
                Field id=new StringField("id", rs.getString("id"),Field.Store.YES);
                Field homeurl=new StringField("homeurl",  rs.getString("homeurl") == null ? "" : rs.getString("homeurl"),Field.Store.YES);
                doc.add(id);     
                doc.add(homeurl);     
                indexWriter.addDocument(doc);     
            }     
                         
//            indexWriter.optimize();     
            indexWriter.close();     
        } catch(Exception e) {     
            e.printStackTrace();     
        }      
    }     
   
    /**   
    * 搜索索引   
    * @param queryStr   
    * @return   
    * @throws Exception   
    */    
    private TopDocs search(String queryStr) throws Exception {    
    	 DirectoryReader ireader=null;
        if(searcher == null) {     
        	
            indexFile = new File(searchDir);    
            Directory directory=FSDirectory.open(indexFile.toPath());
            ireader = DirectoryReader.open(directory);
            searcher = new IndexSearcher(ireader);       
        }     
//        searcher.setSimilarity(new Similarity());     
        QueryParser parser = new QueryParser("homeurl",new IKAnalyzer());     
        Query query = parser.parse(queryStr);  
          
        TopDocs topDocs = searcher.search(query, ireader.maxDoc());     
        return topDocs;     
    }  
      
    /**   
    * 返回结果并添加到List中   
    * @param scoreDocs   
    * @return   
    * @throws Exception   
    */    
    private List<UserModel> addHits2List(ScoreDoc[] scoreDocs ) throws Exception {     
        List<UserModel> listBean = new ArrayList<UserModel>();     
        UserModel bean = null;     
        for(int i=0 ; i<scoreDocs.length; i++) {     
            int docId = scoreDocs[i].doc;     
            Document doc = searcher.doc(docId);     
            bean = new UserModel();     
            bean.setId(doc.get("id"));     
            bean.setNickname(doc.get("homeurl"));
            listBean.add(bean);     
        }     
        return listBean;     
    }  
      
    public static void main(String[] args) {     
        LuceneSql logic = new LuceneSql();     
        try {     
            Long startTime = System.currentTimeMillis();     
            List<UserModel> result = logic.getResult("http://weibo.cn/1000124571");     
            int i = 0;     
            for(UserModel bean : result) {     
                if(i == 10)   
                    break;     
                System.out.println("bean.name " + bean.getClass().getName() + " : bean.id " + bean.getId()+ " : bean.homeurl " + bean.getNickname());   
                i++;     
            }  
              
            System.out.println("searchBean.result.size : " + result.size());     
            Long endTime = System.currentTimeMillis();     
            System.out.println("查询所花费的时间为：" + (endTime-startTime)/1000);     
        } catch (Exception e) {   
            e.printStackTrace();     
            System.out.println(e.getMessage());     
        }     
    }     

}
