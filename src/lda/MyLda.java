package lda;

import java.io.File;

public class MyLda {

	public static void main(String[] args) {
		//LDA myLda=new LDA();
		String folderPath="resource/lda/usermodel";
		File folder=new File(folderPath);
		if(!folder.exists()){
			return;
		}
		File[] files=folder.listFiles();
		for (File userFile : files) {
			System.out.println("-->"+userFile.getName());
			MyLda.runLda(userFile.getPath(), "wbContent.txt");
		}
		System.out.println("ok");

	}
	public static void runLda(String dir,String dfile){
		LDAOption option = new LDAOption();
		
		option.dir = dir;
		option.dfile = dfile;
		option.est = true;  /////
		///option.estc = true;
		option.inf = false;
		option.modelName = "model-final";
		option.niters = 1000;
		Estimator estimator = new Estimator();
		estimator.init(option);
		estimator.estimate();
	}

}
