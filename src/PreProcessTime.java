import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sf.SFConstants;
import sf.filler.regex.SUTimeUtils;
import sf.retriever.ProcessedCorpus;

public class PreProcessTime {

	public static final long START_INDEX = 0;
	public static final long END_INDEX = 10;

	public static void main(String[] args) {
		long startIdx = START_INDEX;
		long endIdx = END_INDEX;

		if (args.length == 2) {
			startIdx = Integer.parseInt(args[0]);
			endIdx = Integer.parseInt(args[1]);
		} else if (args.length == 1) {
			endIdx = Integer.parseInt(args[0]);
		} else if (args.length != 0) {
			throw new IllegalArgumentException();
		}

		if (endIdx < startIdx || startIdx < 0) {
			throw new IllegalArgumentException();
		}

		System.out.println("Processing from " + startIdx + " to " + endIdx);

		ProcessedCorpus corpus;
		SUTimeUtils timeUtil = new SUTimeUtils();

		try {
			FileWriter outFile = new FileWriter("out" + startIdx + "_" + endIdx
					+ ".TIME");
			PrintWriter out = new PrintWriter(outFile);

			corpus = new ProcessedCorpus();
			Map<String, String> annotations = null;

			/* Skip to chunk index */
			for (int i = 0; i < startIdx; i++) {
				if (corpus.hasNext()) {
					corpus.next();
				} else {
					System.out.println("Start Index larger than corpus");
					return;
				}
			}

			long c = startIdx;
			System.out.println("Starting at index " + c);
			

			/* Timing Collection */
			Set<Long> timeSet = new HashSet<Long>();
			while (corpus.hasNext()) {
				if (c >= endIdx) {
					break;
				}

				long startTime = System.nanoTime();

				annotations = corpus.next();
				if (c++ % 100000 == 0) {
					System.err.print("finished reading " + c + " lines\r");

					long tot = 0;
					for (long time : timeSet) {
						tot += time;
					}

					if (!timeSet.isEmpty()) {
						long avg = tot / timeSet.size();
						System.out.println("average time = " + avg + "ns");
					}
				}

				String[] tokens = annotations.get(SFConstants.TOKENS).split(
						"\t");
				String[] meta = annotations.get(SFConstants.META).split("\t");
				/*String time = annotations.get(SFConstants.TIME);
				System.out.println(tokens[0] + "\t Time: " + time); */
				String senid = meta[2];
				String dateraw = senid.substring(8, 16);
				String date = dateraw.substring(0, 4) + "-"
						+ dateraw.substring(4, 6) + "-"
						+ dateraw.substring(6, 8);

				String result = timeUtil.standardizeFormat(tokens[1], date);

				/* System.out.println(tokens[0] + result); */
				out.println(tokens[0] + result);
				long endTime = System.nanoTime();

				long duration = endTime - startTime;
				timeSet.add(duration);
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done!");
	}
}
