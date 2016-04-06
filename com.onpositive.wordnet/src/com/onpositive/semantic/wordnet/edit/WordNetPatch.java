package com.onpositive.semantic.wordnet.edit;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.SemanticRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.words3.IntBooleanLayer;
import com.onpositive.semantic.words3.IntByteLayer;
import com.onpositive.semantic.words3.IntDoubleLayer;
import com.onpositive.semantic.words3.IntIntLayer;
import com.onpositive.semantic.words3.IntMeaningLayer;
import com.onpositive.semantic.words3.IntStringLayer;
import com.onpositive.semantic.words3.MetaLayer;

public class WordNetPatch {

	protected ArrayList<AbstractOperation> toExecute = new ArrayList<WordNetPatch.AbstractOperation>();

	public void execute(IWordNetEditInterface ei) {
		for (AbstractOperation op : toExecute) {
			op.execute(ei);
		}
	}

	protected static abstract class AbstractOperation {
		protected abstract void execute(IWordNetEditInterface net);

		public abstract Node append(Document appendChild);
	}

	static final String WC = "wc";
	static final String LAYER = "meta-layer";
	static final String META_COMMAND = "meta";
	static final String CAPTION = "caption";
	static final String ID = "id";
	static final String REMOVAL = "removal";
	
	public static class LayerOperation extends AbstractOperation {

		private static final String TYPE = "type";
		public final String layerId;
		public final String caption;
		public final String layerType;
		public final boolean remove;
		public LayerOperation(String layerId, String caption, String layerType,
				boolean remove) {
			super();
			this.layerId = layerId;
			this.caption = caption;
			this.layerType = layerType;
			this.remove = remove;
		}
		public LayerOperation(Node item){
			NamedNodeMap attributes = item.getAttributes();
			this.layerId = attributes.getNamedItem(ID).getNodeValue();
			Node nm = attributes.getNamedItem(REMOVAL);
			if (nm != null) {
				this.remove = Boolean.parseBoolean(nm.getNodeValue());
			}
			else{
				this.remove=false;
			}
			if (!this.remove){
				this.caption = attributes.getNamedItem(CAPTION).getNodeValue();
				this.layerType = attributes.getNamedItem(TYPE).getNodeValue();
			}
			else{
				this.caption=null;
				this.layerType=null;
				
			}
		}
		
		@Override
		public Node append(Document appendChild) {
			String tagName = LAYER;
			Element createElement = appendChild.createElement(tagName);
			createElement.setAttribute(ID, layerId);
			if (!remove){
			createElement.setAttribute(CAPTION,caption);
			createElement.setAttribute(TYPE,layerType);
			}
			else{
			createElement.setAttribute(REMOVAL, ""+remove);
			}
			return createElement;
		}
		
		@Override
		protected void execute(IWordNetEditInterface net) {
			if(remove){
				net.getWordNet().getMetaLayers().removeLayer(layerId);
			}
			else{
				MetaLayer<?> layer = net.getWordNet().getMetaLayers().getLayer(layerId);
				Class<?>type=convertType(layerType);
				if (layer!=null){
					if (layer.getType()==type){
						return;
					}
					else{
						throw new IllegalStateException("layer  with id "+layerId+" already exists with type "+type.getSimpleName());
					}
				}
				layer = createLayer(net, layer, type);
				net.getWordNet().getMetaLayers().registerLayer(layer);
			}
		}
		MetaLayer<?> createLayer(IWordNetEditInterface net, MetaLayer<?> layer,
				Class<?> type) {
			if (type==Byte.class){
				layer=new IntByteLayer(layerId, caption);
			}
			if (type==Integer.class){
				layer=new IntIntLayer(layerId, caption);
			}
			if (type==Double.class){
				layer=new IntDoubleLayer(layerId, caption);
			}
			if (type==Boolean.class){
				layer=new IntBooleanLayer(layerId, caption);
			}
			if (type==String.class){
				layer=new IntStringLayer(layerId, caption);
			}
			if (type==MeaningElement.class){
				layer=new IntMeaningLayer(layerId, caption,net.getWordNet());
			}
			return layer;
		}

		private Class<?> convertType(String layerType2) {
			try {
				if (layerType2.equals("concept")){
					return MeaningElement.class;
				}
				return Class.forName("java.lang."+Character.toUpperCase(layerType2.charAt(0))+layerType2.substring(1));
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}

		
		
	}
	
	public static class MetaEditOperation extends AbstractOperation {

		public final String word;
		public boolean removal;
		public final String value;
		public String layer;

