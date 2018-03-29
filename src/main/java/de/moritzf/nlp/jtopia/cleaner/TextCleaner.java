package de.moritzf.nlp.jtopia.cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(TextCleaner.class);
  private static final String UMLAUT = "äåüöÄÅÖÜßæÆøØáéíóúÁÉÍÑÓÚéÉèÈàÀùÙâêîôûÂÊÎÔÛçÇãÃáÁàÀâÂéÉêÊíÍõÕóÓôÔúÚüÜ";
  private static final String MATCH_PATTERN = "([^a-zA-Z" + UMLAUT + "]*)([a-z" + UMLAUT + "A-Z-\\.]*[a-zA-Z"
      + UMLAUT + "])([^a-zA-Z" + UMLAUT + "]*[a-zA-Z" + UMLAUT + "]*)";
  private static final Pattern TOKENIZE_PATTERN = Pattern.compile(MATCH_PATTERN, Pattern.DOTALL | Pattern.MULTILINE);

  public String normalizeText(String text) {
    LOG.debug("Input to normalize text: {}", text);
    text = replaceAll(text, "\\n", " . ").trim();
    LOG.debug("Input text normalized: {}", text);
    return text;
  }

  public List<String> tokenizeText(String text) {

    List<String> tokenizedWords = new ArrayList<>();
    String[] words = text.split("\\s");

    for (String word : words) {
      // If the term is empty, skip it, since we probably just have multiple whitespace characters.
      if (word.isEmpty()) {
        continue;
      }
      // Now, a word can be preceded or succeeded by symbols, so let's split those out
      Matcher matcher = TOKENIZE_PATTERN.matcher(word);
      if (!matcher.find()) {
        tokenizedWords.add(word);
        continue;
      }
      for (int i = 1; i <= matcher.groupCount(); i++) {
        if (matcher.group(i) != null && !matcher.group(i).isEmpty()) {
          LOG.debug("Matcher group: {} text: {}", i, matcher.group(i));
          tokenizedWords.add(matcher.group(i));
        }
      }
    }

    return tokenizedWords;
  }

  private String replaceAll(String text, String regex, String replacement) {
    text = text.replaceAll(regex, replacement);
    return text;
  }
}

