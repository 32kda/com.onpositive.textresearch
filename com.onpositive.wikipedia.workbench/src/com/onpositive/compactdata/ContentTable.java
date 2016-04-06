package com.onpositive.compactdata;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.carrotsearch.hppc.IntLongOpenHashMap;
import com.onpositive.wikipedia.dumps.builder.IPageVisitor;
import com.onpositive.wikipedia.dumps.builder.PageModel;

public class ContentTable implements IPageVisitor {

	protected DataOutputStream s;

	protected IntLongOpenHashMap positions = new IntLongOpenHashMap();
	protected int up = 0;
	File ps;

	public ContentTable(String path) throws IOException {
		ps = new File(path);
		ps.mkdirs();
		s = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
				new File(ps, "content.dat"))));
	}

	public void close() throws IOException {
		s.close();
		DataOutputStream str = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(ps, "content.list"))));
		CompactLongVector.writeIntLongMap(str,positions);
		str.close();
	}

	long k = 0;

	
	protected void onPage(PageModel page) {
		if (page.getNamespace()!=0){
			return;
		}
		positions.put(page.getPageId(),k);
		up++;
		try {
			byte[] bytes = page.getText().getBytes("UTF-8");
			s.writeInt(bytes.length);
			s.write(bytes);
			k += (bytes.length + 4);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visit(PageModel model) {
		onPage(model);
	}

}
