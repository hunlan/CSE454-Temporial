import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.PTBTokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;

public class TestTokenizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "I was born Jan 1st 2000, and I got into the NBA last year, but got cut yesterday";
		String date = "2012-01-31";

		// Dummy object for TimeAnnotator constructor
		Properties noProperties = new Properties();

		// the pipeline
		AnnotationPipeline pipeline = new AnnotationPipeline();

		// Add annotators
		pipeline.addAnnotator(new PTBTokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		// pipeline.addAnnotator(new POSTaggerAnnotator(false));

		// Create annotation of our text
		Annotation annotation = new Annotation(text);

		// Set doc date
		annotation.set(CoreAnnotations.DocDateAnnotation.class, date);

		pipeline.annotate(annotation);

		List<CoreLabel> result = annotation
				.get(CoreAnnotations.TokensAnnotation.class);

		System.out.println("result: " + result);
	}

}
