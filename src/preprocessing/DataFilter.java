package preprocessing;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.generic.Select;

import crawler.WeiboCN;
import dbutil.MySQLConnManager;
import dbutil.MySQLDBHelper;
import model.UserModel;

public class DataFilter {

	public static void main(String[] args) {
		userWeibo();
		System.out.println("ok");
	}
	
	public static void userWeibo(){
		Connection con=MySQLConnManager.creatConnection();
		String sql="select id,follows from user_m";
		ResultSet rsetUserids=MySQLDBHelper.ExecuteQuery(con, sql);
		List<UserModel> users=new ArrayList<>();
		try {
			while (rsetUserids.next()){
				UserModel auser=new UserModel();
				auser.setId(rsetUserids.getString("id"));
				auser.setFollows(rsetUserids.getString("follows"));
				users.add(auser);
			}
			rsetUserids.close();
			for (UserModel auser : users) {
				File modelFolder=new File("resource/lda/usermodel/"+auser.getId());
				System.out.println("-->userid:"+auser.getId());
				if(!modelFolder.exists()){
					modelFolder.mkdirs();
				}
				File weiboFile=new File(modelFolder.getPath()+"/weibo");
				
				BufferedWriter bWriter=new BufferedWriter(new FileWriter(weiboFile.getPath()));
				int weiboCount=0;
				PreparedStatement pstm;
				sql="select * from weibo_m where userid=?";
				pstm=con.prepareStatement(sql);
				pstm.setObject(1, auser.getId());
				ResultSet rsetWeibo=pstm.executeQuery();
				while(rsetWeibo.next()){
					weiboCount++;
//					String line=rsetWeibo.getString("topic")
//							+" "+rsetWeibo.getString("theme")
//							+" "+rsetWeibo.getString("content");
					String line=rsetWeibo.getString("content");
					bWriter.write(line);
					bWriter.newLine();
				}
				System.out.println("-->postweibo:"+weiboCount);
				bWriter.flush();
				rsetWeibo.close();
				String[] follows=auser.getFollows().split(",");
				sql="select * from weibo_m_follow where userid=?";
				pstm=con.prepareStatement(sql);
				for (String id : follows) {
					pstm.setString(1, id);
					ResultSet rsetWeiboFollow=pstm.executeQuery();
					while(rsetWeiboFollow.next()){
						weiboCount++;
//						String line=rsetWeiboFollow.getString("topic")
//								+" "+rsetWeiboFollow.getString("theme")
//								+" "+rsetWeiboFollow.getString("content");
						String line=rsetWeiboFollow.getString("content");
						bWriter.write(line);
						bWriter.newLine();
					}
					bWriter.flush();
					rsetWeiboFollow.close();
				}
				bWriter.close();
				System.out.println("-->weibocount:"+weiboCount);
				weiboFile.renameTo(new File(weiboFile.getPath()+"-"+weiboCount+".txt"));
//				sql="select * from weibo_m where userid = ?";
//				PreparedStatement pstm=con.prepareStatement(sql);
//				
//				ResultSet rsetWeibo=pstm.executeQuery();
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void run(){
		Connection con=MySQLConnManager.creatConnection();
		String sql="select id from user where weibo_num>200 and weibo_num<400";
		
		try {
			ResultSet rSet=MySQLDBHelper.ExecuteQuery(con, sql);
			int fileCount=0; 
			while(rSet.next()){
				
				String userid=rSet.getString("id");
				BufferedWriter bWriter;
				String sqlweibo="select time,content from weibo where userid="+userid;
				ResultSet weibo=MySQLDBHelper.ExecuteQuery(con, sqlweibo);
				System.out.println("-->"+userid);
				//File file=new File("resource/weibosent/"+userid+".txt");
				int weiboCount=0;
				if(weibo.next()){
					
					bWriter=new BufferedWriter(new FileWriter("resource/weibosent/"+userid+".txt"));
					do {
						String time=weibo.getString("time");
						String content=weibo.getString("content");
						bWriter.write(time+" "+content);
						bWriter.newLine();
						weiboCount++;
					} while (weibo.next());
					bWriter.flush();
					bWriter.close();
					fileCount++;
					if(fileCount>=100){
						break;
					}
				}
				System.out.println("-->weibocount:"+weiboCount);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
