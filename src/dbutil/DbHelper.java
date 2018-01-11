package dbutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.UserModel;
import model.WeiboModel;


public class DbHelper {

	private static String dbName;

	public static String getDbName() {
		return dbName;
	}

	public static void setDbName(String dbName) {
		DbHelper.dbName = dbName;
	}

	/**
	 * 不带参数的查询
	 * @param stmt
	 * @param sql
	 * @return
	 */
	public static ResultSet ExecuteQuery(Statement stmt, String sql) {
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
			System.out.println("-->sql:"+sql);
		}
		return rs;
	}
	
	/**
	 * 不带参数的数据库查询。批量读取。
	 * @param pstmt
	 * @param sql
	 * @return
	 */
	public static ResultSet ExecuteQuery(PreparedStatement pstmt,String sql) {
		ResultSet rs = null;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
			System.out.println("-->sql:"+sql);
		}
		return rs;
	}


	/**
	 * 带参数查询
	 * @param pstmt
	 * 
	 * @param params
	 * @return
	 */
	public static ResultSet ExecuteQuery(PreparedStatement pstmt,Object[] params) {
		
		ResultSet rs = null;
		try {
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
			System.out.println(new Date());
			for (Object par : params) {
				System.out.print("-->"+par.toString());
				System.out.println();
			}
		}
		return rs;
	}

	/**
	 * 不带参数更新
	 * @param conn
	 * @param sql
	 * @return
	 */
	public static int ExecuteUpdate(Connection conn, String sql) {
		Statement stmt = null;
		int count = 0;
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			System.out.println("数据库更新出错！" + e.toString());
			System.out.println("-->sql:"+sql);
		}
		return count;
	}


	/**
	 * 带参数更新
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int ExecuteUpdate(Connection conn, String sql, Object[] params) {
		PreparedStatement pstmt = null;
		int count = 0;
		try {
			pstmt = conn.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			count = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("数据库更新出错！" + e.toString());
			System.out.println(new Date());
			for (Object par : params) {
				System.out.print("-->"+par.toString());
				System.out.println();
			}
		}
		return count;
	}
	
	
	
	public static int updateTagInfo(Connection connection, UserModel userModel) {
		int inserts = 0;
		try {
			
				String updateSql = "update "+dbName+" set tags = ? where id = ?";
				Object[] updatePars = { userModel.getTags(), userModel.getId() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("更新1个用户的标签信息！");
			
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("更新标签信息出错");
			System.out.println("dbname="+dbName+"  userid="+userModel.getId());
		}
		return inserts;
	}
	
	
	/**
	 * 插入微博
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int insertWeiboInfo(Connection connection, WeiboModel weibo) {
		int inserts = 0;
		String selectSql = "select * from " + dbName + " where id = ?";
		Object[] selectPars = { weibo.getId() };
		try {
			PreparedStatement pstmtSel=connection.prepareStatement(selectSql);
			ResultSet selects = ExecuteQuery(pstmtSel, selectPars);
			if (!selects.next()) {
				String insertSql = "insert into " + dbName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
				Object[] insertPars = { weibo.getId(), weibo.getUserid(), weibo.getContent(),
						weibo.getLikes(), weibo.getTransfers(), weibo.getComments(),
						weibo.getTime(), weibo.getPlatform(), weibo.getRepostusers(),
						weibo.getCommentusers(),weibo.getType() };
				inserts = ExecuteUpdate(connection, insertSql, insertPars);
				System.out.println("插入1条微博！");
			} else {
//				String updateSql = "update " + dbName
//						+ " set userid =?,content =?,likes=?,transfers=?,comments=?,time=?,platform=?,type=? where id = ?";
//				Object[] insertPars = { weibo.getUserid(), weibo.getContent(), weibo.getLikes(),
//						weibo.getTransfers(), weibo.getComments(), weibo.getTime(),
//						weibo.getPlatform(),weibo.getType(), weibo.getId() };
//				inserts = ExecuteUpdate(connection, updateSql, insertPars);
//				System.out.println("更新1条微博的基本信息！");
			}
			selects.close();
			pstmtSel.close();
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}
	
	
}
