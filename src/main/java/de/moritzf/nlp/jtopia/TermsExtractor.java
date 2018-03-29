package de.moritzf.nlp.jtopia;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import de.moritzf.nlp.jtopia.cleaner.TextCleaner;
import de.moritzf.nlp.jtopia.entities.ConfigurationIF;
import de.moritzf.nlp.jtopia.entities.TaggedTerm;
import de.moritzf.nlp.jtopia.entities.TaggedTermIF;
import de.moritzf.nlp.jtopia.entities.TermResponse;
import de.moritzf.nlp.jtopia.filter.TermsFilter;
import de.moritzf.nlp.jtopia.helpers.TaggerUtils;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TermsExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(TermsExtractor.class);
  private final MaxentTagger tagger;
  private final TermsFilter termsFilter;
  private final TextCleaner textCleaner;

  public TermsExtractor(ConfigurationIF configuration) {

    this.tagger = new MaxentTagger(configuration.getModelFileLocation());
    this.termsFilter = new TermsFilter(configuration.getSingleStrengthMinOccur(),
                                       configuration.getNoLimitStrength(),
                                       configuration.getStopWords());
    this.textCleaner = new TextCleaner();
  }

  public Set<TaggedTerm> generateTaggedTerms(Collection<String> terms) {

    ImmutableSet.Builder<TaggedTerm> builder = ImmutableSet.builder();
    for (String term : terms) {

      String tag = tagger.tagString(term);
      // Since Stanford POS has tagged terms like establish_VB , we only need the POS tag
      // Some POS tags in Stanford has special charaters at end like their/PRP$
      if (tag.split("_").length > 1) {
        builder.add(TaggerUtils.standardizeTaggedTerm(TaggedTermIF.of(term, tag.split("_")[1].replaceAll("\\$", ""), term)));
      }
    }

    return builder.build();
  }

  public Optional<TermResponse> extractTermsByWordBatchSize(String text, int batchSize) {
    if (text == null) {
      return Optional.empty();
    }
    LOG.debug("Extracting terms for text {} in batches of size {}", text, batchSize);
    List<String> tokens = getTokensFromText(text);
    Map<String, Integer> termOccurrenceMap = new HashMap<>();
    Iterables.partition(tokens, batchSize)
        .forEach(tokenBatch -> mergeMapsInPlace(termOccurrenceMap, generateTermsAndTheirOccurrenceMap(tokenBatch)));
    return Optional.of(TermResponse.builder()
                           .setTermsAndOccurrences(termOccurrenceMap)
                           .build());
  }

  public Optional<TermResponse> extractTerms(String text) {
    if (text == null) {
      return Optional.empty();
    }
    LOG.debug("Extracting terms for text {}", text);
    List<String> tokens = getTokensFromText(text);
    return Optional.of(TermResponse.builder()
                           .setTermsAndOccurrences(generateTermsAndTheirOccurrenceMap(tokens))
                           .build());
  }

  private List<String> getTokensFromText(String text) {
    String normalizedText = textCleaner.normalizeText(text);
    return textCleaner.tokenizeText(normalizedText);
  }

  private Map<String, Integer> generateTermsAndTheirOccurrenceMap(List<String> tokens) {
    Set<TaggedTerm> taggedTerms = generateTaggedTerms(tokens);
    Map<String, Integer> extractedTerms = extractTerms(taggedTerms);
    return termsFilter.filterTerms(extractedTerms);
  }

  private Map<String, Integer> extractTerms(Collection<TaggedTerm> taggedTerms) {

    Map<String, Integer> terms = new HashMap<>();
    List<String> multiTerm = new ArrayList<>();
    Consumer<String> addTerms = (term) -> {
      multiTerm.add(term);
      if (terms.containsKey(term)) {
        terms.put(term, terms.get(term) + 1);
      } else {
        terms.put(term, 1);
      }
    };
    int search = 0;
    int noun = 1;
    int state = search;
    for (TaggedTerm taggedTerm : taggedTerms) {

      String term = taggedTerm.getTerm();
      String tag = taggedTerm.getTag();
      if (state == search && tag.startsWith("N")) {
        state = noun;
        addTerms.accept(term);
      } else if ((state == search) && (tag.equals("JJ")) && (Character.isUpperCase(term.charAt(0)))) {
        state = noun;
        addTerms.accept(term);
      } else if (state == noun && tag.startsWith("N")) {
        addTerms.accept(term);
      } else if (state == noun && !tag.startsWith("N")) {
        state = search;
        if (multiTerm.size() > 1) {
          String multiWord = null;
          for (String multi : multiTerm) {
            if (multiWord == null) {
              multiWord = multi;
            } else {
              multiWord = String.format("%s %s", multiWord, multi);
            }
          }
          if (terms.containsKey(multiWord)) {
            Integer count = terms.get(multiWord);
            count += 1;
            terms.put(multiWord, count);
          } else {
            terms.put(multiWord, 1);
          }
        }
        multiTerm.clear();
      }
    }

    return terms;
  }

  private void mergeMapsInPlace(Map<String, Integer> map1, Map<String, Integer> map2) {
    map2.forEach((k, v) -> map1.merge(k, v, (v1, v2) -> v1 + v2));
  }
}

