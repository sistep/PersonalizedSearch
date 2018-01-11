package preprocessing;

import java.sql.ResultSet;
import java.sql.SQLException;

import dbutil.MySqlDBHelperStream;
import model.WeiboModel;

public class ContentTrim {

	public static void main(String[] args) {
		
		
//		
		runStream("weibo");
//		runStream("weibo_m");
//		runStream("weibo_m_follow");
//		System.out.println("ok");
//		

	}
	
	public static String deleteStrangeSymbols(String line){
		String nline=line;
		for(int i=line.length()-1;i>=0;i--){
			if(line.charAt(i)==8203||line.charAt(i)==32){
//				System.out.println("-->"+(int)line.charAt(i)+"-->"+line.charAt(i)+"<--");
				nline=nline.substring(0, i);
			}else{
				break;
			}
		}
		
		return nline;
	}
	
	public static void runStream(String dbName){
			System.out.println("-->dbName="+dbName);
		try {
			
			String selStr="select * from "+dbName;
			MySqlDBHelperStream dbhSelWeibo=new MySqlDBHelperStream();
			dbhSelWeibo.init(selStr);
			ResultSet weibos=dbhSelWeibo.ExecuteQuery();
			WeiboModel aweibo=new WeiboModel();
			MySqlDBHelperStream dbhUpd=new MySqlDBHelperStream();
			String updateSql = "update "+dbName+" set content = ? where id = ?";
			dbhUpd.init(updateSql);
			dbhUpd.setBatchSize(10000);
			String selectIdStr="select * from "+dbName+" where id = ?";
		
			MySqlDBHelperStream dbhSelId=new MySqlDBHelperStream();
			dbhSelId.init(selectIdStr);
			ResultSet selects=null;
			
			while(weibos.next()){
				aweibo.setId(weibos.getString("id"));
				aweibo.setUserid(weibos.getString("userid"));
				aweibo.setContent(weibos.getString("content"));
				aweibo.setLikes(weibos.getString("likes"));
				aweibo.setTransfers(weibos.getString("transfers"));
				aweibo.setTime(weibos.getString("time"));
				aweibo.setPlatform(weibos.getString("platform"));
				aweibo.setRepostusers(weibos.getString("repostusers"));
				aweibo.setComments(weibos.getString("commentusers"));
				if(isExistColumn(weibos, "type")){
				aweibo.setType(weibos.getString("type"));
			}else {
				aweibo.setType("post");
			}
			
				String line=aweibo.getContent();
				if(line.length()>=4){
					
					
					aweibo.setContent(deleteStrangeSymbols(line));
				}
				Object[] selectPars = { aweibo.getId() };
				selects = dbhSelId.ExecuteQuery( selectPars);
				if (selects.next()) {
					Object[] updatePars = { aweibo.getContent(),aweibo.getId()};
					dbhUpd.ExecuteUpdate(updatePars);
					
				} else {
					System.out.println("无法找到微博：");
					System.out.println("id="+aweibo.getId());
					System.out.println("content="+aweibo.getContent());
				}
				
				
			}
			selects.close();
			dbhSelId.close();
			dbhUpd.commit();
			dbhUpd.close();
			weibos.close();
			dbhSelWeibo.close();
	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 判断查询结果集中是否存在某列
	 * @param rs 查询结果集
	 * @param columnName 列名
	 * @return true 存在; false 不存在
	 */
	public static boolean isExistColumn(ResultSet rs, String columnName) {
		try {
			if (rs.findColumn(columnName) > 0 ) {
				return true;
			} 
		}
		catch (SQLException e) {
			return false;
		}
		
		return false;
	}
	


}
