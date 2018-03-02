package de.moritzf.nlp.jtopia.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import de.moritzf.nlp.jtopia.helpers.PunctuationRemover;

public class TermsFilter {

  private static final Logger LOG = LoggerFactory.getLogger(TermsFilter.class);
  private static final Set<String> DATE_OFFSETS = ImmutableSet.of("PM", "ET", "UST", "AM", "IST", "PDT", "AD");
  private static final Pattern DIGIT_OR_PUNCT_PATTERN = Pattern.compile("^[\\d\\p{Punct}]+$");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d+$");

  private int singleStrengthMinOccur;
  private int noLimitStrength;
  private Set<String> stopWords;

  public TermsFilter(int singleStrength, int noLimit, Set<String> stopWords) {
    singleStrengthMinOccur = singleStrength;
    noLimitStrength = noLimit;
    this.stopWords = stopWords;
  }

  public static String removeBlankCharacters(String term, int position) {
    return term.substring(0, position) + '#' + term.substring(position + 1);
  }

  public static boolean isBlank(char ch) {
    return Character.isWhitespace(ch);
  }

  public static String removeUnwantedCharacters(String term) {
    term = term.replaceAll("[#,”“’‘]", "");
    return term.replaceAll("\\s+", " ").trim();
  }

  public static boolean isPunctuation(char c) {
    return (c == '.' || c == '@' || c == '_' || c == '&' || c == '/'
        || c == '-' || c == '\\');
  }

  public Map<String, ArrayList<Integer>> filterTerms(Map<String, Integer> extractedTerms) {
    Map<String, ArrayList<Integer>> filteredTerms = new HashMap<>();

    extractedTerms.forEach((key, value) -> {
      String[] wordCount = key.split(" ");
      int strength = wordCount.length;
      if (!key.isEmpty()) {
                /*If the term is a single word and it occurred more often than defined by singleStrengthMinOccur
                 * OR
                 *if it is constituted multiple words and the number of words is higher or equal to noLimitStrength
                 *THEN include it in the final list of terms */
        if ((strength == 1 && value >= singleStrengthMinOccur) || (strength >= noLimitStrength)) {
          ArrayList<Integer> values = new ArrayList<>();
          values.add(value);
          values.add(strength);
          filteredTerms.put(key, values);
        }
      }
    });

    return cleanUp(filteredTerms);
  }

  private Map<String, ArrayList<Integer>> cleanUp(Map<String, ArrayList<Integer>> filteredTerms) {
    filteredTerms = removeStopWordsAndPunctuations(filteredTerms);
    filteredTerms = removeDateTerms(filteredTerms);
    filteredTerms = removeDuplicateSingleWords(filteredTerms);

    return filteredTerms;
  }

  private Map<String, ArrayList<Integer>> removeDuplicateSingleWords(
      Map<String, ArrayList<Integer>> filteredTerms) {
    Set keySet = filteredTerms.keySet();
    Map<String, ArrayList<Integer>> finalTerms = new HashMap<>();

    try {
      filteredTerms.forEach((key, valueList) -> {
        boolean isWordExist = false;
        String[] termArray = key.split(" ");
        int wordsCount = termArray.length;
        if (wordsCount == 1) {
          isWordExist = isWordPresent(key.trim(), keySet);
        }
        if (!isWordExist) {
          finalTerms.put(key, valueList);
        }
      });
    } catch (Exception ex) {
      LOG.error(ex.toString(), ex);
    }
    return finalTerms;
  }

