package com.onpositive.wikipedia.dumps.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class WikiSqlLoader {

	public void parse(BufferedReader r, ISqlConsumer consumer)
			throws IOException {
		while (true) {
			String readLine = r.readLine();
			if (readLine == null) {
				break;
			}

			if (readLine.startsWith("INSERT INTO")) {
				readLine = readLine.substring(readLine.indexOf('('));
				ArrayList<String>subPars=new ArrayList<String>();
				int pos=0;
				while (true){
					int indexOf = readLine.indexOf("),(",pos);
					if (indexOf!=-1){
						subPars.add(readLine.substring(pos,indexOf));
						pos=indexOf+3;						
					}
					else{
						subPars.add(readLine.substring(pos));
						break;
					}
				}
				//String[] split = readLine.split("\\)\\,\\(");
				String[] mm=subPars.toArray(new String[subPars.size()]);
//				if (!Arrays.equals(mm, split)){
//					throw new IllegalStateException();
//				}
				int a=0;
				for (String s : mm) {
					consume(s, consumer);
					a++;
				}
			}
		}

	}

	StringBuilder db = new StringBuilder();

	private void consume(String s, ISqlConsumer consumer) {
		ArrayList<String> sm = new ArrayList<String>();
		if (s.charAt(0) == '(') {
			s = s.substring(1);
		}
		int length = s.length();
		db.delete(0, db.length());
		char quote = 0;
		char pc = 0;
		for (int a = 0; a < length; a++) {
			char c = s.charAt(a);

			if (c == '\'') {
				if (quote == '\'') {
					if (pc != '\\') {
						quote = 0;
					}
				} else {
					if (quote == 0) {
						quote = c;
					}
				}
			}
			if (c == '\"') {
				if (quote == '\"') {
					if (pc != '\\') {
						quote = 0;
					}
				} else {
					if (quote == 0) {
						quote = c;
					}
				}
			}
			if (c == ',' && quote == 0) {
				sm.add(db.toString());
				db.delete(0, db.length());
				continue;
			}
			db.append(c);
			if (pc != '\\') {
				pc = c;
			} else {
				pc = 0;
			}
		}
		sm.add(db.toString());
		Object[] rs = new Object[sm.size()];
		int a = 0;
		for (String sq : sm) {
			if (sq.endsWith(");")) {
				sq = sq.substring(0, sq.length() - 2);
			}
			try {
				if (sq.length()>0&&Character.isDigit(sq.charAt(0))){
				int parseLong = Integer.valueOf(sq);
				rs[a++] = parseLong;
				continue;
				}
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
			if (sq.charAt(0) == '\'') {
				rs[a++] = sq.substring(1, sq.length() - 1);
				continue;
			} else {
				if (sq.charAt(0) == '"') {
					rs[a++] = sq.substring(1, sq.length() - 1);
					continue;
				}
			}
			rs[a] = sq;
		}
		try{
		consumer.consume(rs);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			String[] list = new File("C:\\ruwiktionary").list();
			System.out.println(list);
			FileInputStream s = new FileInputStream(
					"C:\\ruwiktionary\\ruwiktionary-20140205-categorylinks.sql");
			BufferedReader d = new BufferedReader(new InputStreamReader(s,
					Charset.forName("UTF-8")));

			new WikiSqlLoader().parse(d, new ISqlConsumer() {

				int count;

				@Override
				public void consume(Object[] data) {
					new CategoryLinkModel(data);
					count++;
					System.out.println(count);
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}