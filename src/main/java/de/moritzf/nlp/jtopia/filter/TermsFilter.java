package de.moritzf.nlp.jtopia.filter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

public class TermsFilter {

  private static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d+$");
  private static final Splitter WORD_SPLITTER = Splitter.on(" ").omitEmptyStrings();

  private int singleWordMinOccurrence;
  private int multiWordStrength;
  private Set<String> stopWords;

  public TermsFilter(int singleWordMinOccurrence, int noLimit, Set<String> stopWords) {
    this.singleWordMinOccurrence = singleWordMinOccurrence;
    multiWordStrength = noLimit;
    this.stopWords = stopWords;
  }

  public Map<String, Integer> filterTerms(Map<String, Integer> extractedTerms) {

    return extractedTerms.entrySet().stream()
        .filter(this::isValidTerm)
        .collect(Collectors.toMap(entry -> removeUnwantedCharacters(entry.getKey()),
                                  Entry::getValue));
  }

  /**
   * A term is Valid iff
   * - it is a single word and it occurred more often than defined by singleWordMinOccurrence
   * - it constitutes of multiple words and the number of words is higher or equal to multiWordStrength
   */
  private boolean isValidTerm(Entry<String, Integer> termOccurrenceEntry) {
    String term = termOccurrenceEntry.getKey();
    int occurrence = termOccurrenceEntry.getValue();
    int strength = WORD_SPLITTER.splitToList(term).size();
    if (strength == 1 && occurrence >= singleWordMinOccurrence) {
      return isSingleWordTermValid(term);
    } else if (strength >= multiWordStrength) {
      return true;
    }
    return false;
  }

  private boolean isSingleWordTermValid(String term) {
    return !isBlank(term) && !stopWords.contains(term.toLowerCase()) && !isDigitOnly(term);
  }

  private boolean isDigitOnly(String term) {
    return DIGIT_PATTERN.matcher(term).find();
  }

  private boolean isBlank(String term) {
    return term.trim().isEmpty();
  }

  private String removeUnwantedCharacters(String term) {
    return term.replaceAll("[#,”“’‘]", "")
        .replaceAll("\\s+", " ").trim();
  }
}
