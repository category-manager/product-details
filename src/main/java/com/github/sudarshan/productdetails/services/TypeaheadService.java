package com.github.sudarshan.productdetails.services;

import com.github.sudarshan.productdetails.models.TypeaheadDatum;
import com.github.sudarshan.productdetails.models.TypeaheadNode;
import com.github.sudarshan.productdetails.repositories.TypeaheadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.sudarshan.productdetails.configs.AppConstants.MAX_TYPEAHEAD_SUGGESTIONS;
import static com.github.sudarshan.productdetails.configs.AppConstants.MIN_TYPEAHEAD_LENGTH;

@Service
public class TypeaheadService {
    @Autowired
    private TypeaheadRepository typeaheadRepository;
    private static final int MAX_WORD_INDEX_COUNT = 2;
    private static final Map<Integer, List<String>> ordinalToNamesMap = new Hashtable<>();
    private static final Map<String, List<Integer>> wordToOrdinalsMap = new Hashtable<>();
    private static final TypeaheadNode typeaheadNode = new TypeaheadNode();
    private List<TypeaheadDatum> typeaheadData;
    @PostConstruct
    public void setup() {
        typeaheadData = typeaheadRepository.getTypeaheadData();
        constructTypeaheadDs();
    }

    private void constructTypeaheadDs() {
        // build ordinal map
        // build words map based on MAX_WORD_INDEX_COUNT.
        buildOrdinalMap();
        // construct the typeaheadNode structure.
        buildWordSuffixTree();
    }

    private void buildWordSuffixTree() {
        for (Map.Entry<String, List<Integer>> entry: wordToOrdinalsMap.entrySet()) {
            String word = entry.getKey();
            List<Integer> ordinals = entry.getValue();
            TypeaheadNode temp = typeaheadNode;
            char[] chars = word.toCharArray();
            for(char c: chars) {
                List<Character> children = temp.getChildren().stream().map(TypeaheadNode::getKey).collect(Collectors.toList());
                int index = children.indexOf(c);
                if(index > -1) {
                    temp = temp.getChildren().get(index);
                } else {
                    TypeaheadNode node = new TypeaheadNode();
                    node.setKey(c);
                    temp.getChildren().add(node);
                    children.add(c);
                    temp = temp.getChildren().get(children.size() -1);// node;
                }
                temp.getOrdinals().addAll(ordinals);
            }
        }
    }

    private void buildOrdinalMap() {
        for(int ordinalIdx = 0; ordinalIdx < typeaheadData.size(); ordinalIdx++) {
            TypeaheadDatum datum = typeaheadData.get(ordinalIdx);
            if(Objects.isNull(datum))
                continue;
            String name = datum.getName();
            if(Objects.isNull(name) || name.isBlank())
                continue;
            name = name.toLowerCase();
            List<String> list = new ArrayList<>(2);
            list.add(name);
            ordinalToNamesMap.put(ordinalIdx, list);

            // TODO: remove stopwords from the name before splitting.

            String[] splitWords = name.split("\\s");
            for(int j = 0; j < Math.min(splitWords.length, MAX_WORD_INDEX_COUNT); j++) {
                List<Integer> ordinals = new ArrayList<>(2);
                ordinals.add(ordinalIdx);
                wordToOrdinalsMap.merge(splitWords[j], ordinals, (oldVal, newVal) -> {
                    Set<Integer> distinctOrdinals = new HashSet<>(oldVal);
                    distinctOrdinals.addAll(newVal);
                    oldVal.clear();
                    oldVal.addAll(distinctOrdinals);
                    return oldVal;
                });
            }
        }
    }

    public List<String> getSuggestions(String query) {
        List<String> words = Arrays.stream(query.trim().split("\\s")).collect(Collectors.toList());
        List<String> result = new ArrayList<>();
        if(query.trim().length() < MIN_TYPEAHEAD_LENGTH) {
            return new ArrayList<>();
        }
        for(String word: words) {
            TypeaheadNode temp = typeaheadNode;
            char[] chars = word.toCharArray();
            for(char c: chars) {
                List<Character> children = temp.getChildren().stream().map(TypeaheadNode::getKey).collect(Collectors.toList());
                int index = children.indexOf(c);
                if(index > -1) {
                    temp = temp.getChildren().get(index);
                } else {
                    break;
                }
            }
            Set<Integer> ordinals = new HashSet<>(temp.getOrdinals());
            List<String> suggestions = ordinals.stream()
                    .map(ordinalToNamesMap::get)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            result.addAll(suggestions);
        }
        return new HashSet<String>(result).stream().limit(MAX_TYPEAHEAD_SUGGESTIONS).collect(Collectors.toList());
    }

    public Map<Integer,List<String>> getOrdinalToNamesMap() {
        return ordinalToNamesMap;
    }

    public Map<String, List<Integer>> getWordToOrdinalMap() {
        return wordToOrdinalsMap;
    }
}
