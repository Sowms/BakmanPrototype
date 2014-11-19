import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;


class Schema {
	String template;
	String formula;
	String type;
}
class Entity {
	String name;
	String type;
	int sentence;
	int value;
}
public class Solver {

	static HashMap <String,String> verbCategory = new HashMap<String,String>();
	static ArrayList<Schema> schemas = new ArrayList<Schema>();
	static LinkedHashSet<String> allWords = new LinkedHashSet<String>();
    static ArrayList<Entity> allEntities = new ArrayList<Entity>();
	public static void buildMap() {
		verbCategory.put("receive","TinO");
		verbCategory.put("get","TinO");
		verbCategory.put("lose","ToutO");
		verbCategory.put("forfeit","ToutO");
		verbCategory.put("send","ToutO");
		verbCategory.put("fetch","TinP");
		verbCategory.put("bring","TinP");
		verbCategory.put("put","TinP");
		verbCategory.put("lay","TinP");
		verbCategory.put("enter","TinP");
		verbCategory.put("fall","TinP");
		verbCategory.put("add","TinP");
		verbCategory.put("take out","ToutP");
		verbCategory.put("take away","ToutP");
		verbCategory.put("take exit","ToutP");
		verbCategory.put("go away","ToutP");
		verbCategory.put("send out","ToutP");
		verbCategory.put("drag out","ToutP");
		verbCategory.put("fall from","ToutP");
		verbCategory.put("build","Creation");
		verbCategory.put("born","Creation");
		verbCategory.put("create","Creation");
		verbCategory.put("make","Creation");
		verbCategory.put("eat","Termination");
		verbCategory.put("destroy","Termination");
		verbCategory.put("die","Termination");
		verbCategory.put("kill","Termination");
	}
	public static void buildSchema() {
		Schema transferInPlace = new Schema();
		transferInPlace.template = "[X] [object] in [place] + [Y] [object] [TinP] [place] + [Z] [object] in [place]";
		transferInPlace.formula = "X + Y = Z";
		transferInPlace.type = "TinP";
		schemas.add(transferInPlace);
		Schema transferOutPlace = new Schema();
		transferOutPlace.template = "[X] [object] in [place] + [Y] [object] [ToutP] [place] + [Z] [object] in [place]";
		transferOutPlace.formula = "Y + Z = X";
		transferOutPlace.type = "ToutP";
		schemas.add(transferOutPlace);
		Schema transferInOwnership = new Schema();
		transferInOwnership.template = "[owner] had [R] [object] + [owner] [TinO] [S] [object] + [owner] has [T] [object]";
		transferInOwnership.formula = "R + S = T";
		transferInOwnership.type = "TinO";
		schemas.add(transferInOwnership);
		Schema transferOutOwnership = new Schema();
		transferOutOwnership.template = "[owner] had [R] [object] + [owner] [ToutO] [S] [object] + [owner] has [T] [object]";
		transferOutOwnership.formula = "T + S = R";
		transferOutOwnership.type = "ToutO";
		schemas.add(transferOutOwnership);
		Schema creationOwnership = new Schema();
		creationOwnership.template = "[owner] had [R] [object] + [owner] [Creation] [S] [object] + [owner] has [T] [object]";
		creationOwnership.formula = "R + S = T";
		creationOwnership.type = "Creation";
		schemas.add(creationOwnership);
		Schema creationPlace = new Schema();
		creationPlace.template = "[place] had [R] [object] + [S] [object] [Creation] [place] + [place] has [T] [object]";
		creationPlace.formula = "R + S = T";
		creationPlace.type = "Creation";
		schemas.add(creationPlace);
		Schema terminationPlace = new Schema();
		terminationPlace.template = "[place] had [R] [object] + [S] [object] [Termination] [place] + [place] has [T] [object]";
		terminationPlace.formula = "S + T = R";
		terminationPlace.type = "Termination";
		schemas.add(creationPlace);
		Schema terminationOwnership = new Schema();
		terminationOwnership.template = "[owner] had [R] [object] + [S] [object] [Termination] by [owner] + [ownere] has [T] [object]";
		terminationOwnership.formula = "S + T = R";
		terminationOwnership.type = "Termination";
		schemas.add(terminationOwnership);
		
	}
	public static void instantiateSchema(String type, int sentenceNo) {
		
		ArrayList<Schema> applicableSchemas = new ArrayList<Schema>();
		for (Schema s : schemas) {
			if (s.type.equals(type))
				applicableSchemas.add(s);
		}
		HashMap <String,String> instantiateMap = new HashMap<String,String>();
		for (Schema s : applicableSchemas) {
			String[] stmts = s.template.split("\\+");
			String copy = "";
			//System.out.println(stmts[1]);
			for (Entity e : allEntities) {
				if (e.sentence == sentenceNo) {
					String[] components = stmts[1].split(" ");
					for (String component : components) {
						if(component.equals(""))
							continue;
						//System.out.println(component.contains("object")+"|"+component.matches("\\w*"));
						Pattern varPattern = Pattern.compile("\\[[A-Z]\\]");
					    Matcher matcher = varPattern.matcher(component);
					    if (matcher.find()) {
					    	instantiateMap.put(matcher.group(),new String(e.value+""));
					    	copy = copy + e.value + " ";
					    	instantiateMap.put("[object]",new String(e.name+""));   
					    	continue;
					    }
					    if (component.contains("object")) {
					    	copy = copy + instantiateMap.get("[object]") + " ";
					    	continue;
					    }
					   // System.out.println(instantiateMap);
					    Pattern templatePattern = Pattern.compile("\\[[a-z]+\\]");
					    matcher = templatePattern.matcher(component);
					    if (matcher.find()) {
					    	String match = "";
						    for (String word : allWords) {
						    	if (word.contains("["+sentenceNo+"]") && !instantiateMap.containsValue(word.replaceFirst("\\[\\d\\]", ""))) {
						    		match = word.replaceFirst("\\[\\d\\]", "");
						    		break;
						    	}
						    }
					    	instantiateMap.put(matcher.group(),match);
						    copy = copy + match + " ";
						    continue;
						} 
					    copy = copy + component + " ";
					}    
				}
			}
			System.out.println(copy+"|"+instantiateMap);
		}
	}
 	public static void main(String args[]) {
 		buildMap();
 		buildSchema();
		String input = "Ruth had 3 apples. She put 2 apples into a basket. "+
					   "How many apples are there in the basket now, if in "+
					   "the beginning there were 4 apples in the basket?";
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    String text = input; 
	    Annotation document = new Annotation(text);
	    pipeline.annotate(document);
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    int counter = 1, sentenceNo=0;
	    String type = "";
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		String word = token.get(TextAnnotation.class);
	    		String lemma;
	   			lemma = token.get(LemmaAnnotation.class);
	    		String pos = token.get(PartOfSpeechAnnotation.class);
	    		System.out.println(word+"|"+pos+"|"+lemma+"|"+token.get(NamedEntityTagAnnotation.class));
	    		if (pos.contains("VB"))
	    			if (verbCategory.containsKey(lemma)) {
	    				type = verbCategory.get(lemma);
	    				sentenceNo = counter; 
	    				System.out.println("Trigger "+type);
	    			}
	    		if (pos.contains("NN"))
	    			allWords.add("["+counter+"]"+lemma);
	    	}
	    	SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	    	System.out.println(dependencies);
	    	ArrayList<SemanticGraphEdge> edges = (ArrayList<SemanticGraphEdge>) dependencies.edgeListSorted();
	    	for (SemanticGraphEdge edge : edges) {
	    		System.out.println(edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());
	    		if (edge.getRelation().toString().equals("num")) {
	    			Entity newEntity = new Entity();
	    			if (!edge.getSource().lemma().matches("[a-zA-Z]+"))
	    				continue;
	    			newEntity.name = edge.getSource().lemma();
	    			newEntity.sentence = counter;
	    			newEntity.value = Integer.parseInt(NumberNameToNumber.convert(edge.getTarget().originalText()));
	    			//System.out.println(newEntity.name + "|" + newEntity.value);			
	    			allEntities.add(newEntity);
	    			break;
	    		}
	    	}
	    	counter++;
	    }
	    instantiateSchema(type,sentenceNo);
	}
}
