package org.platform.spidereddit.utility;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class POSFilter {

    private final POSTaggerME posTagger;
    private final Set<String> allowedTags;

    public POSFilter(Set<String> allowedTags) {
        try (InputStream modelStream = getClass().getClassLoader().getResourceAsStream("models/en-pos-maxent.bin")) {
            POSModel model = new POSModel(modelStream);
            this.posTagger = new POSTaggerME(model);
            this.allowedTags = allowedTags;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load POS model", e);
        }
    }

    public List<String> filter(String text) {
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(text);
        String[] tags = posTagger.tag(tokens);

        List<String> filtered = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String tag = tags[i];
            if (allowedTags.stream().anyMatch(tag::startsWith)) {
                filtered.add(tokens[i].toLowerCase());
            }
        }
        return filtered;
    }
}
