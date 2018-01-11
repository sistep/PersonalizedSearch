package lda;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LDAPredictor {

	private Inferencer inferencer;

	//////输入模型文件地址初始�?
	public LDAPredictor(String dir, String modelName) {
		LDAOption option = new LDAOption();
		
		option.dir = dir;
		option.modelName = modelName;
		option.inf = true;
		inferencer = new Inferencer();
		inferencer.init(option);
	}
	
	/////////推断新数�?
	public Model inference(String data){
		String [] docs = new String[1];
		docs[0] = data;
		return inferencer.inference(docs);
	}

	
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		
		LDAPredictor predictor = new LDAPredictor("d:/arec/model", "model-00590");
		
		String input = "金牌 佳能 单反 广角 变焦 红圈 镜头";
		Model model = predictor.inference(input);
		
		double [] dist = model.theta[0];
		for (double d : dist) {
			System.out.print(d + " ");
		}

	}
	
}