  private boolean isWordPresent(String singleTerm, Set keySet) {
    singleTerm = singleTerm.trim().toLowerCase();
    for (Object key : keySet) {
      String termInMap = (String) key;
      String[] termArray = null;
      if (termInMap.contains(" ")) {
        termArray = termInMap.split(" ");
      }
      if (termArray != null && termArray.length > 1) {
        for (String term : termArray) {
          if (term.trim().toLowerCase().equals(singleTerm)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Map<String, ArrayList<Integer>> removeStopWordsAndPunctuations(Map<String, ArrayList<Integer>> filteredTerms) {

    Map<String, ArrayList<Integer>> finalFilterdTerms = new HashMap<>();

    try {
      filteredTerms.forEach((key, values) -> {
        // replacing extra whitespaces
        key = key.replaceAll("\\s+", " ");
        String[] termArray = null;
        if (key.contains(" ")) {
          termArray = key.split(" ");
        }
        boolean isStopWord = false;
        boolean hasDigitOnlyWord = false;
        for (String word : stopWords) {
          if (termArray != null) {
            for (String splitTerm : termArray) {
              isStopWord = false;
              hasDigitOnlyWord = false;
              if (splitTerm.trim().toLowerCase().equals(word.trim())) {
                isStopWord = true;
                break;
              } else if (hasDigitOrPunctuation(splitTerm.trim())) {
                hasDigitOnlyWord = true;
                break;
              }
            }

          } else { // if term has only one word
            if (key.trim().toLowerCase().equals(word.trim())) {
              isStopWord = true;
            } else if (hasDigitOrPunctuation(key.trim())) {
              hasDigitOnlyWord = true;
            }
          }
        }

        int wordLength = values.get(1);
        if (!isStopWord && !hasDigitOnlyWord) {
          key = removeApostrophes(key, wordLength, values);
          // remove punctuation from start and beginning
          key = PunctuationRemover.remove(key);

          // keep some punctuation .@&-_' '/\  in between words
          String regex = "[\\p{Punct}&&[^_.&\\\\/-]]";
          key = key.replaceAll(regex, "").trim();
          // remove punctuation which doesn't need to preserve
          key = filterPunctuations(key.trim().toCharArray(), key);

          // skip words having length less than 3
          if (key.length() > 3 && !isDigitOnly(key)) {
            finalFilterdTerms.put(key, values);
          }
        } else {
          LOG.debug("Discarding term " + key + " isStopword "
                        + isStopWord + " hasDigitonlyWord "
                        + hasDigitOnlyWord);
        }
      });
    } catch (Exception e) {
      LOG.error(e.toString(), e);
    }
    return finalFilterdTerms;
  }

  private Map<String, ArrayList<Integer>> removeDateTerms(
      Map<String, ArrayList<Integer>> filterdTerms) {

    Map<String, ArrayList<Integer>> finalFilterdTerms = new HashMap<>();
    filterdTerms.forEach((key, valueList) -> {
      String[] termArray = null;
      if (key.contains(" ")) {
        termArray = key.split(" ");
      }
      boolean isDate = false;
      for (String offset : DATE_OFFSETS) {
        if (termArray != null) {
          for (String dateTerm : termArray) {
            isDate = false;
            if (dateTerm.trim().equals(offset)) {
              LOG.debug("Date {} found in {}", offset, key);
              isDate = true;
              break;
            }
          }
        } else if (key.trim().equals(offset)) {
          isDate = true;
        }
      }
      if (!isDate) {
        finalFilterdTerms.put(capitalizeTerm(key), valueList);
      }
    });
    return finalFilterdTerms;
  }

  private String capitalizeTerm(String text) {

    String[] words = text.split(" ");
    StringJoiner joiner = new StringJoiner(" ");

    for (String word : words) {
      //Add capitalized words to joiner
      joiner.add(word.substring(0, 1).toUpperCase() + word.substring(1));
    }


    return joiner.toString();
  }

  private boolean isDigitOnly(String term) {

    return DIGIT_PATTERN.matcher(term).find();
  }

  private boolean hasDigitOrPunctuation(String term) {

    return DIGIT_OR_PUNCT_PATTERN.matcher(term).find();
  }

  private String filterPunctuations(char[] ch, String term) {
    try {
      for (int index = 1; index < ch.length - 1; index++) {
        char character = ch[index];
        if (isPunctuation(character)) {
          if (character != '\\') {
            if (!(Character.isLetterOrDigit(term.charAt(index - 1)) && Character
                .isLetterOrDigit(term.charAt(index + 1)))) {
              LOG.debug("Next/Previous charactor is not letterorDigit {} {}{} {} {}", term.charAt(index - 1), term.charAt(index + 1), term, index, character);
              if (isBlank(term.charAt(index - 1))) {
                term = removeBlankCharacters(term, index - 1);
              } else if (isBlank(term.charAt(index + 1))) {
                term = removeBlankCharacters(term, index + 1);
              } else {
                term = term.replace(character, ' ');
              }
            }
          } else {
            if (!(Character.isLetterOrDigit(term.charAt(index - 1)))) {
              LOG.debug("Previous charactor is not letterorDigit {}", term.charAt(index - 1));
              if (isBlank(term.charAt(index - 1))) {
                term = removeBlankCharacters(term, index - 1);
              }
              term = term.replace(character, ' ').trim();
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error(e.toString(), e);
    }
    return removeUnwantedCharacters(term);
  }

  private String removeApostrophes(String term, int wordLength, List<Integer> values) {
    if (term.contains("’s")) {
      term = term.replaceAll("’s", "");
      wordLength = wordLength - 1;
      values.remove(1);
      values.add(wordLength);
    }
    return term;
  }
}

