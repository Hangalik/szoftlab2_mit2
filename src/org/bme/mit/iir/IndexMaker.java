package org.bme.mit.iir;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class IndexMaker {
    public static final String INDEX_FILE_SEPARATOR = ";";
    private static final String TO_BE_STRIPPED = " .,;:?!\"'[]{}<>()\\/|&+-=%#*\t\r\n";

    private final File indexFile;
    private final Map<String, Set<String>> index;

    public IndexMaker(String indexFileName) {
        indexFile = new File(indexFileName);
        index = new HashMap<>();
    }

    public void makeFile() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile), StandardCharsets.ISO_8859_1))) {

            Set<String> appearedWords = index.keySet();
            for (String word: appearedWords) {
                Set<String> containingFiles = index.get(word);
                bw.write(word);
                for (String file: containingFiles) {
                    bw.write(INDEX_FILE_SEPARATOR + file);
                }
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processDirectory(String directoryName) {
        processDirectoryPath(Paths.get(directoryName));
    }

    private void processDirectoryPath(Path directoryPath) {
        if (Files.isRegularFile(directoryPath)) {
            fileToIndex(directoryPath);
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            for (Path file: stream) {
                processDirectoryPath(file);
            }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
        }
    }

    private void fileToIndex(Path filePath) {
        try {
            List<String> linesOfFile = Util.readLinesIntoList(filePath.toAbsolutePath().toString());
            for (String line: linesOfFile) {
                String[] rawWords = line.split(" ");
                for (String rawWord: rawWords) {
                    String word = StringUtils.strip(rawWord, TO_BE_STRIPPED).toLowerCase();
                    if (StringUtils.isNotBlank(word)) {
                        if (!index.containsKey(word)) {
                            index.put(word, new HashSet<>());
                        }
                        index.get(word).add(filePath.toAbsolutePath().toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
