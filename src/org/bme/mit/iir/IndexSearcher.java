package org.bme.mit.iir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;

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

	private Set<String> searchForSinglePhrase(String searchPhrase) {

		ReasoningExample reasoner = new ReasoningExample(ReasoningExample.PCSHOP_ONTOLOGY_FNAME);
		Set<String> extendedSearchPhrases = new HashSet<>();

		extendedSearchPhrases.add(searchPhrase);
		Set<OWLClass> supers = reasoner.getSuperClasses(searchPhrase, false);
		Set<OWLClass> subs = reasoner.getSubClasses(searchPhrase, false);
		for (OWLClass superr : supers) {
			extendedSearchPhrases.add(superr.getIRI().getFragment());
		}
		for (OWLClass sub : subs) {
			extendedSearchPhrases.add(sub.getIRI().getFragment());
		}

		Set<String> ret = new HashSet<>();
		for (String searchPh : extendedSearchPhrases) {
			ret.addAll(searchForPhrases(new String[] {searchPh}));
		}

		return ret;
	}

	public Set<String> extendedSearchForPhrases(String[] searchPhrases) {

		Set<String> ret = new HashSet<>(searchForSinglePhrase(searchPhrases[0]));

		for (String searchPhrase : searchPhrases) {
			Set<String> result = searchForSinglePhrase(searchPhrase);
			ret.retainAll(result);
		}

		return ret;
	}

	public Set<String> searchForPhrases(String[] searchPhrases) {

		if (searchPhrases.length == 0 || index.get(searchPhrases[0]) == null) {
			return Collections.emptySet();
		}
		Set<String> ret = new HashSet<>(index.get(searchPhrases[0]));

		for (String searchPhrase : searchPhrases) {
			ret.retainAll(index.get(searchPhrase));
		}

		return ret;
	}
}
