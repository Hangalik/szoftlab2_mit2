package org.bme.mit.iir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class IndexSearcher {

    private final Map<String, Set<String>> index;

    public IndexSearcher(File indexFile) {
        index = new HashMap<>();

        try (Stream<String> stream = Files.lines(indexFile.toPath(), StandardCharsets.ISO_8859_1)) {
            stream.forEach((line) -> {
                String strs[] = line.split(IndexMaker.INDEX_FILE_SEPARATOR);
                String indexWord = strs[0];
                if (!index.containsKey(indexWord)) {
                    index.put(indexWord, new HashSet<>());
                }
                List<String> files = new ArrayList<>();
                for (int i = 1; i < strs.length; i++) {
                    files.add(strs[i]);
                }
                index.get(indexWord).addAll(files);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> searchForPhrases(String... searchPhrases) {
        Set<String> ret = new HashSet<>(index.get(searchPhrases[0]));

        for (String searchPhrase: searchPhrases) {
            ret.retainAll(index.get(searchPhrase));
        }

        return ret;
    }
}
