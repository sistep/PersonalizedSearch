package preprocessing;

public class WordSeg {
	
	public static void main(String[] args){
		String line="【 上海 举办 首 届 书法 艺术节   】 今年 9月 15 11月 15日 上海 举办 首 届 书法 艺术节 上海 书协 主席 周志高 介绍 说 中国 书法 中华 文化 审美 核心 要素 符号 海外 拥有 无数 拥 趸 独特 魅力 辐射 海外 联合国 秘书长 潘基文 前 外长 杨洁篪 四十 余 位 中外 外交官 书法 迷 交 助阵 附 三 人 书 作 先睹为快   ​​​";
		String[] words=line.split(" ");
		for (String word : words) {
			System.out.println(word);
		}
	}

}
