package preprocessing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.safety.Whitelist;

import dbutil.MySQLConnManager;
import dbutil.MySQLDBHelper;
import dbutil.MySqlDBHelperStream;
import model.WeiboModel;
import nlpir.Nlpir;
import preprocessing.zhconvert.ZHConverter;

public class DataProprecessing {

	public static void main(String[] args) {
		DataProprecessing dp=new DataProprecessing();
		String sourceDB="weibo_tmp";
		String targetDB="weibo_tmp_p";
		
//		dp.run_streaming("weibo_m","weibo_m_p");
		dp.run_streaming("weibo","weibo_p");
		dp.run_streaming("weibo_m_follow","weibo_m_follow_p");
		
	}
	
	

	
	/**
	 * 流式读取流式插入
	 * @param sourceDB
	 * @param targetDB
	 */
	public void run_streaming(String sourceDB, String targetDB){
		System.out.println("-->running data preprocessing");
		System.out.println("-->sourceDB："+sourceDB);
		System.out.println("-->targetDB:"+targetDB);
		
		try {
			MySqlDBHelperStream dbhSelSource=new MySqlDBHelperStream();
			MySqlDBHelperStream dbhInsTarget=new MySqlDBHelperStream();
			String sqlSelSource = "select * from " + sourceDB +" where id not in (select id from "+targetDB+" )";
//			String sqlSelSource = "select * from " + sourceDB;
			String sqlInsTarget="insert into "+targetDB+" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			dbhSelSource.init(sqlSelSource);
			dbhInsTarget.init(sqlInsTarget);
			dbhInsTarget.setBatchSize(10000);
			ResultSet weibos= dbhSelSource.ExecuteQuery();
			while(weibos.next()){
				WeiboModel aweibo = new WeiboModel();
				aweibo.setId(weibos.getString("id"));
				aweibo.setUserid(weibos.getString("userid"));
				aweibo.setContent(weibos.getString("content"));
				aweibo.setLikes(weibos.getString("likes"));
				aweibo.setTransfers(weibos.getString("transfers"));
				aweibo.setTime(weibos.getString("time"));
				aweibo.setPlatform(weibos.getString("platform"));
				aweibo.setRepostusers(weibos.getString("repostusers"));
				aweibo.setComments(weibos.getString("comments"));
				if(isExistColumn(weibos, "type")){
					aweibo.setType(weibos.getString("type"));
				}else {
					aweibo.setType("post");
				}
				
				aweibo=preprocess(aweibo);
				
				
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
			
				dbhInsTarget.ExecuteUpdate(insPars);
				
			}
			
			dbhInsTarget.commit();
			dbhInsTarget.close();
			weibos.close();
			dbhSelSource.close();
			System.out.println("-->preprocessing done");
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
	public boolean isExistColumn(ResultSet rs, String columnName) {
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
	
	/**
	 * 流式数据库操作，读取原始微博数据，预处理后放入目标表
	 * @param sourceDB
	 * @param targetDB
	 */
	public void run_stream(String sourceDB,String targetDB){
		String sql = "select * from " + sourceDB +" where id not in (select id from "+targetDB+" )";
		
		Connection conn = MySQLConnManager.creatConnection();
		Connection conni = MySQLConnManager.creatConnection();
		
		try {
			PreparedStatement pStatement=conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			pStatement.setFetchSize(100);
			ResultSet weibos = pStatement.executeQuery();
			int i=0;
			
			
			while (weibos.next()) {
				i++;
				if(i%10000==0){
					System.out.println("-->"+new Date()+":"+i);
				}
			//	System.out.println("-->"+i+":"+weibos.getString("id"));
				WeiboModel aweibo = new WeiboModel();
				aweibo.setId(weibos.getString("id"));
				aweibo.setUserid(weibos.getString("userid"));
				aweibo.setContent(weibos.getString("content"));
				aweibo.setLikes(weibos.getString("likes"));
				aweibo.setTransfers(weibos.getString("transfers"));
				aweibo.setTime(weibos.getString("time"));
				aweibo.setPlatform(weibos.getString("platform"));
				aweibo.setRepostusers(weibos.getString("repostusers"));
				aweibo.setComments(weibos.getString("comments"));
				aweibo.setType("post");
				
				aweibo=preprocess(aweibo);
				if(!aweibo.getContent().equals(null)&&!aweibo.getContent().equals("")){
					MySQLDBHelper.insertWeiboCorpus(targetDB, conni, aweibo);
					//System.out.println(weibo.getId()+weibo.getContent());
				}
				
			}
			pStatement.close();
			conn.close();
			conni.close();
			
			System.out.println("ok");
			System.out.println("读取数据"+i+"条："+sourceDB);
		} catch (Exception e) {
			System.out.println("查询微博数据出错：" + sourceDB);
			System.out.println(e);
		}
	}

	/**
	 * 微博预处理，分词，提取信息，数据清洗
	 * @param weibo
	 * @return
	 */
	private WeiboModel preprocess(WeiboModel weibo) {
		List<String> meaningless = new ArrayList<>();
		meaningless.add("抱歉，此微博已被作者删除。查看帮助：http://t.cn/Rfd3rQV");
		meaningless.add("该微博因被多人举报，根据《微博社区管理规定》，已被删除。查看帮助：http://t.cn/Rfd14WY");
		meaningless.add("抱歉，由于作者设置，你暂时没有这条微博的查看权限哦。查看帮助：http://t.cn/RfdBWwP");
		String content = weibo.getContent();
		ZHConverter converter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);  
		content=converter.convert(content);
		boolean meanless = false;
		Pattern pattern;
		Matcher matcher;
		String theme = "";
		String topic = "";
		String reminds = "";
		String url = "";
		for (String regex : meaningless) {
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				meanless = true;
				break;
			}
		}
		if (meanless) {
			content = "";
		}else {
			
			
			
			
			String urlRegex = "http://t.cn/\\w{7}";
			String themeRegex = "^【.*?】";
			String topicRegex = "^#.*?#";
			String remindsRegex = "//";
			String interactRegex = "@.*?[ $,@:：]";
			String viaRegex = "via@.*?[ $,：:]";
			String questionRegex = "\\?\\?\\?";

			pattern = Pattern.compile(urlRegex);
			matcher = pattern.matcher(content);
			while (matcher.find()) {
				String str =matcher.group();
				// System.out.println(url);
				content = content.replace(str, "");
				url += str;
			}

			pattern = Pattern.compile(topicRegex);
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				topic = matcher.group(0);
				content = content.replace(topic, "").trim();
				topic = topic.substring(1, topic.length() - 1);
			}

			pattern = Pattern.compile(themeRegex);
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				theme = matcher.group(0);
				content = content.replace(theme, "");
				theme = theme.substring(1, theme.length() - 1);
			}

			pattern = Pattern.compile(questionRegex);
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				content = content.replaceAll(questionRegex, "");
			}

			pattern = Pattern.compile(viaRegex);
			matcher = pattern.matcher(content);
			if (matcher.find()) {
				String str = matcher.group();
				content = content.replace(str, "");
				reminds += str;
			}

			pattern = Pattern.compile(interactRegex);
			matcher = pattern.matcher(content);
			while (matcher.find()) {
				String str = matcher.group();
				content = content.replace(str, "");
				if(str.charAt(str.length()-1)=='@'){
					str=str.substring(0, str.length()-1);
				}
				content = content.replace(str, "");
			
				matcher = pattern.matcher(content);
				reminds += str;
			}
			
			content=content.trim();
			
			if(content.length()<=8){
				content="";
			}
		}
		
		
		content=Nlpir.NLPIR_ParagraphProcess(content, 0).trim();
		
		theme=Nlpir.NLPIR_ParagraphProcess(theme, 0).trim();
		topic=Nlpir.NLPIR_ParagraphProcess(topic, 0).trim();
		weibo.setUrl(url);
		weibo.setContent(content);
		weibo.setTheme(theme);
		weibo.setTopic(topic);
		weibo.setReminds(reminds);
		return weibo;
	}

}
