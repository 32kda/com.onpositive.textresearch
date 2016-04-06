package com.onpositive.wikipedia.workbench;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import junit.framework.TestCase;

import com.onpositive.semantic.search.core.ICategorizable;
import com.onpositive.semantic.search.core.ICategory;
import com.onpositive.semantic.search.core.IDocument;
import com.onpositive.semantic.wikipedia2.WikiEngine2;
import com.onpositive.semantic.wikipedia2.catrelations.BasicConnectionFinder;
import com.onpositive.semantic.wikipedia2.catrelations.Connection;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.wikipedia.workbench.TreeBuilder.Node;
import com.onpositive.wikipedia.workbench.words.primary.WikiEngineProvider;
import com.onpositive.wikipedia.workbench.words.primary.WordDefinitions;

public class BasicCategorizationTest extends TestCase {
	
private static final String CONTENT_TAG = "<!--CONTENT-->";

//	public void test00() {
//		testForStartingWith("А");
//	}
	
//	public void test01() {
//		testForStartingWith("Транспорт");
//	}
	
	public void test02() {
		testForStartingWith("Самолёты_Антонова");
	}
	
	private ArrayList<TextElement> getKeywords(String title) {
		WikiEngine2 service = WikiEngineProvider.getInstance();
		IDocument document = service.getDocumentByTitle(title);
		assertNotNull(document);
		WordDefinitions wordDefinitions = new WordDefinitions(service);
		wordDefinitions.processDocument(service, document.getIntId());
		ArrayList<TextElement> elements = wordDefinitions.getById(document.getIntId());
		return elements;
	}
	
	public void test03() {
		ArrayList<TextElement> elements = getKeywords("Ан-2");
		assertTrue(containsWords(elements, "советский", "лёгкий", "многоцелевой", "самолёт"));
	}

	
	public void test04() {
		ArrayList<TextElement> elements = getKeywords("Ан-3");
		assertTrue(containsWords(elements, "самолёт"));
	}
	
	public void test05() {
		ArrayList<TextElement> elements = getKeywords("Ан-12");
		assertTrue(containsWords(elements, "самолёт"));
	}
	
	private boolean containsWords(ArrayList<TextElement> elements, String... words) {
		for (String string : words) {
			if (!elements.stream().anyMatch(element -> element.getBasicForm().equals(string))) {
				return false;
			}
		}
		return true;
	}

//	public void test04() {
//		WikiEngine2 service = WikiEngineProvider.getInstance();
//		int[] documentIDs = service.getDocumentIDs();
//		Set<String> shortened = new HashSet<>();
//		for (int i : documentIDs) {
//			WikiDoc document = service.getDocument(i);
//			String abstract1 = document.getPlainTextAbstract();
//			shortened.addAll(findShortened(abstract1));
//		}
//		List<String> list = new ArrayList<String>(shortened);
//		Collections.sort(list);
//		
//		try {
//			Writer writer = new BufferedWriter(new FileWriter("shortened.txt")); 
//			for(String str: list) {
//			  writer.write(str);
//			}
//			writer.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private Collection<String> findShortened(String text) {
		List<String> shortened = new ArrayList<>();
		int level = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '(') {
				level++;
			}
			else if (text.charAt(i) == ')') {
				level = Math.max(0, level - 1);
			} else if (level > 0 && text.charAt(i) == '.') {
				int j = i - 1;
				for (; j >= 0 && isWordPart(text.charAt(j)); j--);
				if (j < i) {
					String word = text.substring(j + 1, i);
					shortened.add(word);
				}
				
			}
			
		}
		return shortened;
		
	}

	private boolean isWordPart(char ch) {
		return Character.isLetter(ch) || Character.isDigit(ch) || ch == '-';
	}

	protected void testForStartingWith(String str) {
		ICategory[] categories = WikiEngineProvider.getInstance().getCategories(str);
		List<ICategorizable> entities = new ArrayList<ICategorizable>();
		for (ICategory curCategory : categories) {
			entities.addAll(Arrays.asList(curCategory.getPages()));
//			entities.addAll(Arrays.asList(curCategory.getSubCategories()));
		}
		BasicConnectionFinder finder = new BasicConnectionFinder() {
			public String getStringRepresenation() {
				TreeMap<Long, Connection> sortedMap = new TreeMap<>((Long l1, Long l2) -> 
					foundConnections.get(l1).toString().compareTo(foundConnections.get(l2).toString()));
				sortedMap.putAll(foundConnections);
				StringBuilder builder = new StringBuilder();
				sortedMap.keySet().forEach(key -> {
					builder.append(foundConnections.get(key).toString());
					String meta = SimpleConnectionMetadataProvider.get(foundConnections.get(key));
					if (meta != null) {
						builder.append(" ");
						builder.append(meta);
					}
					builder.append("\n");
				});
				return builder.toString();
			}
			
			@Override
			protected void iterationFinished(int interation) {
				String stringRepresenation = getStringRepresenation();
				try {
					Files.write( Paths.get("categorized" + interation + ".txt"), stringRepresenation.getBytes(), StandardOpenOption.CREATE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		finder.addConnectionFactory(new BasicConnectionFactory());
		finder.findConnections(entities.toArray(new ICategorizable[0]));
		String stringRepresenation = finder.getStringRepresenation();
		try {
			new TreeBuilder().processStream(Arrays.stream(stringRepresenation.split("\n")));
			
			Node rootNode = new TreeBuilder().calculateRootNode(Arrays.stream(stringRepresenation.split("\n")));
			writeTreeHTML(rootNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(stringRepresenation);
	}

	private void writeTreeHTML(Node rootNode) throws IOException {
		String content = new String(Files.readAllBytes(Paths.get("template.html")));
		int insertionPoint = content.indexOf(CONTENT_TAG);
		if (insertionPoint < 0) {
			System.err.println("Incorrect template: no " + CONTENT_TAG + " inside");
			return;
		}
		String head = content.substring(0, insertionPoint);
		String end = content.substring(insertionPoint + CONTENT_TAG.length());
		
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
//		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("cats.html")));
//		writer.println("<html>");
//		writer.println("<head>");
//		writer.println("<meta charset=\"UTF-8\"/>");
//		writer.println("<script type=\"text/javascript\" src=\"CollapsibleLists.js\"></script>");
//		writer.println("</head>");
//		writer.println("<body>");
//		writer.println("<ul class=\"collapsibleList\">");
		for (Node node : rootNode.children) {
			printTags(writer, node);
		}
//		writer.println("</ul>");
//		writer.println("</body>");
//		writer.println("</html>");
		writer.close();
		String html = head + stringWriter.toString() + end;
		
		Writer out = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream("cats.html"), "UTF-8"));
		try {
		    out.write(html);
		} finally {
		    out.close();
		}
	}

	private void printTags(PrintWriter writer, Node node) {
		writer.println("<li>");
		writer.println(node.name);
		if (node.children.size() > 0) {
			writer.println("<ul>");
			for (Node childNode : node.children) {
				printTags(writer, childNode);
			}
			writer.println("</ul>");
		}
		writer.println("</li>");
	}
	
	

}
