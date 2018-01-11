package lda;

import java.io.File;

public class LDA implements Runnable{

	@Override
	public void run() {
		LDAOption option = new LDAOption();
		
		option.dir = "resource/lda/usermodel/1015625032";
		option.dfile = "wbContent.txt";
		option.est = true;  /////
		///option.estc = true;
		option.inf = false;
		option.modelName = "model-final";
		option.niters = 1000;
		Estimator estimator = new Estimator();
		estimator.init(option);
		estimator.estimate();
	}
	
	public void runLda(String dir,String dfile){
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

	 public static void main(String args[]) {
//		new LDA().run();
//		System.out.println("ok");
		LDA myLda=new LDA();
		String folderPath="resource/lda/usermodel";
		File folder=new File(folderPath);
		if(!folder.exists()){
			return;
		}
		File[] files=folder.listFiles();
		for (File userFile : files) {
			System.out.println("-->"+userFile.getName());
			myLda.runLda(userFile.getPath(), "wbContent");
		}
		System.out.println("ok");
	}
	
}
