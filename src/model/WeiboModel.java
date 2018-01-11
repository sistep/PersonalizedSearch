package model;

public class WeiboModel {
	String id;
	String userid;
	String content;
	String likes;
	String transfers;
	String comments;
	String time;
	String platform;
	String repostusers;
	String commentusers;
	String type;
	String theme;
	String topic;
	String reminds;
	String url;
	
	public String toString() {
		StringBuilder weiboStr=new StringBuilder("-->weibo:\n");
		weiboStr.append("id="+getId()+"\n");
		weiboStr.append("userid="+getUserid()+"\n");
		weiboStr.append("content="+getContent()+"\n");
		weiboStr.append("likes="+getLikes()+"\n");
		weiboStr.append("transfers="+getTransfers()+"\n");
		weiboStr.append("comments="+getComments()+"\n");
		weiboStr.append("times="+getTime()+"\n");
		weiboStr.append("platform="+getPlatform()+"\n");
		weiboStr.append("type="+getType()+"\n");
		
		return weiboStr.toString();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLikes() {
		return likes;
	}
	public void setLikes(String likes) {
		this.likes = likes;
	}
	public String getTransfers() {
		return transfers;
	}
	public void setTransfers(String transfers) {
		this.transfers = transfers;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getRepostusers() {
		return repostusers;
	}
	public void setRepostusers(String repostusers) {
		this.repostusers = repostusers;
	}
	public String getCommentusers() {
		return commentusers;
	}
	public void setCommentusers(String commentusers) {
		this.commentusers = commentusers;
	}
	public String getType(){
		return this.type;
	}
	public void setType(String type){
		this.type=type;
	}
	public String getTheme(){
		return this.theme;
	}
	public void setTheme(String theme){
		this.theme=theme;
	}
	public String getTopic(){
		return this.topic;
	}
	public void setTopic(String topic){
		this.topic=topic ;
	}
	public String getReminds(){
		return this.reminds;
	}
	public void setReminds(String reminds){
		this.reminds=reminds;
	}
	public String getUrl(){
		return this.url;
	}
	public void setUrl(String url){
		this.url=url;
	}
}
