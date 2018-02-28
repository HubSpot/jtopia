# jtopia

Keyword/Keyphrase extractor for English, Spanish, French and German texts.

This project is a fork of [moritzfl/jtopia](https://github.com/moritzfl/jtopia). 
The trained models for the aforementioned languages are from the [stanford nlp library](https://nlp.stanford.edu/software/tagger.shtml#History). 
The stopwords for the languages are from [stopwords/iso](https://github.com/stopwords-iso).
This lib has support for using only the `Stanford POS tagger`. 

This lib also has support for running in a multi-threaded environment. In the earlier version, the configuration is set in a static way, which would be cause issues when dealing with multiple languages.

## Maven artifacts
Add the following dependency to your pom

```xml
<dependency>
  <groupId>com.hubspot.jtopia</groupId>
  <artifactId>jtopia</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## More information on jtopia
Java clone for Python term extractor [topia](https://github.com/turian/topia.termextract). It will extract important keywords/keyphrases from the text.

jtopia is a light-weight term extractor, which is domain independent in nature.
It uses a rule based + POS tagged based approach to find out the keywords / keyphrases.

You can even tune the parameters in jtopia to get some filtered keywords / keyphrases.

jtopia just throws n number of keywords from the input text. It does not rank the keyword as first or second.

The numbers in the square is just an information about the extracted term. 

Hurricane Saturday Night=[1, 3]. Here 1 means the extracted keyword "Hurricane Saturday Night" has frequency 1 in the input text. 
The number 3 means the keyword is formed using 3 words "Hurricane", "Saturday" and "Night".


## Fine tuning jtopia

* You can change the extracted terms output using the TermsFilter class.There are two parameters (singleStrengthMinOccur and noLimitStrength) which filters the extracted jtopia output according to the parameters. 
* If you are dealing with short text, setting singleStrengthMinOccur and noLimitStrength to minimum will give you a largest possible number of keywords from the text.
* If you are dealing with  large text, setting singleStrengthMinOccur, noLimitStrength to a feasible maximum will filter out unwanted junk keywords and provide you with a smaller but more reasonable set of keywords.

singleStrengthMinOccur:
If the extracted term is a single word (eg. "Tower") and occurred at least as often as defined via singleStrength, it will be included in the final list of keywords.

noLimitStrength:
If the extracted term is constructed of at least as many separate words as defined in no limit strength, it will be included in the final list of keywords (eg. "Eiffel Tower" for noLimitStrength == 2).


## Usage

You can define a constant  `TermsExtractor` per language by generating a `Configuration` per language from `ConfigurationManager` and using the `Configuration` object to create a `TermsExtractor`.

```java
@com.google.inject.Inject
ConfigurationManager configurationManager;

Configuration engConfigurationOne = configurationManager.getConfigurationFrom("en");
//You may choose to get a fine tuned configuration
Configuration engConfigurationTwo = configurationManager.getConfigurationFrom("en", // language
                                                                        3, // noLimitStrength
                                                                        2, // singleStrengthMinOccur,
                                                                        ConfigurationManager.ENGLISH_WSJ_LEFT3WORDS_NODISTSIM_TAGGER // Different tagger);
TermsExtractor termsExtractor = new TermsExtractor(engConfigurationOne);

String text = "Keywords will be extracted from this text";

TermDocument topiaDoc = termsExtractor.extractTerms(text);

logger.info("Extracted terms : " + topiaDoc.getExtractedTerms());
logger.info("Final Filtered Terms : " + topiaDoc.getFinalFilteredTerms());
```
