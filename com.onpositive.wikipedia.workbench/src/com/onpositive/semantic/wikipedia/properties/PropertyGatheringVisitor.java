package com.onpositive.semantic.wikipedia.properties;

import java.util.ArrayList;

import org.sweble.wikitext.parser.nodes.WtName;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateArguments;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtValue;


import de.fau.cs.osr.ptk.common.AstVisitor;

public class PropertyGatheringVisitor extends AstVisitor<WtNode> {

	ArrayList<PropertyInfo> infos = new ArrayList<PropertyInfo>();

	public void visit(WtTemplate template) {
		WtName name = template.getName();
		WtTemplateArguments args = template.getArgs();
		int sz = args.size();
		if (name.isResolved()) {
			String templateName=name.getAsString();
			for (int a = 0; a < sz; a++) {
				WtNode wtNode = args.get(a);
				if (wtNode instanceof WtTemplateArgument) {
					WtTemplateArgument argument = (WtTemplateArgument) wtNode;
					WtName name2 = argument.getName();
					if (name2.isResolved()) {
						String tName = name2.getAsString();
						String valueText = null;
						boolean isSimple = true;
						WtValue value = argument.getValue();
						if (value.size() == 1) {
							if (value.get(0) instanceof WtText) {
								WtText ts = (WtText) value.get(0);
								valueText = ts.getContent();
							}
						}
						if (valueText == null) {
							StringBuildingVisitor stringBuildingVisitor = new StringBuildingVisitor();
							stringBuildingVisitor.go(value);
							valueText = stringBuildingVisitor.bld.toString();
							isSimple=false;
						}
						if (valueText!=null&&valueText.trim().length()>0){
							infos.add(new PropertyInfo(templateName, tName, valueText.trim(), isSimple,null));
						}
					}
				}
			}
		}
	}

	public static class StringBuildingVisitor extends AstVisitor<WtNode> {

		StringBuilder bld = new StringBuilder();

		public void visit(WtText template) {
			bld.append(template.getContent()+" ");
		}

		protected Object visitNotFound(WtNode node) {
			int size = node.size();
			for (int a = 0; a < size; a++) {
				dispatch(node.get(a));
			}
			return node;
		}
	}

	protected Object visitNotFound(WtNode node) {
		int size = node.size();
		for (int a = 0; a < size; a++) {
			dispatch(node.get(a));
		}
		return node;
	}
}
