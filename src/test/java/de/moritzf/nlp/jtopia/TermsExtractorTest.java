package de.moritzf.nlp.jtopia;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import de.moritzf.nlp.jtopia.config.ConfigurationManager;
import de.moritzf.nlp.jtopia.entities.Configuration;
import de.moritzf.nlp.jtopia.entities.TermResponse;

public class TermsExtractorTest {

  private ConfigurationManager configurationManager;

  @Before
  public void setup() {
    configurationManager = new ConfigurationManager();
  }

  @Test
  public void itExtractsEnglishPage() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("oil lemon juice", "abrasive agents", "websites offer", "non-scratch sponge", "ClearShield simple spray-on",
                                                                 "specific recommendations", "standard check manufacturers website", "care instructions company",
                                                                 "soap scum", "Mirror Inc", "mineral deposits", "research hardness", "cleaning time",
                                                                 "brushed-on product", "basic level", "water spots", "baking soda liquid", "life prevention key",
                                                                 "cause damage", "reminder use", "Arrow Glass", "efforts need", "customers Central Texas", "others mixture"));

    Optional<TermResponse> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getTermsAndOccurrences().keySet()).containsOnlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsEnglishPageInWordBatches() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("non-scratch sponge", "Mirror Inc", "cleaning time", "water spots", "liquid soap",
                                                                 "others mixture", "websites offer", "abrasive agents", "cleaning guidelines", "check door manufacturers website",
                                                                 "care instructions", "dish soap", "soap scum", "tips Let", "mineral deposits", "vinegar water",
                                                                 "Central Texas", "basic level", "brushed-on product", "ClearShield simple spray-on solution",
                                                                 "ShowerGuard glass", "life prevention key", "cause damage", "ShowerGuard Diamon-Fusion",
                                                                 "specific cleaning product recommendations", "Arrow Glass", "oil lemon juice"));

    Optional<TermResponse> termDocument = termsExtractor.extractTermsByWordBatchSize(englishText, 100);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getTermsAndOccurrences().keySet()).containsOnlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsEnglishPageWithOtherTagger() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH, ConfigurationManager.ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("oil lemon juice", "websites offer", "standard check manufacturers website", "care instructions company",
                                                                 "soap scum", "Mirror Inc", "mineral deposits", "research hardness", "cleaning time", "brushed-on product",
                                                                 "water spots", "soda liquid", "life prevention key", "cause damage", "reminder use", "Arrow Glass",
                                                                 "efforts need", "customers Central Texas", "others mixture"));

    Optional<TermResponse> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getTermsAndOccurrences().keySet()).containsOnlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsEnglishPageWithOtherTaggerWithFineTuneConfiguration() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH,
                                                                           3,
                                                                           2,
                                                                           ConfigurationManager.ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("oil lemon juice", "life prevention key", "standard check manufacturers website", "care instructions company", "customers Central Texas"));

    Optional<TermResponse> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getTermsAndOccurrences().keySet()).containsOnlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsSpanishPage() throws IOException {

    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.SPANISH);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String spanishText = Resources.toString(getClass().getResource("/spanish-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = ImmutableSet.of("accesorio cabeza", "colocarte una camisa", "mallas tipo nylon polipropileno", "un abrigo", "gorra pañoleta", "tu deporte");

    Optional<TermResponse> termDocument = termsExtractor.extractTerms(spanishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getTermsAndOccurrences().keySet()).containsOnlyElementsOf(expectedKeyphrases);
  }
}
