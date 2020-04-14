import org.omegat.core.dictionaries.DictionariesManager;
import org.omegat.core.dictionaries.DictionaryEntry;
import org.omegat.core.dictionaries.IDictionary;
import org.omegat.core.dictionaries.StarDict;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.tokenizer.LuceneRussianTokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cleaner {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No input file specified! Usage: cleaner <input file> <dict file> (.ifo)");
            return;
        } else if (!new File(args[0]).isFile()) {
            System.err.println("Invalid input file " + args[0]);
            return;
        }
        if (args.length < 2) {
            System.err.println("No dictionary file specified! Usage: cleaner <inpu file> <dict file> (.ifo)");
            return;
        } else if (!new File(args[1]).isFile()) {
            System.err.println("Invalid dictionary file " + args[1]);
            return;
        }
        File input = new File(args[0]);
        File output = new File(input.getParent(), input.getName() + ".out");

        File dict = new File(args[1]);
        double similarityCoef = 0.3;
        try (PrintStream printStream = new PrintStream(new FileOutputStream(dict))) {
            Files.lines(input.toPath()).map(str -> str.split("\\t")).filter(strings -> strings.length > 1).forEach(strings -> {
                String en = strings[0];
                String ru = strings[1];
                LuceneEnglishTokenizer englishTokenizer = new LuceneEnglishTokenizer();
                LuceneRussianTokenizer russianTokenizer = new LuceneRussianTokenizer();
                int enWordCount = englishTokenizer.tokenizeWordsToStrings(en, ITokenizer.StemmingMode.GLOSSARY).length;
                int ruWordCount = russianTokenizer.tokenizeWordsToStrings(ru, ITokenizer.StemmingMode.GLOSSARY).length;
                boolean matchQ = en.indexOf('?') > 0 == ru.indexOf('?') > 0;
                boolean matchY = en.indexOf('!') > 0 == ru.indexOf('!') > 0;
                int diff = Math.abs(enWordCount - ruWordCount);
                boolean matchLen = diff * 1.0 / Math.max(enWordCount, ruWordCount) <= similarityCoef;
                if (matchQ && matchY && matchLen) {
                    printStream.println(strings[0] + "\t" + strings[1]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
