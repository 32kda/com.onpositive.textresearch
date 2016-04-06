package com.onpositive.text.analisys.tools.exec;

import java.io.File;

import com.onpositive.text.analisys.tools.VerbsCaseExtractor;
import com.onpositive.text.analisys.tools.data.FileTextLoader;
import com.onpositive.text.analisys.tools.data.LogWriter;

public class ExtractCases {

	public static void main(String[] args) {
		
		File rootFolder = new File("H:/!distrib/lib/lib.ru/book");///ADAMS/New folder");
		String errLogPath = "C:/workspaces/TextAnalysis/com.onpositive.text.analisys.tools/dump/errLog.txt";
		String resultFilePath = "C:/workspaces/TextAnalysis/com.onpositive.text.analisys.tools/dump/result.txt";
		String progressFilePath = "C:/workspaces/TextAnalysis/com.onpositive.text.analisys.tools/dump/progress.txt";
		VerbsCaseExtractor verbsCaseExtractor = new VerbsCaseExtractor(new FileTextLoader("windows-1251"), "html", "htm");
		
		LogWriter errLog = new LogWriter(errLogPath);
		LogWriter resultLog = new LogWriter(resultFilePath);
		LogWriter progressLog = new LogWriter(progressFilePath);
		errLog.reset();
		resultLog.reset();
		progressLog.reset();
		
		verbsCaseExtractor.setErrLog(errLog);		
		verbsCaseExtractor.setResultLog(resultLog);
		verbsCaseExtractor.setProgressLog(progressLog);
		
		verbsCaseExtractor.visit(rootFolder);

	}

}