		public MetaEditOperation(String word, boolean removal,String value,String layer) {
			super();
			this.word = word;
			this.removal = removal;
			this.value=value;
			this.layer=layer;
		}

		public MetaEditOperation(Node item) {
			NamedNodeMap attributes = item.getAttributes();
			this.word = attributes.getNamedItem("w").getNodeValue();
			this.layer=attributes.getNamedItem("l").getNodeValue();
			Node nm = attributes.getNamedItem("removal");
			if (nm != null) {
				this.removal = Boolean.parseBoolean(nm.getNodeValue());
			}
			if (!this.removal){
			this.value= attributes.getNamedItem("v").getNodeValue();
			}
			else{
				this.value=null;
			}
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			TextElement wordElement = net.getWordNet().getWordElement(word);
			if (wordElement==null){
				wordElement=net.registerWord(word);
			}
			int id=wordElement.getConcepts()[0].id();
			MetaLayer<Object> layer2 = net.getWordNet().getMetaLayers().getLayer(layer);
			if (removal) {
				layer2.removeValue(id);
			} else {
				Class<Object> type = layer2.getType();
				Object val=null;
				if (type.equals(Byte.class)){
					val=Byte.parseByte(value);
				}
				if (type.equals(Double.class)){
					val=Double.parseDouble(value);
				}
				if (type.equals(Integer.class)){
					val=Integer.parseInt(value);
				}
				if (type.equals(Boolean.class)){
					val=Boolean.parseBoolean(value);
				}
				if (type.equals(String.class)){
					val=value;
				}
				if (type.equals(MeaningElement.class)){
					TextElement wordElement2 = net.getWordNet().getWordElement(value);
					val=wordElement2.getConcepts()[0];					
				}
				layer2.putValue(id, val);
			}
		}

		@Override
		public Node append(Document appendChild) {
			String tagName = META_COMMAND;
			Element createElement = appendChild.createElement(tagName);
			createElement.setAttribute("w", word);
			createElement.setAttribute("l", layer);
			if (value!=null&&!removal){
			createElement.setAttribute("v", value);
			}
			if (removal) {
				createElement.setAttribute("removal", "" + removal);
			}
			return createElement;
		}
	}

	public static class WordOperation extends AbstractOperation {

		public final String word;
		public boolean removal;

		public WordOperation(String word, boolean removal) {
			super();
			this.word = word;
			this.removal = removal;
		}

		public WordOperation(Node item) {
			NamedNodeMap attributes = item.getAttributes();
			this.word = attributes.getNamedItem("w").getNodeValue();
			Node nm = attributes.getNamedItem("removal");
			if (nm != null) {
				this.removal = Boolean.parseBoolean(nm.getNodeValue());
			}
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				net.removeWord(word);
			} else {
				net.registerWord(word);
			}
		}

		@Override
		public Node append(Document appendChild) {
			String tagName = WC;
			Element createElement = appendChild.createElement(tagName);
			createElement.setAttribute("w", word);
			if (removal) {
				createElement.setAttribute("removal", "" + removal);
			}
			return createElement;
		}
	}

	public abstract static class RelationOperation extends AbstractOperation {
		public final String from;
		public final String to;
		public final int code;
		boolean removal;

		public RelationOperation(String from, String to, boolean removal,
				int code) {
			super();
			this.from = from;
			this.to = to;
			this.removal = removal;
			this.code = code;
		}

		public RelationOperation(Node item) {
			NamedNodeMap attributes = item.getAttributes();
			this.from = attributes.getNamedItem("f").getNodeValue();
			this.to = attributes.getNamedItem("t").getNodeValue();
			Node nm = attributes.getNamedItem("removal");
			if (nm != null) {
				this.removal = Boolean.parseBoolean(nm.getNodeValue());
			}
			Node namedItem = attributes.getNamedItem("r");
			this.code = parseCode(namedItem.getNodeValue());
		}

		protected abstract int parseCode(String nodeValue);

		public abstract String getTag();

		@Override
		public Node append(Document appendChild) {
			Element createElement = appendChild.createElement(getTag());
			createElement.setAttribute("f", from);
			createElement.setAttribute("t", to);
			if (removal) {
				createElement.setAttribute("removal", "" + removal);
			}
			createElement.setAttribute("r", getRelationString(code));
			return createElement;
		}

		protected abstract String getRelationString(int code);
	}

	private static final String SC = "sc";

	public static class SemanticRelationOperation extends RelationOperation {

		@Override
		protected int parseCode(String nodeValue) {
			Class<SemanticRelation> class1 = SemanticRelation.class;
			return parse(nodeValue, class1);
		}

