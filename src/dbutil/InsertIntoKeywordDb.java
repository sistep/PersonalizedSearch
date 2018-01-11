package dbutil;

import model.WeiboModel;

public class InsertIntoKeywordDb {

	public void run(String dbName){
		MySqlDBHelperStream dbhIns=new MySqlDBHelperStream();
		String sqlIns="insert into "+dbName+" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		dbhIns.init(sqlIns);
	
		
		WeiboModel aweibo=new WeiboModel();
		
		Object[] insPars = { 
				aweibo.getId(), 
				aweibo.getUserid(), 
				aweibo.getContent(),
				aweibo.getTopic(),
				aweibo.getTheme(),
				aweibo.getReminds(),
				aweibo.getUrl(),
				aweibo.getLikes(),
				aweibo.getTransfers(), 
				aweibo.getComments(), 
				aweibo.getTime(), 
				aweibo.getPlatform(),
				aweibo.getRepostusers(), 
				aweibo.getCommentusers(),
				aweibo.getType()
				};
	
		dbhIns.ExecuteUpdate(insPars);
		
		dbhIns.commit();
		dbhIns.close();
	}

}
