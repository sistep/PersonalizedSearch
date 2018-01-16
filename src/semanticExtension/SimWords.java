package semanticExtension;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import semanticExtension.hownet.WordSimilarity;
/**
 * 根据HowNet获取相似词
 * @author Rick
 *
 */
public class SimWords {
	
	private static double simThreshold=0.8;//相似阈值
	private static int maxNum=10;//最大词数
	
	public static double getSimThreshold() {
		return simThreshold;
	}

	public static void setSimThreshold(double simThreshold) {
		SimWords.simThreshold = simThreshold;
	}

	public static int getMaxNum() {
		return maxNum;
	}

	public static void setMaxNum(int maxNum) {
		SimWords.maxNum = maxNum;
	}

	/**
	 * 获取相似的词
	 * @param word
	 * @return 返回相似词map<词，相似度>
	 */
	public static Map<String, Double> getSimWords(String word){
		Map<String, Double> words=new HashMap<>();
    	HashMap<Double, ArrayList<String>>simValue = new HashMap<>();
		for(String key : WordSimilarity.getWords())
		{
			Double value = WordSimilarity.simWord(word, key);
			if(!simValue.containsKey(value))
			{
				simValue.put(value, new ArrayList<String>());
			}
			simValue.get(value).add(key);
		}
		ArrayList<Double> values = new ArrayList<>();
		for(double item : simValue.keySet().toArray(new Double[1]))
		{
			values.add(item);
		}
		Collections.sort(values, Collections.reverseOrder());
		int count = 0;
		for(Double item : values)
		{
			if(item<simThreshold){
				break;
			}
			for(String simWord : simValue.get(item))
			{
				if(!simWord.equals(word)){
					words.put(simWord, item);
					count += 1;
					if(count >= simThreshold)break;
				}
			
			}
			
		}
		return words;
	}

	public static void main(String[] args) {
		
	}

}
