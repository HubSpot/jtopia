package de.moritzf.nlp.jtopia.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunctuationRemover {

  private static final Pattern PUNCT_PATTERN = Pattern.compile("^\\W*(.*?)\\W*$");

  public static String remove(String sentence) {

    Matcher matcher = PUNCT_PATTERN.matcher(sentence);
    return matcher.matches() ? matcher.group(1) : sentence;
  }
}
