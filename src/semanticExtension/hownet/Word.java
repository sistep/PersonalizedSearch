
package semanticExtension.hownet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/* 代表一个词*/
public class Word {
    private String word;
    private String type;


    private String firstPrimitive;    /* 第一基本义原*/
    private List<String> otherPrimitives = new ArrayList<String>(); /* 其他基本义原。  */

    /* 如果该list非空，则该词是一个虚词（无完整意义的词汇，但有语法意义或功能意义的词）。 列表里存放的是该虚词的一个义原，部分虚词无中文虚词解释*/
    private List<String> structruralWords = new ArrayList<String>();

    /* 该词的关系义原。key: 关系义原。 value： 基本义原|(具体词)的一个列表 */
    private Map<String, List<String>> relationalPrimitives = new HashMap<String, List<String>>();

    /* 该词的关系符号义原。Key: 关系符号。 value: 属于该挂系符号的一组基本义原|(具体词)*/
    private Map<String, List<String>> relationSimbolPrimitives = new HashMap<String, List<String>>();

    /* 是否为虚词*/
    public boolean isStructruralWord(){
        return !structruralWords.isEmpty(); //返回虚词
    }
    
    public String getWord() {
        return word;
    }
    public void setWord(String word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getFirstPrimitive() {
        return firstPrimitive;
    }
    public void setFirstPrimitive(String firstPrimitive) {
        this.firstPrimitive = firstPrimitive;
    }
    
    public List<String> getOtherPrimitives() {
        return otherPrimitives;
    }
    public void setOtherPrimitives(List<String> otherPrimitives) {
        this.otherPrimitives = otherPrimitives;
    }

    public void addOtherPrimitive(String otherPrimitive) {
        this.otherPrimitives.add(otherPrimitive);
    }

    public List<String> getStructruralWords() {
        return structruralWords;
    }
    public void setStructruralWords(List<String> structruralWords) {
        this.structruralWords = structruralWords;
    }
    public void addStructruralWord(String structruralWord) {
        this.structruralWords.add(structruralWord);
    }

  
    public void addRelationalPrimitive(String key, String value) {
        List<String> list = relationalPrimitives.get(key);

        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            relationalPrimitives.put(key, list);
        } else {
            list.add(value);
        }
    }
    public void addRelationSimbolPrimitive(String key,String value){
        List<String> list = relationSimbolPrimitives.get(key);

        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            relationSimbolPrimitives.put(key, list);
        } else {
            list.add(value);
        }
    }
    public Map<String, List<String>> getRelationalPrimitives() {
        return relationalPrimitives;
    }
    public Map<String, List<String>> getRelationSimbolPrimitives() {
        return relationSimbolPrimitives;
    }
}
