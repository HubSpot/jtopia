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
import de.moritzf.nlp.jtopia.entities.TermDocument;

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
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("Reminder Use", "Mirror Inc", "Soap Scum", "ClearShield Simple Spray-on", "Research Hardness",
                                                                 "Baking Soda Liquid", "Others Mixture", "Care Instructions Company", "Oil Lemon Juice",
                                                                 "Cause Damage", "Efforts Need", "Specific Recommendations", "Websites Offer", "Water Spots",
                                                                 "Life Prevention Key", "Abrasive Agents", "Brushed-on Product", "Non-scratch Sponge", "Customers Central Texas",
                                                                 "Standard Check Manufacturer", "Mineral Deposits", "Basic Level", "Arrow Glass", "Cleaning Time", "Stone/tile"));

    Optional<TermDocument> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getFinalFilteredTerms().keySet()).containsExactlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsEnglishPageWithOtherTagger() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH, ConfigurationManager.ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("Cause Damage", "Reminder Use", "Efforts Need", "Websites Offer", "Water Spots", "Life Prevention Key",
                                                                 "Mirror Inc", "Brushed-on Product", "Soap Scum", "Customers Central Texas", "Research Hardness", "Standard Check Manufacturer",
                                                                 "Mineral Deposits", "Arrow Glass", "Others Mixture", "Care Instructions Company", "Oil Lemon Juice",
                                                                 "Soda Liquid", "Cleaning Time"));

    Optional<TermDocument> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getFinalFilteredTerms().keySet()).containsExactlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsEnglishPageWithOtherTaggerWithFineTunecConfiguration() throws IOException {
    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.ENGLISH,
                                                                           3,
                                                                           2,
                                                                           ConfigurationManager.ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String englishText = Resources.toString(getClass().getResource("/english-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = new HashSet<>(Arrays.asList("Standard Check Manufacturer", "Life Prevention Key", "Care Instructions Company",
                                                                 "Oil Lemon Juice", "Customers Central Texas"));

    Optional<TermDocument> termDocument = termsExtractor.extractTerms(englishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getFinalFilteredTerms().keySet()).containsExactlyElementsOf(expectedKeyphrases);
  }

  @Test
  public void itExtractsSpanishPage() throws IOException {

    Configuration configuration = configurationManager.getConfigurationFor(ConfigurationManager.SPANISH);
    TermsExtractor termsExtractor = new TermsExtractor(configuration);
    String spanishText = Resources.toString(getClass().getResource("/spanish-text.txt"), StandardCharsets.UTF_8);
    Set<String> expectedKeyphrases = ImmutableSet.of("Colocarte Una Camisa", "Un Abrigo", "Tu Deporte", "Desempe Ño", "Accesorio Cabeza", "Mallas Tipo Nylon Polipropileno");

    Optional<TermDocument> termDocument = termsExtractor.extractTerms(spanishText);
    assertThat(termDocument).isPresent();
    assertThat(termDocument.get().getFinalFilteredTerms().keySet()).containsExactlyElementsOf(expectedKeyphrases);
  }
}
