package org.bme.mit.iir;

import java.io.File;
import java.util.Scanner;
import java.util.Set;

public class SearchMain {

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            String line;
            IndexSearcher indexSearcher = new IndexSearcher(new File("index.txt"));
            while (true) {
                line = scanner.nextLine();
                if (line.equals("exit")) {
                    break;
                }
                String searchPhrases[] = line.split(" ");
                Set<String> searchResults = indexSearcher.searchForPhrases(searchPhrases);
                System.out.println(searchResults.size() + " results");
                for (String searchResult: searchResults) {
                    System.out.println(searchResult);
                }
            }
        }
    }
}
