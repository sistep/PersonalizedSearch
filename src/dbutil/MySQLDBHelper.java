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

/**
 * 数据库相关操作
 * 
 * @author Jia Zheng
 */
public class MySQLDBHelper {

	/**
	 * 执行不带参数的数据库查询操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            查询的sql语句
	 * @return 查询结果ResulSet
	 */

	private static String dbName;

	public static String getDbName() {
		return dbName;
	}

	public static void setDbName(String dbName) {
		MySQLDBHelper.dbName = dbName;
	}

	public static ResultSet ExecuteQuery(Connection conn, String sql) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			System.out.println("数据库查询出错！" + e.toString());
			System.out.println("-->sql:"+sql);
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
	public static ResultSet ExecuteQuery(Connection conn, String sql, Object[] params) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			pstmt.setFetchSize(50);
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
	 * 执行不带参数的数据库更新操作
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            更新的sql语句
	 * @return 更新操作影响的行数
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

	/**
	 * 
	 * @param dbName
	 * @param conn
	 */
	public static void insertUserID(String dbName,String userid, Connection conn){
		String sql="select * from "+dbName+" where id=?";
		Object[] params = { userid };
		ResultSet ResultSet = ExecuteQuery(conn, sql, params);
		try {
			if(!ResultSet.next()){
				sql="insert into "+dbName+" values(?)";
				ExecuteUpdate(conn, sql, params);
				System.out.println("-->插入一个用户id");
			}else{
				System.out.println("-->id已存在"+userid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取微博数据库中的全部微博数据
	 * 
	 * @param dbName
	 * @param conn
	 * @return
	 */
	public static List<WeiboModel> getWeiboList(String dbName, Connection conn) {
		List<WeiboModel> weiboList = new ArrayList<>();
		String sql = "select * from " + dbName;
		
		try {
			ResultSet weibos = ExecuteQuery(conn, sql);
			int i=0;
			while (weibos.next()) {
				i++;
				WeiboModel aweibo = new WeiboModel();
				aweibo.setId(weibos.getString("id"));
				aweibo.setUserid(weibos.getString("userid"));
				aweibo.setContent(weibos.getString("content"));
				aweibo.setLikes(weibos.getString("likes"));
				aweibo.setTransfers(weibos.getString("transfers"));
				aweibo.setTime(weibos.getString("time"));
				aweibo.setPlatform(weibos.getString("platform"));
				aweibo.setRepostusers(weibos.getString("repostusers"));
				aweibo.setComments(weibos.getString("commentusers"));
				
				
				weiboList.add(aweibo);
				
			}
			System.out.println("读取数据"+i+"条："+dbName);
		} catch (Exception e) {
			System.out.println("查询微博数据出错：" + dbName);
			System.out.println(e);
		}
		return weiboList;
	}
	
	/**
	 * 获取{dbname1-dbname2}的数据
	 * @param dbName1
	 * @param dbName2
	 * @param conn
	 * @return
	 */
	public static List<WeiboModel> getRemainWeiboList(String dbName1, String dbName2, Connection conn) {
		List<WeiboModel> weiboList = new ArrayList<>();
		String sql = "select * from " + dbName1+" where id not in (select id from "+dbName2+" )";
		
		try {
			ResultSet weibos = ExecuteQuery(conn, sql);
			int i=0;
			while (weibos.next()) {
				i++;
				WeiboModel aweibo = new WeiboModel();
				aweibo.setId(weibos.getString("id"));
				aweibo.setUserid(weibos.getString("userid"));
				aweibo.setContent(weibos.getString("content"));
				aweibo.setLikes(weibos.getString("likes"));
				aweibo.setTransfers(weibos.getString("transfers"));
				aweibo.setTime(weibos.getString("time"));
				aweibo.setPlatform(weibos.getString("platform"));
				aweibo.setRepostusers(weibos.getString("repostusers"));
				aweibo.setComments(weibos.getString("commentusers"));
				if (weibos.getString("type") != null && weibos.getString("type") != "") {
					aweibo.setType("repost");
				} else {
					aweibo.setType("post");
				}
				weiboList.add(aweibo);
				
			}
			System.out.println("读取数据"+i+"条："+dbName1);
		} catch (Exception e) {
			System.out.println("查询微博数据出错：" + dbName1);
		}
		return weiboList;
	}

	/**
	 * 插入分词后的微博数据
	 * @param dbName
	 * @param conn
	 * @param weibo
	 * @return
	 */
	public static int insertWeiboCorpus(String dbName, Connection conn, WeiboModel weibo) {
		int insert=0;
		try {

			
				String selsql = "select * from " + dbName + " where id = ?";
				Object[] selPars = { weibo.getId() };
				ResultSet idResultSet = ExecuteQuery(conn, selsql, selPars);
				
				if (idResultSet.next()) {
					String delsql="update "+dbName+" set userid=?,content=?,topic=?,theme=?,reminds=?,url=?,likes=?,transfers=?,comments=?,time=?,platform=?,repostusers=?,commentusers=?,type=? where id=?";
					Object[] updPars = { 
							
							weibo.getUserid(), 
							weibo.getContent(),
							weibo.getTopic(),
							weibo.getTheme(),
							weibo.getReminds(),
							weibo.getUrl(),
							weibo.getLikes(),
							weibo.getTransfers(), 
							weibo.getComments(), 
							weibo.getTime(), 
							weibo.getPlatform(),
							weibo.getRepostusers(), 
							weibo.getCommentusers(),
							weibo.getType(),
							weibo.getId()
							};
					insert=ExecuteUpdate(conn, delsql, updPars);
				}else{
					String sql="insert into "+dbName+" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					Object[] insPars = { 
							weibo.getId(), 
							weibo.getUserid(), 
							weibo.getContent(),
							weibo.getTopic(),
							weibo.getTheme(),
							weibo.getReminds(),
							weibo.getUrl(),
							weibo.getLikes(),
							weibo.getTransfers(), 
							weibo.getComments(), 
							weibo.getTime(), 
							weibo.getPlatform(),
							weibo.getRepostusers(), 
							weibo.getCommentusers(),
							weibo.getType()
							};
					insert=ExecuteUpdate(conn, sql, insPars);
				}
				idResultSet.close();
			

		} catch (Exception e) {
		
		}
		return insert;
	}

	/**
	 * 插入基本信息
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserUserBasicInfor(Connection connection, UserModel userModel) {
		int inserts = 0;
		String selectSql = "select * from user where id = ?";
		Object[] selectPars = { userModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (!selects.next()) {
				String insertSql = "insert into user(id,nickname,birthday,gender,marriage,address,signature,homeUrl,profile) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
				Object[] insertPars = { userModel.getId(), userModel.getNickname(), userModel.getBirthday(),
						userModel.getGender(), userModel.getMarriage(), userModel.getAddress(),
						userModel.getSignature(), userModel.getHomeUrl(), userModel.getProfile() };
				inserts = ExecuteUpdate(connection, insertSql, insertPars);
				System.out.println("插入1个用户！");
			} else {
				String updateSql = "update user set nickname = ?,birthday= ?, gender= ?, marriage= ?, address= ?, signature= ?, homeUrl= ?, profile= ? where id = ?";
				Object[] updatePars = { userModel.getNickname(), userModel.getBirthday(), userModel.getGender(),
						userModel.getMarriage(), userModel.getAddress(), userModel.getSignature(),
						userModel.getHomeUrl(), userModel.getProfile(), userModel.getId() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("更新1个用户的基本信息！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入标签信息
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserUserTagsInfor(Connection connection, UserModel userModel) {
		int inserts = 0;
		String selectSql = "select * from user where id = ?";
		Object[] selectPars = { userModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String updateSql = "update user set tags = ? where id = ?";
				Object[] updatePars = { userModel.getTags(), userModel.getId() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("更新1个用户的标签信息！");
			} else {
				String updateSql = "insert into user (id, tags) values (?, ?)";
				Object[] updatePars = { userModel.getId(), userModel.getTags() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1个用户！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入微博数目、关注数目、粉丝数目
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserUserNumInfor(Connection connection, UserModel userModel) {
		int inserts = 0;
		String selectSql = "select * from user where id = ?";
		Object[] selectPars = { userModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String updateSql = "update user set weibo_num = ?, follows_num = ?, fans_num = ? where id = ?";
				Object[] updatePars = { userModel.getWeiboNum(), userModel.getFollowsNum(), userModel.getFansNum(),
						userModel.getId() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("更新1个用户的微博数目、粉丝数目、关注者数目！");
			} else {
				String updateSql = "insert into user (id, weibo_num, follows_num,fans_num) values (?, ?, ?, ?)";
				Object[] updatePars = { userModel.getId(), userModel.getWeiboNum(), userModel.getFollowsNum(),
						userModel.getFansNum() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1个用户！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入粉丝
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserUserFansInfor(Connection connection, UserModel userModel) {
		int inserts = 0;
		String selectSql = "select * from user where id = ?";
		Object[] selectPars = { userModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String selectSqls = "select fans from user where id = ?";
				ResultSet selectFans = ExecuteQuery(connection, selectSqls, selectPars);
				if (selectFans.next()) {
					String fans = selectFans.getString(1);
					if (fans == null || fans == "") {
						String updateSql = "update user set fans = ? where id = ?";
						Object[] updatePars = { userModel.getFans(), userModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1个用户的粉丝！");
					}
					if (!fans.contains(userModel.getFans())) {
						String newFans = fans + "," + userModel.getFans();
						String updateSql = "update user set fans = ? where id = ?";
						Object[] updatePars = { newFans, userModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1个用户的粉丝！");
					}
				} else {
					String updateSql = "update user set fans = ? where id = ?";
					Object[] updatePars = { userModel.getFans(), userModel.getId() };
					inserts = ExecuteUpdate(connection, updateSql, updatePars);
					System.out.println("更新1个用户的粉丝！");
				}
			} else {
				String updateSql = "insert into user (id, fans) values (?, ?)";
				Object[] updatePars = { userModel.getId(), userModel.getFans() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1个用户！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入关注者
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserUserFollowsInfor(Connection connection, UserModel userModel) {
		int inserts = 0;
		String selectSql = "select * from user where id = ?";
		Object[] selectPars = { userModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String selectSqls = "select follows from user where id = ?";
				ResultSet selectFollows = ExecuteQuery(connection, selectSqls, selectPars);
				if (selectFollows.next()) {
					String follows = selectFollows.getString(1);
					if (follows == null || follows == "") {
						String updateSql = "update user set follows = ? where id = ?";
						Object[] updatePars = { userModel.getFollows(), userModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1个用户的关注者！");
					} else if (!follows.contains(userModel.getFollows())) {
						String newFollows = follows + "," + userModel.getFollows();
						String updateSql = "update user set follows = ? where id = ?";
						Object[] updatePars = { newFollows, userModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1个用户的关注者！");
					}
				} else {
					String updateSql = "update user set follows = ? where id = ?";
					Object[] updatePars = { userModel.getFollows(), userModel.getId() };
					inserts = ExecuteUpdate(connection, updateSql, updatePars);
					System.out.println("更新1个用户的关注者！");
				}
			} else {
				String updateSql = "insert into user (id, follows) values (?, ?)";
				Object[] updatePars = { userModel.getId(), userModel.getFollows() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1个用户！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
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
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (!selects.next()) {
				String insertSql = "insert into " + dbName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
				Object[] insertPars = { weibo.getId(), weibo.getUserid(), weibo.getContent(),
						weibo.getLikes(), weibo.getTransfers(), weibo.getComments(),
						weibo.getTime(), weibo.getPlatform(), weibo.getRepostusers(),
						weibo.getCommentusers(),weibo.getType() };
				inserts = ExecuteUpdate(connection, insertSql, insertPars);
				System.out.println("插入1条微博！");
			} else {
				String updateSql = "update " + dbName
						+ " set userid =?,content =?,likes=?,transfers=?,comments=?,time=?,platform=?,type=? where id = ?";
				Object[] insertPars = { weibo.getUserid(), weibo.getContent(), weibo.getLikes(),
						weibo.getTransfers(), weibo.getComments(), weibo.getTime(),
						weibo.getPlatform(),weibo.getType(), weibo.getId() };
				inserts = ExecuteUpdate(connection, updateSql, insertPars);
				System.out.println("更新1条微博的基本信息！");
			}
			selects.close();
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入转发者
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserWeiboRepostUsers(Connection connection, WeiboModel weiboModel) {
		int inserts = 0;
		String selectSql = "select * from " + dbName + " where id = ?";
		Object[] selectPars = { weiboModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String selectSqls = "select repostusers from " + dbName + " where id = ?";
				ResultSet selectRepostUsers = ExecuteQuery(connection, selectSqls, selectPars);
				if (selectRepostUsers.next()) {
					String repostUsers = selectRepostUsers.getString(1);
					if (repostUsers == null || repostUsers == "") {
						String updateSql = "update " + dbName + " set repostusers = ? where id = ?";
						Object[] updatePars = { weiboModel.getRepostusers(), weiboModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1条微博的转发者！");
					} else if (!repostUsers.contains(weiboModel.getRepostusers())) {
						String newRepostUsers = repostUsers + "," + weiboModel.getRepostusers();
						String updateSql = "update " + dbName + " set repostusers = ? where id = ?";
						Object[] updatePars = { newRepostUsers, weiboModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1条微博的转发者！");
					}
				} else {
					String updateSql = "update " + dbName + " set repostusers = ? where id = ?";
					Object[] updatePars = { weiboModel.getRepostusers(), weiboModel.getId() };
					inserts = ExecuteUpdate(connection, updateSql, updatePars);
					System.out.println("更新1条微博的转发者！");
				}
			} else {
				String updateSql = "insert into " + dbName + " (id, repostusers) values (?, ?)";
				Object[] updatePars = { weiboModel.getId(), weiboModel.getRepostusers() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1条微博！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}

	/**
	 * 插入评论者
	 * 
	 * @param connection
	 * @param userModel
	 * @return
	 */
	public static int inserWeiboCommentUsers(Connection connection, WeiboModel weiboModel) {
		int inserts = 0;
		String selectSql = "select * from " + dbName + " where id = ?";
		Object[] selectPars = { weiboModel.getId() };
		try {
			ResultSet selects = ExecuteQuery(connection, selectSql, selectPars);
			if (selects.next()) {
				String selectSqls = "select commentusers from " + dbName + " where id = ?";
				ResultSet selectCommentUsers = ExecuteQuery(connection, selectSqls, selectPars);
				if (selectCommentUsers.next()) {
					String commentUsers = selectCommentUsers.getString(1);
					if (commentUsers == null || commentUsers == "") {
						String updateSql = "update " + dbName + " set commentusers = ? where id = ?";
						Object[] updatePars = { weiboModel.getCommentusers(), weiboModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1条微博的评论者！");
					} else if (!commentUsers.contains(weiboModel.getCommentusers())) {
						String newCommentUsers = commentUsers + "," + weiboModel.getCommentusers();
						String updateSql = "update " + dbName + " set commentusers = ? where id = ?";
						Object[] updatePars = { newCommentUsers, weiboModel.getId() };
						inserts = ExecuteUpdate(connection, updateSql, updatePars);
						System.out.println("更新1条微博的评论者！");
					}
				} else {
					String updateSql = "update " + dbName + " set commentusers = ? where id = ?";
					Object[] updatePars = { weiboModel.getCommentusers(), weiboModel.getId() };
					inserts = ExecuteUpdate(connection, updateSql, updatePars);
					System.out.println("更新1条微博的评论者！");
				}
			} else {
				String updateSql = "insert into " + dbName + " (id, commentusers) values (?, ?)";
				Object[] updatePars = { weiboModel.getId(), weiboModel.getCommentusers() };
				inserts = ExecuteUpdate(connection, updateSql, updatePars);
				System.out.println("插入1条微博！");
			}
		} catch (Exception sqlE) {
			// TODO Auto-generated catch block
			System.out.println("查询出错");
		}
		return inserts;
	}
}