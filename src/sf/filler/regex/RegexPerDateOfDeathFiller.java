package sf.filler.regex;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.SUTimeMain;
import edu.stanford.nlp.time.SUTime.Duration;
import edu.stanford.nlp.time.SUTime.Range;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.time.SUTime.Time;

import sf.SFConstants;
import sf.SFEntity;
import sf.filler.Filler;
import tackbp.KbEntity.EntityType;

/**
 * Needs "tokens", "meta",
 * 
 * @author xiaoling
 * 
 */
public class RegexPerDateOfDeathFiller extends Filler {
	private final String[] KEY_DEATH_WORDS = { "death", "dead", "died",
			"passed", "killed", "suicide" };

	private SUTimeUtils suTimeUtils;

	public RegexPerDateOfDeathFiller() {
		slotName = "per:date_of_birth";
		this.suTimeUtils = new SUTimeUtils();
	}

	@Override
	public void predict(SFEntity mention, Map<String, String> annotations) {
		// the query needs to be a PER type.
		if (mention.ignoredSlots.contains(slotName)
				|| mention.entityType != EntityType.PER) {
			return;
		}

		// check if the entity is mentioned.
		String tokens = annotations.get(SFConstants.TOKENS);
		if (!tokens.contains(mention.mentionString)) {
			return;
		}
		// tokens = suTimeUtils.stdTimeInSentence(tokens, "2013-02-03");

		// get the filename of this sentence.
		String[] meta = annotations.get(SFConstants.META).split("\t");
		String filename = meta[2];

		// get the sentence of this sentence
		// String[] textMeta = annotations.get(SFConstants.TEXT).split("\t");
		// String sentence = textMeta[1];

		String[] names = mention.mentionString.split(" ");
		String first = names[0];
		String last = null;
		if (names.length > 1) {
			last = names[names.length - 1];
		} else {
			last = first;
		}

//		// TODO
//		if (mention.mentionString.equals("Michael Sandy")) {
//			String types = annotations.get(SFConstants.STANFORDPOS);
//			String proc = annotations.get(SFConstants.DEPS_STANFORD_CC_PROCESSED);
//			System.out.println(tokens);
//			System.out.println(types);
//			System.out.println(proc);
//			
//		}
		
		// Get some NL data		
		String[] tokensArray = tokens.split("\t")[1].split(" ");
		String[] posArray = null;
		
		String standfordposWithId = annotations.get(SFConstants.STANFORDPOS);
		if (standfordposWithId != null) {
			String[] standfordposArr = standfordposWithId.split("\t");
			if (standfordposArr.length >= 2) {
				posArray = standfordposArr[1].split(" ");
			}
		}
		
		for (String keyWord : KEY_DEATH_WORDS) {
			int index = stringArrayContains(tokensArray, keyWord);
			while (index != -1) {
				if (isNNtoINRule(mention.mentionString, tokensArray, posArray, index)) {
					System.out.println(mention.mentionString + "::" + tokens);
				}
				
				index = stringArrayContains(tokensArray, keyWord, index + 1);
			}
		}

	}
	
	private boolean isNNtoINRule(String names, String[] tokensArray, String[] posArray, int index) {
		if (names == null || tokensArray == null || posArray == null || index < 0) {
			return false;
		}
		
		if (posArray[index].equals("NN") && 
				posArray[index + 1].equals("IN")) {
			String nameAfterRule = tokensArray[index + 2];
			if (names.contains(nameAfterRule)) {
				return true;
			}
		}
		return false;
	}
	
	private int stringArrayContains(String[] arr, String s, int offset) {
		if (arr == null || s == null) {
			return -1;
		}
		
		for (int i = offset; i < arr.length; i++) {
			if (arr[i].equals(s)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private int stringArrayContains(String[] arr, String s) {
		return this.stringArrayContains(arr, s, 0);
	}

}
