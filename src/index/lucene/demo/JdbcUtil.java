package index.lucene.demo;

import java.sql.Connection;     
import java.sql.DriverManager;       
import java.sql.SQLException;    
/**     
* JdbcUtil.java   
* @version 1.0   
* @createTime JDBC获取Connection工具类   
*/    
public class JdbcUtil {     
    private static Connection conn = null;     
    private static final String URL ="jdbc:mysql://localhost:3306/sina?useSSL=false&rewriteBatchedStatements=true"; 
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";     
    private static final String USER_NAME = "root";     
    private static final String PASSWORD = "123456";  
//    public final static String DRIVER = "com.mysql.jdbc.Driver"; // 数据库驱动  
//    public final static String URL = "jdbc:mysql://localhost:3306/sina?useSSL=false&rewriteBatchedStatements=true";  //useUnicode=true&characterEncoding=UTF-8&
//    public final static String DBUSER = "root";   // 数据库用户名  
//    public final static String DBPASS = "123456"; // 数据库密码  
//      
    public static Connection getConnection() {     
        try {     
            Class.forName(JDBC_DRIVER);     
            conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);     
        } catch (ClassNotFoundException e) {     
            e.printStackTrace();     
        } catch (SQLException e) {     
            e.printStackTrace();     
        }     
        return conn;     
    }  
}    