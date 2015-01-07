import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
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
	String name;
}
class Entity {
	String name;
	String type;
	int sentence;
	int value;
}
class Instantiation {
	Schema s;
	String[] instantiatedSchema = new String[3];
	HashMap <String,String> instantiateMap = new HashMap<String,String>();
}
public class Solver2 {

	static HashMap <String,String> verbCategory = new HashMap<String,String>();
	static ArrayList<Schema> schemas = new ArrayList<Schema>();
	static LinkedHashSet<String> allWords = new LinkedHashSet<String>();
    static ArrayList<Entity> allEntities = new ArrayList<Entity>();
    static TreeSet<String> allTenses = new TreeSet<String>();
    //static int schemaNo = 0;
    //static HashMap <String,String> instantiateMap = new HashMap<String,String>();
    //static String[] instantiatedSchema = new String[3];
	static ArrayList<Instantiation> instantiatedSchemas = new ArrayList<Instantiation>();
	static ArrayList<Instantiation> acceptedSchemas = new ArrayList<Instantiation>();
	static ArrayList<String> allPremises = new ArrayList<String>();
	static ArrayList<String> normalVerbs = new ArrayList<String>();
	static ArrayList<String> answers = new ArrayList<String>();
	static StanfordCoreNLP pipeline ;
	static int sentenceNo;
    public static String typeSchema(Schema s) {
    	if (s.name.equals("Transfer in Place") || s.name.equals("Transfer out Place") || s.name.equals("Creation Place") || s.name.equals("Termination Place"))
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
		//verbCategory.put("eat","Termination");
		verbCategory.put("destroy","Termination");
		verbCategory.put("die","Termination");
		verbCategory.put("kill","Termination");
		verbCategory.put("more","ComparePlus");
		normalVerbs.add("more");
		verbCategory.put("great","ComparePlus");
		normalVerbs.add("great");
		verbCategory.put("altogether","Combine");
		normalVerbs.add("altogether");
		verbCategory.put("together","Combine");
		normalVerbs.add("together");
		verbCategory.put("less","CompareMinus");
		normalVerbs.add("less");
		
		
	}
	public static void buildSchema() {
		Schema transferInPlace = new Schema();
		transferInPlace.name = "Transfer in Place";
		transferInPlace.template = "[X] [object] in [place] + [Y] [object] [TinP] [place] + [Z] [object] in [place]";
		transferInPlace.formula = "X + Y = Z";
		transferInPlace.type = "TinP";
		schemas.add(transferInPlace);
		Schema transferOutPlace = new Schema();
		transferOutPlace.template = "[X] [object] in [place] + [Y] [object] [ToutP] [place] + [Z] [object] in [place]";
		transferOutPlace.formula = "Y + Z = X";
		transferOutPlace.name = "Transfer out Place";
		transferOutPlace.type = "ToutP";
		schemas.add(transferOutPlace);
		Schema transferInOwnership = new Schema();
		transferInOwnership.template = "[owner] had [R] [object] + [owner] [TinO] [S] [object] + [owner] has [T] [object]";
		transferInOwnership.formula = "R + S = T";
		transferInOwnership.type = "TinO";
		transferInOwnership.name = "Transfer in Ownership";
		schemas.add(transferInOwnership);
		Schema transferOutOwnership = new Schema();
		transferOutOwnership.template = "[owner] had [R] [object] + [owner] [ToutO] [S] [object] + [owner] has [T] [object]";
		transferOutOwnership.formula = "T + S = R";
		transferOutOwnership.type = "ToutO";
		transferOutOwnership.name = "Transfer Out Ownership";
		schemas.add(transferOutOwnership);
		Schema creationOwnership = new Schema();
		creationOwnership.template = "[owner] had [R] [object] + [owner] [Creation] [S] [object] + [owner] has [T] [object]";
		creationOwnership.formula = "R + S = T";
		creationOwnership.type = "Creation";
		transferInPlace.name = "Creation Ownership";
		schemas.add(creationOwnership);
		Schema creationPlace = new Schema();
		
		creationPlace.template = "[place] had [R] [object] + [S] [object] [Creation] [place] + [place] has [T] [object]";
		creationPlace.formula = "R + S = T";
		creationPlace.type = "Creation";
		transferInPlace.name = "Creation Place";
		schemas.add(creationPlace);
		Schema terminationPlace = new Schema();
		terminationPlace.template = "[place] had [R] [object] + [S] [object] [Termination] [place] + [place] has [T] [object]";
		terminationPlace.formula = "S + T = R";
		terminationPlace.type = "Termination";
		terminationPlace.name = "Termination Place";
		schemas.add(creationPlace);
		Schema terminationOwnership = new Schema();
		terminationOwnership.template = "[owner] had [R] [object] + [S] [object] [Termination] by [owner] + [owner] has [T] [object]";
		terminationOwnership.formula = "S + T = R";
		terminationOwnership.type = "Termination";
		terminationPlace.name = "Termination Ownership";
		schemas.add(terminationOwnership);
		//compare schemas
		Schema moreThan = new Schema();
		moreThan.template = "[[owner]2] has [Y] [object] + [[owner]1] has [X] [object] [ComparePlus] than [[owner]2] + [[owner]1] has [Z] [object]";
		moreThan.formula = "X + Y = Z";
		moreThan.type = "ComparePlus";
		moreThan.name = "More Than Present";
		schemas.add(moreThan);
		Schema moreThanPast = new Schema();
		moreThanPast.template = "[[owner]2] had [Y] [object] + [[owner]1] had [X] [object] [ComparePlus] than [[owner]2] + [[owner]1] had [Z] [object]";
		moreThanPast.formula = "X + Y = Z";
		moreThanPast.type = "ComparePlus";
		moreThanPast.name = "More Than Past";
		schemas.add(moreThanPast);
		Schema moreThanSingle = new Schema();
		moreThanSingle.template = "[owner] had [Y] [object] + [owner] has [X] [object] [ComparePlus] + [owner] has [Z] [object]";
		moreThanSingle.formula = "X + Y = Z";
		moreThanSingle.type = "ComparePlus";
		moreThanSingle.name = "More Than Present";
		schemas.add(moreThanSingle);
		Schema lessThan = new Schema();
		lessThan.template = "[[owner]2] has [Y] [object] + [[owner]1] has [X] [object] [CompareMinus] than [[owner]2] + [[owner]1] has [Z] [object]";
		lessThan.formula = "Z + X = Y";
		lessThan.type = "CompareMinus";
		lessThan.name = "Less Than Present";
		schemas.add(lessThan);
		Schema lessThanPast = new Schema();
		lessThanPast.template = "[[owner]2] had [Y] [object] + [[owner]1] had [X] [object] [CompareMinus] than [[owner]2] + [[owner]1] had [Z] [object]";
		lessThanPast.formula = "Z + X = Y";
		lessThanPast.type = "CompareMinus";
		lessThanPast.name = "Less Than Past";
		schemas.add(lessThanPast);
		//combine schema

		Schema combineOwnerSingle = new Schema();
		Schema combineOwner = new Schema();
		combineOwner.template = "[[owner]1] has [Y] [object]  + [Combine] [[owner]1] and [[owner]2] has [Z] [object] + [[owner]2] has [X] [object]";
		combineOwner.formula = "X + Y = Z";
		combineOwner.type = "Combine";
		combineOwner.name = "Combine Owner Present";
		schemas.add(combineOwner);
		combineOwnerSingle.template = "[owner] has [Y] [object]  + [Combine] [owner] has [Z] [object] + [owner] has [X] [object]";
		combineOwnerSingle.formula = "X + Y = Z";
		combineOwnerSingle.type = "Combine";
		combineOwnerSingle.name = "Combine Owner Single Present";
		schemas.add(combineOwnerSingle);
		Schema combineOwnerSinglePast = new Schema();
		combineOwnerSinglePast.template = "[owner] had [Y] [object]  + [Combine] [owner] had [Z] [object] + [owner] had [X] [object]";
		combineOwnerSinglePast.formula = "X + Y = Z";
		combineOwnerSinglePast.type = "Combine";
		combineOwnerSinglePast.name = "Combine Owner Single Past";
		schemas.add(combineOwnerSinglePast);
		
		
	}
	public static void instantiateSchema(String type, String lemma, int sentenceNo, String pos) {
		HashMap <String,String> instantiateMap = new HashMap<String,String>();
		instantiateMap.put("["+type+"]", lemma);
		String[] instantiatedSchema = new String[3];
		ArrayList<Schema> applicableSchemas = new ArrayList<Schema>();
		for (Schema s : schemas) {
			if (s.type.equals(type)) {
				String fineType = typeSchema(s);
				System.out.print(fineType);
				if (s.name.contains("Present") && ((pos.equals("VBN") || pos.equals("VBD"))))
					continue;
				if (s.name.contains("Past") && !pos.equals("VBN") && !pos.equals("VBD"))
					continue;
				boolean typeFlag = false;
				for (String word : allWords) {
					if (word.contains(fineType)) {
						typeFlag = true;
						break;
					}
				}
				if (typeFlag) {
					System.out.println("acc"+lemma+"|"+s.name+"|"+pos);
					applicableSchemas.add(s);
				}
			}
		}
		System.out.println("app"+applicableSchemas.size());
		for (Schema s : applicableSchemas) {
			String[] stmts = s.template.split("\\+");
			String copy = "";
			//System.out.println(stmts[1]);
			boolean doesEntityExist = false;
			for (Entity e : allEntities) {
				if (e.sentence == sentenceNo) {
					doesEntityExist = true;
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
					    //Pattern complexTemplatePattern = Pattern.compile("\\[\\[[a-z]+\\]\\d\\]");
					    //matcher = complexTemplatePattern.matcher(component);
					    
					    Pattern templatePattern = Pattern.compile("\\[[a-z]+\\]");
					    matcher = templatePattern.matcher(component);
					    if (matcher.find()) {
					    	String match = "";
						    for (String word : allWords) {
						    	String typePlaceholder = typeSchema(s);
						    	String givenType = word.substring(word.length()-7, word.length());
						    	if (typePlaceholder.equals(givenType) && word.contains("["+sentenceNo+"]")) {
						    		word = word.substring(0,word.length()-7);
						    		if (!instantiateMap.containsValue(word.replaceFirst("\\[\\d\\]", ""))) {
						    			match = word.replaceFirst("\\[\\d\\]", "");
						    			break;
						    		}
						    	}
						    }
						    System.out.println("ccc"+component+"|"+match);
						    if (!match.equals("")) {
						    	instantiateMap.put(component,match);
						    	copy = copy + match + " ";
						    	continue;
						    }
						} 
					    if (instantiateMap.containsKey(component)) {
					    	copy = copy + instantiateMap.get(component) + " ";
					    	continue;
					    }
					    copy = copy + component + " ";
					}    
				}
			}
			if (!doesEntityExist) {
				String[] components = stmts[1].split(" ");
				for (String component : components) {
					if (component.equals(""))
						continue;
					for (String word : allWords) {
						if(word.contains("object")) {
							String tempWord = word.substring(0,word.length()-8);
				    		instantiateMap.put("[object]",tempWord.replaceFirst("\\[\\d\\]", ""));
				    		continue;
						}
						String typePlaceholder = typeSchema(s);
				    	String givenType = word.substring(word.length()-7, word.length());
				    	word = word.substring(0,word.length()-7);
				    	if (component.contains(typePlaceholder) && typePlaceholder.equals(givenType) && word.contains("["+sentenceNo+"]") && !instantiateMap.containsValue(word.replaceFirst("\\[\\d\\]", ""))) 
				    		instantiateMap.put(component,word.replaceFirst("\\[\\d\\]", ""));
				    }
					if(component.contains("[") && !component.matches("\\[[A-Z]\\]"))
						copy = copy + instantiateMap.get(component)+ " ";
					else if (component.matches("\\[[A-Z]\\]")) {
						copy = copy + "? ";
						instantiateMap.put(component, "?");
					}
					else
						copy = copy + component+ " ";
				}
			}
			System.out.println("mmm"+copy+"|"+instantiateMap);
			if (copy.contains("null")) {
				instantiateMap = new HashMap<String,String>();
				continue;
			}
			else {
				instantiatedSchema[1] = copy;
				break;
			}
		}
		Instantiation currentInstantiation = new Instantiation();
		
