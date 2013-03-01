import java.util.List;

import sf.filler.regex.SUTimeUtils;
import edu.stanford.nlp.util.CoreMap;

public class Random {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "The major US stock indexes opened stronger Tuesday , rebounding from a selloff Monday amid renewed market optimism and as investors braced for monthly sales updates from the big auto manufacturers .";//"I was born Jan 1st 2000, and I got into the NBA last year, but got cut yesterday";
		String text2 = "Lets meet tomorrow";
		String date = "2012-01-31";

		//List<CoreMap> times = SUTimeUtils.getTimeMapFromSentence(text, date);
		SUTimeUtils suTimeUtils = new SUTimeUtils();
		String newTime = suTimeUtils.stdTimeInSentence(text, date);//suTimeUtils.standardizeFormat(text, date);//
		System.out.println(newTime);
		
		
	}

}
