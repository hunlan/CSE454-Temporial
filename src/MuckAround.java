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
import edu.stanford.nlp.util.CoreMap;

public class MuckAround {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "I was born Jan 1st 2000, and I got into the NBA last year, but got cut yesterday";
		String date = "2012-01-31";

		// Dummy object for TimeAnnotator constructor
		Properties noProperties = new Properties();

		// time Annotator for pipeline
		Annotator timeAnnotator = new TimeAnnotator("sutime", noProperties);

		// the pipeline
		AnnotationPipeline pipeline = new AnnotationPipeline();

		// Add annotators
		pipeline.addAnnotator(new PTBTokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		// pipeline.addAnnotator(new POSTaggerAnnotator(false));

		// add timeAnnotator to pipeline
		pipeline.addAnnotator(timeAnnotator);

		// Create annotation of our text
		Annotation annotation = new Annotation(text);

		// Set doc date
		annotation.set(CoreAnnotations.DocDateAnnotation.class, date);

		// Perform
		pipeline.annotate(annotation);

		// Get result
		List<CoreMap> timexAnnsAll = annotation
				.get(TimeAnnotations.TimexAnnotations.class);

		// Print result
		System.out.println("result: " + timexAnnsAll);

		
//		// Test 2
//		for (CoreMap timexAnn: timexAnnsAll) {
//			for (CoreLabel token : timexAnn.get(TokensAnnotation.class)) {
//				String word = token.getString(TextAnnotation.class);
//				System.out.println("word = " + word);
//			}
//		}
		
//		// TESTING---------------
//		// this value is null
//		int charBeginOffset = 0;
//		// annotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//
//		List<ValuedInterval<CoreMap, Integer>> timexList = new ArrayList<ValuedInterval<CoreMap, Integer>>(
//				timexAnnsAll.size());
//
//		for (CoreMap timexAnn : timexAnnsAll) {
//			Timex timex = timexAnn.get(TimeAnnotations.TimexAnnotation.class);
//			System.out.println("bla: " + timex.value());
//			
//			// TODO what does interval mean in this case??
//			Interval<Integer> interval = MatchedExpression.COREMAP_TO_CHAR_OFFSETS_INTERVAL_FUNC
//					.apply(timexAnn);
//
//			timexList.add(new ValuedInterval<CoreMap, Integer>(timexAnn,
//					interval));
//		}
//
//		Collections.sort(timexList,
//				HasInterval.CONTAINS_FIRST_ENDPOINTS_COMPARATOR);
//
//		// createTimexNodesPresorted
//		// String str = annotation.get(CoreAnnotations.TextAnnotation.class);
//		// List<Node> nodes = new ArrayList<Node>();
//		int previousEnd = 0;
//		List<Element> timexElems = new ArrayList<Element>();
//		List<ValuedInterval<CoreMap, Integer>> processed = new ArrayList<ValuedInterval<CoreMap, Integer>>();
//		CollectionValuedMap<Integer, ValuedInterval<CoreMap, Integer>> unprocessed = new CollectionValuedMap<Integer, ValuedInterval<CoreMap, Integer>>(
//				CollectionFactory
//						.<ValuedInterval<CoreMap, Integer>> arrayListFactory());
//		for (ValuedInterval<CoreMap, Integer> v : timexList) {
//			CoreMap timexAnn = v.getValue();
//			int begin = timexAnn
//					.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)
//					- charBeginOffset;
//			
//			int end = timexAnn
//					.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)
//					- charBeginOffset;
//			
//			if (begin >= previousEnd) {
//				// Add text
//				// nodes.add(XMLUtils.createTextNode(str.substring(previousEnd,
//				// begin)));
//				
//				// Add timex
//				Timex timex = timexAnn
//						.get(TimeAnnotations.TimexAnnotation.class);
//				Element timexElem = timex.toXmlElement();
//
//				System.out.println("---- " + timex.value()); //TODO: i added
//				
//				//nodes.add(timexElem);
//				previousEnd = end;
//
//				// For handling nested timexes
//				processed.add(v);
//				timexElems.add(timexElem);
//			} else {
//				unprocessed.add(processed.size() - 1, v);
//				System.out.println("Oh No!!!");
//			}
//		}
//		// if (previousEnd < str.length()) {
//		// nodes.add(XMLUtils.createTextNode(str.substring(previousEnd)));
//		// }
//		// for (Integer i : unprocessed.keySet()) {
//		// ValuedInterval<CoreMap, Integer> v = processed.get(i);
//		// String elemStr = v.getValue().get(
//		// CoreAnnotations.TextAnnotation.class);
//		// int charStart = v.getValue().get(
//		// CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//		// List<Node> innerElems = createTimexNodesPresorted(elemStr,
//		// charStart,
//		// (List<ValuedInterval<CoreMap, Integer>>) unprocessed.get(i));
//		// Element timexElem = timexElems.get(i);
//		// XMLUtils.removeChildren(timexElem);
//		// for (Node n : innerElems) {
//		// timexElem.appendChild(n);
//		// }
//		// }
//		// return nodes;
	}

}
