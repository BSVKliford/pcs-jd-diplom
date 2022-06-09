import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;


public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> wordsMap = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File[] folderEntries = pdfsDir.listFiles();
        for (File file : folderEntries) {
            if (!file.isDirectory()) {
                var doc = new PdfDocument(new PdfReader(file));
                int numberOfPages = doc.getNumberOfPages();
                //System.out.println(numberOfPages);
                for (int i = 1; i <= numberOfPages; i++) {
                    var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>();
                    for (var word : words) {
                        if (word.isEmpty()) {
                            continue;
                        }
                        freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
                    }
                    for (Map.Entry<String, Integer> entry : freqs.entrySet()) {

                        List<PageEntry> pageEntryList;
                        if (wordsMap.containsKey(entry.getKey())) {
                            pageEntryList = wordsMap.get(entry.getKey());
                        } else {
                            pageEntryList = new ArrayList<>();
                        }
                        PageEntry pg = new PageEntry(file.getName(), i, entry.getValue());
                        pageEntryList.add(pg);
                        wordsMap.put(entry.getKey(), pageEntryList);
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        if (wordsMap.isEmpty() || !wordsMap.containsKey(word)) {
            return Collections.emptyList();
        } else {
            return wordsMap.get(word);
        }
    }

    public String listToJson(List<PageEntry> list) {
        Collections.sort(list);
        Type listType = new TypeToken<List<PageEntry>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(list, listType);
        return json;
    }
}