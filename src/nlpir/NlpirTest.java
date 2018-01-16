package nlpir;

public class NlpirTest {

	public static void main(String[] args) {
		String line="中华人民共和国";
		line=Nlpir.NLPIR_ParagraphProcess(line, 0);
		System.out.println(">"+line+"<");
		System.out.println("ok");

	}

}
