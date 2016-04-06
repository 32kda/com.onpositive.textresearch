package com.onpositive.semantic.wordnet.edit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.words3.ReadOnlyMapWordNet;
import com.onpositive.semantic.words3.ReadOnlyWordNet;

public class WordNetAutoPatcher {
	
	private static final String PATCHED_WORDNET_HEADER = "PATCHED_WORDNET";
	
	/**
	 * Obtains wordnet from location specified in System Properties in "engineConfigDir"
	 * property & patches it with xml's located in same folder with it
	 * @return Resulting wordnet
	 */
	public static ReadOnlyWordNet obtainWordNet() {
		String property = System.getProperty(WordNetProvider.ENGINE_CONFIG_DIR_PROP);
		if (property != null) {
			File file = new File(property, WordNetProvider.MAP_WORDNET_FILE_NAME);
			File parentDir = new File(property);
			if (!file.exists() && parentDir.exists() && parentDir.isDirectory()) {
				File[] files = parentDir.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".dat");
					}
				});
				for (File curFile : files) {
					if (curFile.isFile()) {
						file = files[0];
						break;
					}
				}
			}
			if (file.exists()) {
				return obtainWordNet(file);
			}
		}
		return null;
	}
	
	public static ReadOnlyWordNet obtainWordNet(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("File argument shouldn't be null & should point to existing file");
		}
		return obtainWordNet(file, file.getParentFile());
	}
	

	public static ReadOnlyWordNet obtainWordNet(File file, File xmlDir) {
		if (file == null || !file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("File argument shouldn't be null & should point to existing file");
		}
		if (xmlDir == null || !xmlDir.exists() || !xmlDir.isDirectory()) {
			throw new IllegalArgumentException("xmlDir argument shouldn't be null & should point to existing folder");
		}
		File[] modifiers = xmlDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		Map<String, Long> modifiersMap = new HashMap<String, Long>();
		for (File curFile : modifiers) {
			modifiersMap.put(curFile.getName(), curFile.lastModified());
		}
		DataInputStream stream = null;
		try {
			stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			stream.mark(0);
			String header = stream.readUTF();
			if (!header.equals(PATCHED_WORDNET_HEADER)) {
				return rewriteWordNet(file, stream, modifiers, modifiersMap);
			} else {
				int oldCount = stream.readInt();
				if (oldCount != modifiers.length) {
					return rewriteWordNet(file, stream, modifiers, modifiersMap);
				}
				for (int i = 0; i < oldCount; i++) {
					String fileName = stream.readUTF();
					long lastModified = stream.readLong();
					if (!modifiersMap.get(fileName).equals(lastModified)) {
						return rewriteWordNet(file, stream, modifiers ,modifiersMap);
					}
				}
				return ReadOnlyMapWordNet.load(stream);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {stream.close();} catch (IOException e) {}
			}
		}
		return null;
		
	}

	private static ReadOnlyWordNet rewriteWordNet(File file, DataInputStream stream, File[] modifiers,
			Map<String, Long> modifiersMap) {
		if (stream == null) {
			return null;
		}
		DataOutputStream outputStream = null;
		try {
			stream.reset();
			ReadOnlyMapWordNet mapWordNet = new ReadOnlyMapWordNet(new DataInputStream(stream));
			IWordNetEditInterface editableNet = WordNetProvider.editable(mapWordNet);
			for (File curFile : modifiers) {
				WordNetPatch wordNetPatch = WordNetPatch.parse(new BufferedReader(new InputStreamReader(new FileInputStream(curFile),"UTF-8")));
				wordNetPatch.execute(editableNet);
			}
			stream.close();
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			outputStream.writeUTF(PATCHED_WORDNET_HEADER);
			outputStream.writeInt(modifiers.length);
			for (File curFile: modifiers) {
				outputStream.writeUTF(curFile.getName());
				outputStream.writeLong(curFile.lastModified());
			}
			editableNet.store(outputStream);
			outputStream.close();
			stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			
			stream.readUTF(); //Skip header
			int count = stream.readInt();
			for (int i = 0; i < count; i++) {
				stream.readUTF();
				stream.readLong();
			}
			
			return ReadOnlyMapWordNet.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {stream.close();} catch (IOException e) {}
			}
			if (outputStream != null) {
				try {outputStream.close();} catch (IOException e) {}
			}
		}
		return null;
	}

	public static ReadOnlyWordNet obtainWordNet(String fileName) {
		return obtainWordNet(new File(fileName));
	}

}