		currentInstantiation.instantiatedSchema = instantiatedSchema;
		currentInstantiation.instantiateMap = instantiateMap;
		currentInstantiation.s = applicableSchemas.get(0);
		instantiatedSchemas.add(currentInstantiation);
		completeSchema(sentenceNo,currentInstantiation, pos);
	}
	public static void solve(int sentenceNo, int otherSentence, Instantiation inst, boolean changeTenseFlag) {
		String formula = inst.s.formula;
		HashMap <String, String> instantiateMap = inst.instantiateMap;
		String[] elements = formula.split(" ");
		acceptedSchemas.remove(inst);
		String question = null;
		for (int i=0; i<elements.length; i++) {
			String element = elements[i];
			if (element.equals("+") || element.equals("=")) 
				continue;
			if (instantiateMap.get("["+element+"]").equals("?")) {
				question = "["+element+"]";
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
		String[] answerSchema = new String[3];
		for (int i = 0; i < 3; i++) {
			String copy = "";
			boolean isQuestion = false;
			String[] components = inst.s.template.split("\\+")[i].split(" ");
			for (String component : components) {
				if (component.equals(""))
					continue;
				if (component.equals(question)) 
					isQuestion = true;
				if (component.contains("[")) 
					copy = copy + instantiateMap.get(component) + " ";
				else
					copy = copy + component + " ";
				
			}
			System.out.println(copy);
			answerSchema[i] = copy;
			if (isQuestion) {
				
				/*boolean changeFlag = false;
				for (String word : allWords) {
					
				}*/
				if (changeTenseFlag) 
					copy = copy.replace("had", "has");
				answers.add(copy);
				allPremises.add(sentenceNo-1,copy+".");
				allPremises.remove(sentenceNo);
				allPremises.remove(otherSentence - 1);
				String text = "";
				for (String premise : allPremises) {
					text = text + premise + "\n";
				}
				allPremises = new ArrayList<String>();
				allWords = new LinkedHashSet<String>();
				allTenses = new TreeSet<String>();
				instantiatedSchemas = new ArrayList<Instantiation>();
				acceptedSchemas = new ArrayList<Instantiation>();
				allEntities = new ArrayList<Entity>();
				String input = expandPremises(text);
				solveProb(input);
				/*Annotation document = new Annotation(copy+".");
			    pipeline.annotate(document);
			    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			    CoreMap sentence = sentences.get(0);
			    processSentence(sentence,sentenceNo);*/
			}
		}
		inst.instantiateMap = instantiateMap;
		inst.instantiatedSchema = answerSchema;
		System.out.println(allPremises);
		acceptedSchemas.add(inst);
	}
	public static String expandPremises(String initialPremises) {
		String ans = null;
		System.out.println(initialPremises);
		String[] premises = initialPremises.split("\n");
		ArrayList<String> newPremises = new ArrayList<String>();
		ArrayList<String> firstType = new ArrayList<String>();
		firstType.add("give");
		firstType.add("sell");
		firstType.add("pay");
		firstType.add("donate");
		ArrayList<String> secondType = new ArrayList<String>();
		secondType.add("steal");
		secondType.add("buy");
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse");
	    pipeline = new StanfordCoreNLP(props);
		for (String premise : premises) {
			Annotation document = new Annotation(premise);
		    pipeline.annotate(document);
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		    CoreMap sentence = sentences.get(0);
		    boolean complexVerbFlag = false;
		    for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		    	String word = token.get(TextAnnotation.class);
		    	String lemma;
		    	lemma = token.get(LemmaAnnotation.class);
		    	String pos = token.get(PartOfSpeechAnnotation.class);
		    	System.out.println("expand"+word+"|"+pos+"|"+lemma+"|"+token.get(NamedEntityTagAnnotation.class));
		    	if (pos.contains("VB")) {
		    		if (firstType.contains(lemma) || secondType.contains(lemma)) {
		    			complexVerbFlag = true;
		    			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		    	    	System.out.println(dependencies);
		    	    	ArrayList<SemanticGraphEdge> edges = (ArrayList<SemanticGraphEdge>) dependencies.edgeListSorted();
		    	    	String owner1 = null, owner2 = null, object = "", entity = "";
		    	    	boolean isEntity = false;
		    	    	for (SemanticGraphEdge edge : edges) {
		    	    		System.out.println(edge.getTarget().toString());
		    	    		if (edge.getTarget().toString().contains("NN")) {
		    	    			System.out.println("in"+edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());	
		    	    			if (edge.getRelation().toString().equals("nsubj"))
		    	    				owner1 = edge.getTarget().lemma();
		    	    			else if (!edge.getRelation().toString().contains("dobj"))
		    	    				owner2 = edge.getTarget().lemma();
		    	    			else if (edge.getRelation().toString().contains("dobj"))
		    	    				object = edge.getTarget().lemma();
		    	    		}
		    	    		if (edge.getRelation().toString().equals("num")) {
		    	    			isEntity = true;
		    	    			entity = entity + edge.getTarget().lemma();
		    	    			entity = entity + " " + edge.getSource().originalText();
		    	    		}
		    	    	}
		    	    	if (!isEntity)
		    	    		entity = "Q " + object;
		    	    	if (firstType.contains(lemma)) {
		    	    		newPremises.add(owner1 +" forfeited "+entity+".");
		    	    		newPremises.add(owner2 +" got "+entity+".");
		    	    	}
		    	    	else {
		    	    		newPremises.add(owner1 +" got "+entity+".");
		    	    		newPremises.add(owner2 +" forfeited "+entity+".");
		    	    	}
		    		}
		    		break;
		    	}
		    }
			if (!complexVerbFlag)
				newPremises.add(premise);
		}
		ans = "";
		for (String premise : newPremises) {
			ans = ans + premise + "\n";
		}
		System.out.println(ans);
		allPremises = newPremises;
		return ans;
	}
	public static void completeSchema(int sentenceNo, Instantiation curInstantiation, String pos) {
		System.out.println(curInstantiation.instantiateMap);
		System.out.println("ha"+allWords);
		System.out.println("ha"+allTenses);
		HashMap<String,String> instantiateMap = curInstantiation.instantiateMap;
		ArrayList<String> eventTrackers = new ArrayList<String>();
		boolean changeTenseFlag = false;
		String[] instantiatedSchema = curInstantiation.instantiatedSchema;
		Schema s = curInstantiation.s;
		ArrayList<String> instantiations = new ArrayList<String>(); 
		for (Map.Entry<String, String> entry : instantiateMap.entrySet()) {
		    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		    if (entry.getKey().contains(typeSchema(s))) 
		    	instantiations.add(entry.getValue());
		}
		String[] stmts = s.template.split("\\+");
		int index = 0, otherSentence = 0;
		for (String word : allWords) {
			String core = word.substring(3,word.length()-7);
			//skipping object
			if (core.contains("["))
				continue;
			if (instantiations.contains(core)) {
				if(!word.contains(sentenceNo+"")) {
					System.out.println(word);
					//assumes single digit
					int no = Integer.parseInt(word.charAt(1)+"");
					for (String tense : allTenses) {
						int tenseSentence = Integer.parseInt(tense.charAt(1)+"");
						String verb = tense.substring(tense.lastIndexOf("[")+1, tense.length()-1);
						//take care of multiple events for same actor
						if (no == tenseSentence && verbCategory.containsKey(verb) && no>sentenceNo) {
							eventTrackers.add(core);
							System.out.println(no + "|" +sentenceNo + "|" + eventTrackers);
							continue;
						}
						System.err.println("hiiii"+verb+"|"+core+"|"+no+"|"+tenseSentence+"|"+sentenceNo);
						//need to generalize more
						if (no == tenseSentence && verbCategory.containsKey(verb) && no < sentenceNo && !s.type.contains("Compare") && !s.type.contains("Combine") && !normalVerbs.contains(verb)) {
							System.err.println("hiiii"+verb+"|"+core);
							changeTenseFlag = true;
						}
						if (no == tenseSentence && verbCategory.containsKey(verb) && no > sentenceNo && !s.type.contains("Compare") && !s.type.contains("Combine"))
							return;
						
						if (no == tenseSentence && !verbCategory.containsKey(verb) && !eventTrackers.contains(core)) { 
							if (s.name.contains("Past") && tense.contains("present"))
								continue;
							if (s.type.contains("Compare") || s.type.contains("Combine")) {
								
								if (stmts[0].contains("[[owner]1]") && instantiateMap.get("[[owner]1]").equals(core))
									index = 0;
								else if (stmts[0].contains("[[owner]2]") && instantiateMap.get("[[owner]2]").equals(core))
									index = 0;
								if (stmts[2].contains("[[owner]1]") && instantiateMap.get("[[owner]1]").equals(core))
									index = 2;
								else if (stmts[2].contains("[[owner]2]") && instantiateMap.get("[[owner]2]").equals(core))
									index = 2;
							}
							else if (!s.name.contains("Present") && !s.name.contains("Past")) {
								if (tense.contains("past")) {
									/*System.out.println(pos + "|" +no + "|" + sentenceNo);
									if (pos.equals("VBD") || pos.equals("VBN")) {
										if (no > sentenceNo)
											index = 2;
										else
											index = 0;
									}
									else*/
										index = 0;
								}
								else if (!tense.contains("past") && no>sentenceNo)
									index = 2; 
								else if (no<sentenceNo)
									index = 0;
							}
							else if (s.name.contains("Past") && !tense.contains("past"))
								continue;
							else if (s.name.contains("Present") && tense.contains("past"))
								continue;
							String copy = "";
							otherSentence = no;
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
			if (stmt == null && !instantiateMap.containsValue("?")) {
				String copy = "";
				String[] components = s.template.split("\\+")[i].split(" ");
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
		curInstantiation.instantiatedSchema = instantiatedSchema;
		curInstantiation.instantiateMap = instantiateMap;
		boolean accepted = true;
		for (int i = 0; i < 3; i++) {
			String stmt = instantiatedSchema[i];
			if (stmt == null) {
				accepted = false;
				break;
			}
		}
		if (accepted) {
			acceptedSchemas.add(curInstantiation);
			solve(sentenceNo, otherSentence, curInstantiation, changeTenseFlag);
		}
	}
	public static void processSentence(CoreMap sentence, int sentenceNo) {
		SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
    	System.out.println(dependencies);
    	ArrayList<SemanticGraphEdge> edges = (ArrayList<SemanticGraphEdge>) dependencies.edgeListSorted();
    	//assumes one entity  per sentence
    	IndexedWord entity = null;
    	for (SemanticGraphEdge edge : edges) {
    		System.out.println(edge.getTarget().toString());
    		if (edge.getTarget().toString().contains("NN")) {
    			System.out.println("in"+edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());
    			//assumes all proper nouns are people
    			if (edge.getTarget().toString().contains("NNP") && !edge.getTarget().lemma().equals("Q"))
    				allWords.add("["+sentenceNo+"]"+edge.getTarget().lemma()+"[owner]");
    			else if (edge.getRelation().toString().equals("nsubj"))
    				allWords.add("["+sentenceNo+"]"+edge.getTarget().lemma()+"[owner]");
    			else if (edge.getRelation().toString().contains("prep"))
    				allWords.add("["+sentenceNo+"]"+edge.getTarget().lemma()+"[place]");
    			//else if (edge.getRelation().toString().contains("dobj"))
    				//allWords.add("["+sentenceNo+"]"+edge.getTarget().lemma()+"[object]");
    		}
    		if (edge.getRelation().toString().equals("num")) {
    			Entity newEntity = new Entity();
    			if (!edge.getSource().lemma().matches("[a-zA-Z]+"))
    				continue;
    			entity = edge.getSource();
    			newEntity.name = edge.getSource().lemma();
    			allWords.add("["+sentenceNo+"]"+newEntity.name+"[object]");
    			newEntity.sentence = sentenceNo;
    			newEntity.value = Integer.parseInt(NumberNameToNumber.convert(edge.getTarget().originalText()));
    			//System.out.println(newEntity.name + "|" + newEntity.value);			
    			allEntities.add(newEntity);
    		}
    	}
    	for (SemanticGraphEdge edge : edges) {
    		System.out.println("aaa"+edge.getSource()+"|"+edge.getTarget()+"|"+edge.getRelation());
    		if (edge.getRelation().toString().equals("nsubj") || edge.getRelation().toString().equals("dobj")) {
    			if (!edge.getSource().lemma().matches("[a-zA-Z]+"))
    				continue;
    			if (edge.getTarget().equals(entity)) {
    				String pos = edge.getSource().toString();
    				if (pos.contains("VBD") || pos.contains("VBN"))
	    				allTenses.add("["+sentenceNo+"] "+"past ["+edge.getSource().lemma()+"]");
	    			else
	    				allTenses.add("["+sentenceNo+"] "+"present ["+edge.getSource().lemma()+"]");
	    		}
    		}
    	}
    	System.out.println("In"+sentenceNo+"|"+allWords);
	}
 	public static void solveProb(String input) {
 		Annotation document = new Annotation(input);
	    pipeline.annotate(document);
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    sentenceNo = 1;
	    String type = "";
	    for(CoreMap sentence: sentences) {
	    	System.out.println(sentence);
	    	processSentence(sentence,sentenceNo);
	    	sentenceNo++;
	    }
	    System.out.println(allWords);
	    System.out.println(allTenses);
	    int counter = 1;
	    for(CoreMap sentence: sentences) {
	    	String verbTense = ""; 
		    for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		    	String word = token.get(TextAnnotation.class);
		    	String lemma;
		    	lemma = token.get(LemmaAnnotation.class);
		    	String pos = token.get(PartOfSpeechAnnotation.class);
		    	System.out.println(word+"|"+pos+"|"+lemma+"|"+token.get(NamedEntityTagAnnotation.class));
		    	if (pos.contains("VB") || pos.contains("RB") || pos.contains("JJR")) {
		    		if (pos.contains("VB")) {
		    			verbTense = pos;
		    		}
		    		if (verbCategory.containsKey(lemma)) {
		    			type = verbCategory.get(lemma);
		    			System.out.println("Trigger "+type+"|"+pos);
		    			if (!pos.contains("VB")) {
		    				String deletedTense = "";
		    				for (String tense : allTenses) {
		    					if(tense.contains(counter+"")) {
		    						deletedTense = tense;
		    					}
		    				}
		    				allTenses.remove(deletedTense);
		    				if (verbTense.contains("VBD") || verbTense.contains("VBN"))
		    					allTenses.add("["+counter+"] past ["+lemma+"]");
		    				else
		    					allTenses.add("["+counter+"] present ["+lemma+"]");
		    				instantiateSchema(type,lemma,counter,verbTense);
		    			}
		    			else
		    				instantiateSchema(type,lemma,counter,pos);
		    			//completeSchema(sentenceNo,questionSentence);
		    		}
		    	}
		    }
		    counter++;
	    }
	}
 	public static void main(String[] args) {
 		buildMap();
 		buildSchema();
 		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner,parse");
	    pipeline = new StanfordCoreNLP(props);
	    String question = "David gave 3 candies to Ruth, and John gave 2 candies to David. Now David has 4 candies more than Ruth has. How many candies does David have now, if Ruth had 7 candies in the beginning?";
		String input = Parser.parse(question);
		String text = expandPremises(input);
 		solveProb(text);
 		System.out.println("===========================================================================");
 		System.out.println("Question: ");
 		System.out.println(question);
 		System.out.println("===========================================================================");
 		System.out.println("Simplified Question: ");
 		System.out.println(input);
 		System.out.println("===========================================================================");
 		System.out.println("Solution: ");
 		for (String answer : answers) 
 			if (!question.contains(answer.trim()))
 				System.out.println(answer);
 		
 	}
}