		public SemanticRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		public SemanticRelationOperation(Node item) {
			super(item);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			if (removal) {
				MeaningElement from = getOrCreate(net, this.from);
				MeaningElement to = getOrCreate(net, this.to);
				SemanticRelation tt = new SemanticRelation(net.getWordNet(),
						to.id(), code);
				SemanticRelation tt1 = new SemanticRelation(net.getWordNet(),
						from.id(), backlink(code));
				net.removeSemanticRelation(from, tt);
				net.removeSemanticRelation(to, tt1);
			} else {
				MeaningElement from = getOrCreate(net, this.from);
				MeaningElement to = getOrCreate(net, this.to);
				SemanticRelation tt = new SemanticRelation(net.getWordNet(),
						to.id(), code);
				SemanticRelation tt1 = new SemanticRelation(net.getWordNet(),
						from.id(), backlink(code));
				net.addSemanticRelation(from, tt);
				net.addSemanticRelation(to, tt1);
			}
		}

		private int backlink(int code) {
			switch (code) {
			case SemanticRelation.ANTONIM:
				return SemanticRelation.ANTONIM_BACKLINK;
			case SemanticRelation.ANTONIM_BACKLINK:
				return SemanticRelation.ANTONIM;
			case SemanticRelation.SYNONIM:
				return SemanticRelation.SYNONIM_BACK_LINK;
			case SemanticRelation.SYNONIM_BACK_LINK:
				return SemanticRelation.SYNONIM;
			case SemanticRelation.MERONIM:
				return SemanticRelation.MERONIM_BACKLINK;
			case SemanticRelation.MERONIM_BACKLINK:
				return SemanticRelation.MERONIM;
			case SemanticRelation.GENERALIZATION:
				return SemanticRelation.SPECIALIZATION_BACK_LINK;
			case SemanticRelation.GENERALIZATION_BACK_LINK:
				return SemanticRelation.SPECIALIZATION;
			case SemanticRelation.SPECIALIZATION:
				return SemanticRelation.GENERALIZATION_BACK_LINK;
			case SemanticRelation.SPECIALIZATION_BACK_LINK:
				return SemanticRelation.GENERALIZATION;
			default:
				break;
			}
			return 0;
		}

		@Override
		public String getTag() {
			return SC;
		}

		@Override
		protected String getRelationString(int code) {
			Class<SemanticRelation> class1 = SemanticRelation.class;
			return toStringFromCode(code, class1);
		}

	}

	static final String MC = "mc";

	public static class MorphologicalRelationOperation extends
			RelationOperation {

		public MorphologicalRelationOperation(String from, String to,
				boolean removal, int code) {
			super(from, to, removal, code);
		}

		public MorphologicalRelationOperation(Node item) {
			super(item);
		}

		@Override
		protected void execute(IWordNetEditInterface net) {

			if (removal) {
				MeaningElement from = getOrCreate(net, this.from);
				MeaningElement to = getOrCreate(net, this.to);
				MorphologicalRelation tt = new MorphologicalRelation(
						net.getWordNet(), to.id(), code);
				MorphologicalRelation tt1 = new MorphologicalRelation(
						net.getWordNet(), from.id(), code
								+ MorphologicalRelation.BACK_LINK_OFFSET);
				net.removeMorphologicalRelation(from, tt);
				net.removeMorphologicalRelation(to, tt1);
			} else {
				MeaningElement from = getOrCreate(net, this.from);
				MeaningElement to = getOrCreate(net, this.to);
				MorphologicalRelation tt = new MorphologicalRelation(
						net.getWordNet(), to.id(), code);
				MorphologicalRelation tt1 = new MorphologicalRelation(
						net.getWordNet(), from.id(), code
								+ MorphologicalRelation.BACK_LINK_OFFSET);
				net.addMorphologicalRelation(from, tt);
				net.addMorphologicalRelation(to, tt1);
			}
		}

		@Override
		public String getTag() {
			return MC;
		}

		@Override
		protected String getRelationString(int code) {
			Class<MorphologicalRelation> class1 = MorphologicalRelation.class;
			return toStringFromCode(code, class1);
		}

		@Override
		protected int parseCode(String nodeValue) {
			Class<MorphologicalRelation> class1 = MorphologicalRelation.class;
			return parse(nodeValue, class1);
		}

	}

	static final String GC = "gc";

	public static class GrammarRelationOperation extends RelationOperation {

		public final String grammems;

		public GrammarRelationOperation(String from, String to,
				boolean removal, String grammems) {
			super(from, to, removal, 0);
			this.grammems = grammems;
		}

		public GrammarRelationOperation(Node item) {
			super(item);
			this.grammems = item.getAttributes().getNamedItem("r")
					.getNodeValue();
		}

