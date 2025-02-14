package de.moritzf.nlp.jtopia.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.moritzf.nlp.jtopia.entities.Configuration;

public class ConfigurationManager {

  // Languages
  public static final String ENGLISH = "en";
  public static final String SPANISH = "es";
  public static final String FRENCH = "fr";
  public static final String GERMAN = "de";


  // English
  public static final String ENGLISH_WSJ_BIDIRECTIONAL_DISTSIM_TAGGER = "wsj-0-18-bidirectional-distsim.tagger";
  public static final String ENGLISH_BIDIRECTIONAL_DISTSIM_TAGGER = "english-bidirectional-distsim.tagger";
  public static final String ENGLISH_CASELESS_LEFT3WORDS_DISTSIM_TAGGER = "english-caseless-left3words-distsim.tagger";
  public static final String ENGLISH_LEFTWORDS_DISTSIM_TAGGER = "english-left3words-distsim.tagger";
  public static final String ENGLISH_WSJ_BIDIRECTIONAL_NODISTSIM_TAGGER = "wsj-0-18-bidirectional-nodistsim.tagger";
  public static final String ENGLISH_WSJ_CASELESS_LEFT3WORDS_DISTSIM_TAGGER = "wsj-0-18-caseless-left3words-distsim.tagger";
  public static final String ENGLISH_WSJ_LEFT3WORDS_DISTSIM_TAGGER = "wsj-0-18-left3words-distsim.tagger";
  public static final String ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER = "wsj-0-18-left3words-nodistsim.tagger";
  // French
  public static final String FRENCH_TAGGER = "french.tagger";
  public static final String FERNCH_UD_TAGGER = "french-ud.tagger";
  // Spanish
  public static final String SPANISH_TAGGER = "spanish.tagger";
  public static final String SPANISH_DISTSIM_TAGGER = "spanish-distsim.tagger";
  public static final String SPANISH_UD_TAGGER = "spanish-ud.tagger";
  // German
  public static final String GERMAN_HGC_TAGGER = "german-hgc.tagger";
  public static final String GERMAN_FAST_TAGGER = "german-fast.tagger";
  public static final String GERMAN_FAST_CASELESS_TAGGER = "german-fast-caseless.tagger";
  public static final String GERMAN_UD = "german-ud.tagger";

  public static final Set<String> SUPPORTED_LANGS = ImmutableSet.of("en", "es", "de", "fr");
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);
  private static final Map<String, String> LANG_TO_TAGGER_MAP = ImmutableMap.<String, String>builder()
      .put(ENGLISH, ENGLISH_WSJ_BIDIRECTIONAL_DISTSIM_TAGGER)
      .put(SPANISH, SPANISH_UD_TAGGER)
      .put(FRENCH, FRENCH_TAGGER)
      .put(GERMAN, GERMAN_HGC_TAGGER)
      .build();
  private static final int DEFAULT_NO_LIMIT_STRENGTH = 2;
  private static final int DEFAULT_SINGLE_STRENGTH_MIN_OCCUR = 3;
  private static final String DEFAULT_LANGUAGE = ENGLISH;

  private final int defaultNoLimitStrength;
  private final int defaultSingleStrengthMinOccur;
  private final String defaultLanguage;

  public ConfigurationManager() {
    this(DEFAULT_NO_LIMIT_STRENGTH, DEFAULT_SINGLE_STRENGTH_MIN_OCCUR, DEFAULT_LANGUAGE);
  }

  public ConfigurationManager(int defaultNoLimitStrength,
                              int defaultSingleStrengthMinOccur,
                              String defaultLanguage) {
    this.defaultNoLimitStrength = defaultNoLimitStrength;
    this.defaultSingleStrengthMinOccur = defaultSingleStrengthMinOccur;
    this.defaultLanguage = defaultLanguage;
  }

  public Configuration getDefaultConfiguration() {
    return getConfigurationFor(defaultLanguage);
  }

  public Configuration getConfigurationFor(String language) {
    return getConfigurationFor(language, getDefaultTaggerFile(language));
  }

  public Configuration getConfigurationFor(String language, String taggerFile) {
    return getConfigurationFor(language,
                               defaultNoLimitStrength,
                               defaultSingleStrengthMinOccur,
                               taggerFile);
  }

  public Configuration getConfigurationFor(String language, int multiWordMinStrength, int singleWordMinOccurrence) {
    return getConfigurationFor(language, multiWordMinStrength, singleWordMinOccurrence, getDefaultTaggerFile(language));
  }

  public Configuration getConfigurationFor(String language, int multiWordMinStrength, int singleWordMinOccurrence, String taggerFile) {
    Preconditions.checkArgument(SUPPORTED_LANGS.contains(language), "Language {} not supported", language);
    Preconditions.checkArgument(!taggerFile.isEmpty(), "TaggerFile location should not be empty");
    Preconditions.checkArgument(multiWordMinStrength > 0, "Multi-word minimum strength needs to be > 0");
    Preconditions.checkArgument(singleWordMinOccurrence > 0, "Single word minimum occurrence count needs to be > 0");

    return Configuration.builder()
        .setMultiWordMinStrength(multiWordMinStrength)
        .setSingleWordMinOccurrence(singleWordMinOccurrence)
        .setStopWords(getStopWords(language))
        .setModelFileLocation(getModelLocation(language, taggerFile))
        .build();
  }

  private Set<String> getStopWords(String language) {
    String stopWordsFilePath = String.format("/stopwords/stopwords-%s.txt", language);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(stopWordsFilePath), StandardCharsets.UTF_8))) {
      return reader.lines()
          .map(String::trim)
          .collect(Collectors.toSet());
    } catch (IOException e) {
      String errorMessage = String.format("Unable to load the stopwords for language %s from %s", language, stopWordsFilePath);
      LOG.error(errorMessage, e);
      throw new RuntimeException(errorMessage);
    }
  }

  private String getDefaultTaggerFile(String language) {

    String langForConsideration = language;
    if (!LANG_TO_TAGGER_MAP.containsKey(language)) {
      langForConsideration = defaultLanguage;
    }
    return LANG_TO_TAGGER_MAP.get(langForConsideration);
  }

  private String getModelLocation(String language, String taggerFilename) {
    String modelFileName = String.format("/models/%s", taggerFilename);
    try {
      return FileSystems.getDefault().getPath(this.getClass().getResource(modelFileName).toString()).toString();
    } catch (Exception e) {
      String errorMessage = String.format("Unable to find model file for language %s at %s", language, modelFileName);
      LOG.error(errorMessage, e);
      throw new RuntimeException(errorMessage);
    }
  }
}
