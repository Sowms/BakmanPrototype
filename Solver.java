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
import edu.stanford.nlp.ling.IndexedWord;
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
    static ArrayList<String> allTenses = new ArrayList<String>();
    static int schemaNo = 0;
    static HashMap <String,String> instantiateMap = new HashMap<String,String>();
    static String[] instantiatedSchema = new String[3];
	
    public static String typeSchema(int schemaNumber) {
    	if (schemaNo == 0 || schemaNo == 5 || schemaNo ==6 || schemaNo == 1)
    		return "[place]";
    	return "[owner]";
    	
    }
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
		terminationOwnership.template = "[owner] had [R] [object] + [S] [object] [Termination] by [owner] + [owner] has [T] [object]";
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
		for (Schema s : applicableSchemas) {
			schemaNo = schemas.indexOf(s);
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
					    Pattern templatePattern = Pattern.compile("\\[[a-z]+\\]");
					    matcher = templatePattern.matcher(component);
					    if (matcher.find()) {
					    	String match = "";
						    for (String word : allWords) {
						    	String typePlaceholder = typeSchema(schemaNo);
						    	String givenType = word.substring(word.length()-7, word.length());
						    	if (typePlaceholder.equals(givenType) && word.contains("["+sentenceNo+"]")) {
						    		word = word.substring(0,word.length()-7);
						    		if (!instantiateMap.containsValue(word.replaceFirst("\\[\\d\\]", ""))) {
						    			match = word.replaceFirst("\\[\\d\\]", "");
						    			break;
						    		}
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
			instantiatedSchema[1] = copy;
		}
	}
	public static void solve() {
		String formula = schemas.get(schemaNo).formula;
		String[] elements = formula.split(" ");
		for (int i=0; i<elements.length; i++) {
			String element = elements[i];
			if (element.equals("+") || element.equals("=")) 
				continue;
			if (instantiateMap.get("["+element+"]").equals("?")) {
				if (i==0) {
					int value1 = Integer.parseInt(instantiateMap.get("["+elements[2]+"]"));
					int value2 = Integer.parseInt(instantiateMap.get("["+elements[4]+"]"));
					instantiateMap.put("["+element+"]",value2 - value1 + "");
					break;
				}
				if (i==2) {
					int value1 = Integer.parseInt(instantiateMap.get("["+elements[0]+"]"));
					int value2 = Integer.parseInt(instantiateMap.get("["+elements[4]+"]"));
					instantiateMap.put("["+element+"]",value2 - value1 + "");
					break;
				}
				if (i==4) {
					int value1 = Integer.parseInt(instantiateMap.get("["+elements[0]+"]"));
					int value2 = Integer.parseInt(instantiateMap.get("["+elements[2]+"]"));
					instantiateMap.put("["+element+"]",value2 + value1 + "");
					break;	
				}
			}
		}
		System.out.println("Answer:");
		for (int i = 0; i < 3; i++) {
			String copy = "";
			String[] components = schemas.get(schemaNo).template.split("\\+")[i].split(" ");
			for (String component : components) {
				if (component.equals(""))
					continue;
				if (component.contains("[")) 
					copy = copy + instantiateMap.get(component) + " ";
				else
					copy = copy + component + " ";
				
			}
			System.out.println(copy);
		}
		
	}
	public static void completeSchema(int sentenceNo, int questionNo) {
		String instantiation = instantiateMap.get(typeSchema(schemaNo));
		System.out.println(allTenses+"|"+instantiation);
		String[] stmts = schemas.get(schemaNo).template.split("\\+");
		int index = 0;
		for (String word : allWords) {
			if (word.contains(instantiation)) {
				if(!word.contains(sentenceNo+"")) {
					System.out.println(word);
					//assumes single digit
					int no = Integer.parseInt(word.charAt(1)+"");
					for (String tenses : allTenses) {
						int tenseSentence = Integer.parseInt(tenses.charAt(1)+"");
						if (no == tenseSentence) { 
							if (tenses.contains("past") && no>sentenceNo)
								index = 0;
							else if (!tenses.contains("past") && no>sentenceNo)
								index = 2; 
							else if (no<sentenceNo)
								index = 0;
							String copy = "";
							for (Entity e : allEntities) {
								if (e.sentence == no) {
									String[] components = stmts[index].split(" ");
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
										if (instantiateMap.containsKey(component)) {
											copy = copy + instantiateMap.get(component);
											continue;
										}
										copy = copy + component + " ";
									}    
								}
							}
							System.out.println(copy+"|"+instantiateMap);
							instantiatedSchema[index] = copy;
						}
					}
				}
			}
		}
		for (int i = 0; i < 3; i++) {
			String stmt = instantiatedSchema[i];
			if (stmt == null) {
				String copy = "";
				String[] components = schemas.get(schemaNo).template.split("\\+")[i].split(" ");
				for (String component : components) {
					if (component.equals(""))
						continue;
					if (component.contains("[")) {
						if (instantiateMap.containsKey(component))
							copy = copy + instantiateMap.get(component) + " ";
						else {
							copy = copy + "?" + " ";
							instantiateMap.put(component, "?");
						}
						continue;
					}
					copy = copy + " ";
				}
				instantiatedSchema[i] = copy;
				break;
			}
		}
		System.out.println(instantiatedSchema[0]);
		System.out.println(instantiatedSchema[1]);
		System.out.println(instantiatedSchema[2]);
		System.out.println(instantiateMap);
	}
 	public static void main(String args[]) {
 		buildMap();
 		buildSchema();
		String input = "There are 4 apples in a basket. Ruth put 2 apples into the basket. "+
					   "How many apples are there in the basket now?";
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    String text = input; 
	    Annotation document = new Annotation(text);
	    pipeline.annotate(document);
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    int counter = 1, sentenceNo=0, questionSentence = 0;
	    String type = "";
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		String word = token.get(TextAnnotation.class);
	    		String lemma;
	   			lemma = token.get(LemmaAnnotation.class);
	    		String pos = token.get(PartOfSpeechAnnotation.class);
	    		System.out.println(word+"|"+pos+"|"+lemma+"|"+token.get(NamedEntityTagAnnotation.class));
	    		if (pos.contains("VB")) {
	    			if (verbCategory.containsKey(lemma)) {
	    				type = verbCategory.get(lemma);
	    				sentenceNo = counter; 
	    				instantiateMap.put("["+type+"]", lemma);
	    				System.out.println("Trigger "+type);
	    			}
	    		}
	    		if (pos.contains("W"))
	    			questionSentence = counter;
	    	}
	    	SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	    	System.out.println(dependencies);
	    	ArrayList<SemanticGraphEdge> edges = (ArrayList<SemanticGraphEdge>) dependencies.edgeListSorted();
	    	//assumes one entity per sentence
	    	IndexedWord entity = null;
	    	for (SemanticGraphEdge edge : edges) {
	    		System.out.println(edge.getTarget().toString());
	    		if (edge.getTarget().toString().contains("NN")) {
	    			System.out.println("in"+edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());	
	    			if (edge.getRelation().toString().equals("nsubj"))
	    				allWords.add("["+counter+"]"+edge.getTarget().lemma()+"[owner]");
	    			else if (edge.getRelation().toString().contains("prep"))
	    				allWords.add("["+counter+"]"+edge.getTarget().lemma()+"[place]");
	    			else if (edge.getRelation().toString().contains("obj"))
	    				allWords.add("["+counter+"]"+edge.getTarget().lemma()+"[object]");
	    		}
	    		if (edge.getRelation().toString().equals("num")) {
	    			Entity newEntity = new Entity();
	    			if (!edge.getSource().lemma().matches("[a-zA-Z]+"))
	    				continue;
	    			entity = edge.getSource();
	    			newEntity.name = edge.getSource().lemma();
	    			newEntity.sentence = counter;
	    			newEntity.value = Integer.parseInt(NumberNameToNumber.convert(edge.getTarget().originalText()));
	    			//System.out.println(newEntity.name + "|" + newEntity.value);			
	    			allEntities.add(newEntity);
	    		}
	    	}
	    	for (SemanticGraphEdge edge : edges) {
	    		//System.out.println(edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());
	    		if (edge.getRelation().toString().equals("nsubj")) {
	    			if (!edge.getSource().lemma().matches("[a-zA-Z]+"))
	    				continue;
	    			if (edge.getTarget().equals(entity)) {
	    				String pos = edge.getSource().toString();
	    				if (pos.contains("VBD") || pos.contains("VBN"))
		    				allTenses.add("["+counter+"] "+"past");
		    			else
		    				allTenses.add("["+counter+"] "+"present");
		    		}
	    		}
	    	}
	    	counter++;
	    }
	    System.out.println(allWords);
	    instantiateSchema(type,sentenceNo);
	    completeSchema(sentenceNo,questionSentence);
	    solve();
	}
}
