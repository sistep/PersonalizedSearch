package dbutil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lda.Model;
import model.WeiboModel;

public class MySqlDBHelperStream {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public MySqlDBHelperStream(){
		updateCount=0;
		conn=MySQLConnManager.creatConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private  int FETCH_SIZE=100;
	private  int BATCH_SIZE=1000;
	private int updateCount;
	private PreparedStatement pstmt;
	private Connection conn;
	
	
	
	public int getFetchSize(){
		return FETCH_SIZE;
	}
	
	public void setFetchSize(int fetchSize){
		FETCH_SIZE=fetchSize;
	}
	
	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public void setBatchSize(int batchSize) {
		BATCH_SIZE =batchSize;
	}
	
	public void resetUpdateCount(){
		updateCount=0;
		
	}
	
	public void init(String sql){
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setFetchSize(FETCH_SIZE);
			updateCount=0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void commit(){
		try {
			pstmt.executeBatch();
			System.out.println("-->updated "+updateCount+" rows  "+new Date());
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void close(){
		try {
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 执行不带参数的数据库查询操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            查询的sql语句
	 * @return 查询结果ResulSet
	 */
	public  ResultSet ExecuteQuery() {
		ResultSet rs = null;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
		}
		return rs;
	}

	/**
	 * 执行带参数的数据库查询操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            查询的sql语句
	 * @param params
	 *            参数数组
	 * @return 查询结果ResulSet
	 */
	public  ResultSet ExecuteQuery( Object[] params) {
//		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			rs = pstmt.executeQuery();
//			pstmt.close();
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
		}
		return rs;
	}
	
	

	/**
	 * 执行不带参数的数据库更新操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            更新的sql语句
	 * @return 更新操作影响的行数
	 */
	public  int ExecuteUpdate() {
		updateCount++;
		
//		PreparedStatement pstmt=null;
		
		try {
			
			pstmt.addBatch();
			if(updateCount%BATCH_SIZE==0){
				pstmt.executeBatch();
				System.out.println("-->updated "+updateCount+" rows  "+new Date());
				conn.commit();
			}
//			pstmt.close();

		} catch (SQLException e) {
			System.out.println("数据库更新出错！" + e.toString());
		}
		return updateCount;
	}

	/**
	 * 执行带参数的数据库更新操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            更新的sql语句
	 * @param params
	 *            参数数组
	 * @return 更新操作影响的行数
	 */
	public  int ExecuteUpdate( Object[] params) {
		updateCount++;
//		PreparedStatement pstmt = null;
		int count = 0;
		try {
			
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			pstmt.addBatch();
			if(updateCount%BATCH_SIZE==0){
				pstmt.executeBatch();
				conn.commit();
				System.out.println("-->updated "+updateCount+" rows  "+new Date());
			}
//			pstmt.close();
		} catch (SQLException e) {
			System.out.println("数据库更新出错！" + e.toString());
			System.out.println(new Date());
			for (Object par : params) {
				if(par!=null){
					System.out.print("-->"+par.toString());
				}else{
					System.out.print("-->");
				}
				
				System.out.println();
			}
		}
		return count;
	}
	



}