		@Override
		protected void execute(IWordNetEditInterface net) {
			MeaningElement to = getOrCreate(net, this.to);
			LinkedHashSet<Grammem> code = getCode(grammems, net);
			if (removal) {

				net.removeGrammarRelation(from, to.getParentTextElement(), code);
			} else {
				net.addGrammarRelation(from, to.getParentTextElement(), code);
			}
		}

		private LinkedHashSet<Grammem> getCode(String grammems2,
				IWordNetEditInterface net) {
			String[] split = grammems.split(",");
			LinkedHashSet<Grammem> ms = new LinkedHashSet<Grammem>();
			for (String s : split) {
				Grammem grammem = Grammem.get(s.trim());
				if (grammem == null) {
					throw new IllegalStateException("unknown grammem");
				}
				ms.add(grammem);
			}

			return ms;
		}

		@Override
		public String getTag() {
			return GC;
		}

		@Override
		protected String getRelationString(int code) {
			return grammems;
		}

		@Override
		protected int parseCode(String nodeValue) {
			return 0;
		}
	}

	private static MeaningElement getOrCreate(IWordNetEditInterface net,
			String from2) {
		TextElement wordElement = net.getWordNet().getWordElement(from2);

		if (wordElement == null) {
			wordElement = net.registerWord(from2);
		}
		return wordElement.getConcepts()[0];
	}

	static int parse(String nodeValue, Class<?> class1) {
		Field[] fields = class1.getFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) {
				if (f.getType() == int.class) {
					if (f.getName().toLowerCase().equals(nodeValue)) {
						try {
							return f.getInt(null);
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}

				}
			}
		}
		throw new IllegalArgumentException("Unknown relation:" + nodeValue);
	}

	static String toStringFromCode(int code, Class<?> class1) {
		Field[] fields = class1.getFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) {
				if (f.getType() == int.class) {
					try {
						if (f.getInt(null) == code) {
							return f.getName().toLowerCase();
						}
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
		return null;
	}

	public static WordNetPatch parse(Reader reader) throws Exception {
		Document parse = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(new InputSource(reader));
		WordNetPatch patch = new WordNetPatch();
		Element documentElement = parse.getDocumentElement();
		processNodes(patch, documentElement, null);
		return patch;
	}

	static void processNodes(WordNetPatch patch, Element documentElement,
			NamedNodeMap extraAttrs) {
		NodeList childNodes = documentElement.getChildNodes();
		int length = childNodes.getLength();
		for (int a = 0; a < length; a++) {
			Node item = childNodes.item(a);
			if (item instanceof Element) {
				if (item.getNodeName().equals("gr")) {
					NamedNodeMap attributes = item.getAttributes();
					processNodes(patch, (Element) item, attributes);
					continue;
				}
				if (extraAttrs != null) {
					for (int i = 0; i < extraAttrs.getLength(); i++) {
						Node item2 = extraAttrs.item(i);
						((Element) item).setAttribute(item2.getNodeName(), item2.getNodeValue());
					}
				}
				AbstractOperation op = createCommand(item);
				if (op == null) {
					throw new IllegalArgumentException("unknown tag:"
							+ item.getNodeName());
				} else {
					patch.toExecute.add(op);
				}
			}
		}
	}

	private static AbstractOperation createCommand(Node item) {
		if (item instanceof Element) {
			Element el = (Element) item;
			if (el.getNodeName().equals(WC)) {
				return new WordOperation(item);
			}
			if (el.getNodeName().equals(LAYER)) {
				return new LayerOperation(item);
			}
			if (el.getNodeName().equals(META_COMMAND)) {
				return new MetaEditOperation(item);
			}
			if (el.getNodeName().equals(SC)) {
				return new SemanticRelationOperation(item);
			}
			if (el.getNodeName().equals(MC)) {
				return new MorphologicalRelationOperation(item);
			}
			if (el.getNodeName().equals(GC)) {
				return new GrammarRelationOperation(item);
			}
		}
		return null;
	}

	public void store(Writer wr) throws Exception {
		Document newDocument = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();
		Element createElement = newDocument.createElement("wnc");
		Node appendChild = newDocument.appendChild(createElement);
		for (AbstractOperation o : this.toExecute) {
			Node append = o.append(newDocument);
			appendChild.appendChild(append);
		}
		Transformer newTransformer = TransformerFactory.newInstance()
				.newTransformer();
		newTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
		newTransformer.transform(new DOMSource(newDocument), new StreamResult(
				wr));
	}

	public int size() {
		return toExecute.size();
	}
}
