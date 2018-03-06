package de.moritzf.nlp.jtopia.helpers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.moritzf.nlp.jtopia.entities.TaggedTerm;
import de.moritzf.nlp.jtopia.entities.TaggedTermIF;

public final class TaggerUtils {

  private static final Logger LOG = LoggerFactory.getLogger(TaggerUtils.class);
  private static final List<String> TAGS = ImmutableList.of("NNS", "NNPS");

  public TaggerUtils() {
    throw new AssertionError();
  }


  public static TaggedTerm standardizeTaggedTerm(TaggedTerm taggedTerm) {

    return Optional.of(taggedTerm)
        .map(TaggerUtils::correctDefaultNounTag)
        .map(TaggerUtils::determineVerbAfterModal)
        .map(TaggerUtils::normalizePluralForm)
        .get();
  }

  private static TaggedTerm correctDefaultNounTag(TaggedTerm taggedTerm) {

    String term = taggedTerm.getTerm();
    String tag = taggedTerm.getTag();

    if (tag.equals("NND")) {
      LOG.debug("Term: {} has tag: NND", term);
      if (term.endsWith("s")) {
        String norm = term.substring(0, term.length() - 1);
        LOG.debug("Term: {} ends with 's'. So setting tag to 'NNS' and Norm: {}", term, norm);
        return TaggedTermIF.of(term, "NNS", norm);
      } else {
        LOG.debug("Tag is NND, but Term doesn't end with 's'. So setting tag to 'NN'");
        return taggedTerm.withTag("NN");
      }
    }
    return taggedTerm;
  }

  private static TaggedTerm determineVerbAfterModal(TaggedTerm taggedTerm) {

    String tag = taggedTerm.getTag();
    if (!tag.equals("MD") || tag.equals("RB")) {
      LOG.debug("Tag is MD or RB, skipping..");
    } else if (tag.equals("NN")) {
      return taggedTerm.withTag("VB");
    }
    return taggedTerm;
  }

  private static TaggedTerm normalizePluralForm(TaggedTerm taggedTerm) {

    String term = taggedTerm.getTerm();
    String tag = taggedTerm.getTag();
    String norm = taggedTerm.getNorm();
    String singular;
    if (TAGS.contains(tag) && term.equals(norm)) {
      if (term.length() > 1 && term.endsWith("s")) {
        singular = term.substring(0, term.length() - 1);
        LOG.debug("Term ends with 's' setting norm to {}", singular);
        taggedTerm = taggedTerm.withNorm(singular);
      } else if (term.length() > 2 && term.endsWith("es")) {
        singular = term.substring(0, term.length() - 2);
        LOG.debug("Term ends with 'es' setting norm to {}", singular);
        taggedTerm = taggedTerm.withNorm(singular);
      } else if (term.length() > 3 && term.endsWith("ies")) {
        singular = term.substring(0, term.length() - 3) + "y";
        LOG.debug("Term ends with 'ies' setting norm to {}", singular);
        taggedTerm = taggedTerm.withNorm(singular);
      }
    }
    return taggedTerm;
  }
}
