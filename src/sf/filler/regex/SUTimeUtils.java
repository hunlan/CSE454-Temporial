package sf.filler.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.util.CoreMap;

public class SUTimeUtils {
	private AnnotationPipeline pipeline;
	
	// Static class
	public SUTimeUtils() {
		// Dummy object for TimeAnnotator constructor
		Properties noProperties = new Properties();

		// time Annotator for pipeline
		Annotator timeAnnotator = new TimeAnnotator("sutime", noProperties);

		// the pipeline
		this.pipeline = new AnnotationPipeline();

		// Add annotators
		this.pipeline.addAnnotator(new PTBTokenizerAnnotator(false));// ,
																// "invertible,ptb3Escaping=false"));
		this.pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		// pipeline.addAnnotator(new POSTaggerAnnotator(false));

		// add timeAnnotator to pipeline
		this.pipeline.addAnnotator(timeAnnotator);
	}

	// Converting a time in a sentence into standard time form
	public String stdTimeInSentence(String sentence, String initialDate) {
		List<CoreMap> times = getTimeMapFromSentence(sentence, initialDate);
		return stdTimeInSentence(sentence, times, initialDate);
	}

	// Converting a time in a sentence into standard time form
	// Overload with List<CoreMap> (so don't need to perform this heavy weight
	// instruction again
	public String stdTimeInSentence(String sentence,
			List<CoreMap> times, String initialDate) {
		String result = "";

		List<Timex> timexs = getTimexFromCoreMap(times);

		int previousEnd = 0;

		for (int i = 0; i < times.size(); i++) {
			CoreMap time = times.get(i);
			Timex timex = timexs.get(i);
//System.out.println(timex);
			int begin = time
					.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
			int end = time
					.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

			if (begin >= previousEnd) {
				result += sentence.substring(previousEnd, begin);
				if (timex == null) {
					result += sentence.substring(begin, end);
				} else {
					result += timex.value();
				}

				previousEnd = end;
			} else {
				// TODO: what happen in this case?
				System.out.println("What happened?");
			}
		}

		if (previousEnd < sentence.length()) {
			result += sentence.substring(previousEnd);
		}

		return result;
	}

	/**
	 * Give me a sentence, and I will return a list of CoreMap(general object
	 * from stanford lib)
	 * 
	 * @precondition initialDate is in the format YYYY-MM-DD
	 * @param sentence
	 * @param initialDate
	 * @return
	 */
	public List<CoreMap> getTimeMapFromSentence(String sentence,
			String initialDate) {

		// Check arguement
		checkStringNullEmpty(sentence);
		checkStringNullEmpty(initialDate);

		// Create annotation of our text
		Annotation annotation = new Annotation(sentence);

		// Set doc date
		annotation.set(CoreAnnotations.DocDateAnnotation.class, initialDate);

		// Perform
		pipeline.annotate(annotation);

		// Get result
		return annotation.get(TimeAnnotations.TimexAnnotations.class);
	}

	// Will not check for null in timexAnn.get
	public List<Timex> getTimexFromCoreMap(List<CoreMap> coreMap) {
		checkObjectNull(coreMap);

		List<Timex> results = new ArrayList<Timex>(coreMap.size());

		for (CoreMap timexAnn : coreMap) {
			Timex timex = timexAnn.get(TimeAnnotations.TimexAnnotation.class);
			results.add(timex);
		}
		return results;
	}

	// Method to get standarize Format (String starts off with a \t already)
	public String standardizeFormat(String tokenizedSentence, String initTime) {
		checkStringNullEmpty(tokenizedSentence);
		if (initTime == null) {
			initTime = "2013-01-01"; //TODO - what should i do if no date?
		}
		
		StringBuilder result = new StringBuilder();
		
		List<CoreMap> coreMaps = getTimeMapFromSentence(tokenizedSentence, initTime);
		List<Timex> timexs = getTimexFromCoreMap(coreMaps);
		
		int previousEnd = 0;

		for (int i = 0; i < coreMaps.size(); i++) {
			CoreMap time = coreMaps.get(i);
			Timex timex = timexs.get(i);

			int begin = time
					.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
			int end = time
					.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
			
			int beginIdx = getIndexOfToken(tokenizedSentence, begin);
			int endIdx = getIndexOfToken(tokenizedSentence, end);

			if (begin >= previousEnd) {
				result.append("\t" + beginIdx + " " + endIdx);
				if (timex != null) {
					result.append(" ").append(timex.value());
					result.append(" ").append(timex.timexType());
					//result.append(" ").append(timex.getRange().toString());					
				} else {
					// TODO: what should i do here?
					System.out.println("timex object is null");
				}

				previousEnd = end;
			} else {
				// TODO: what happen in this case?
				System.out.println("I've got a messed up sentence with:\n\t::" + tokenizedSentence);
			}
		}
		
		return result.toString();
	}
	
	// Get index of token base on spaces
	private int getIndexOfToken(String tokenString, int charIndex) {
		checkStringNullEmpty(tokenString);
		
		String tokenSubstr = tokenString.substring(0, charIndex);
		String tokenSubstrNoSpace = tokenSubstr.replace(" ", "");
		
		return tokenSubstr.length() - tokenSubstrNoSpace.length();
	}
	
	private void checkObjectNull(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("object is null");
		}
	}

	// check string arguments
	private void checkStringNullEmpty(String str) {
		checkObjectNull(str);
		if (str.isEmpty() || str.trim().length() <= 0) {
			throw new IllegalArgumentException("arguement is an empty string");
		}
	}

}
