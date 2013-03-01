import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import cuny.PropertiesTag;
import cuny.UWClassifierSurfaceFeature;
import edu.cuny.qc.cs.kbp.core.SlotTime;
import edu.cuny.qc.cs.kbp.searching.SourceDataSearcher;
import edu.cuny.qc.cs.kbp2011.tie.TSFScore_Revised;
import edu.cuny.qc.cs.kbp2011.tie.Utilities;
import edu.cuny.qc.cs.kbp2011.tie.Utils;
import edu.cuny.qc.cs.kbp2011.tie.aggregation.Aggregator;
import edu.cuny.qc.cs.kbp2011.tie.aggregation.AggregatorBasic;
import edu.cuny.qc.cs.kbp2011.tie.aggregation.AggregatorBiasHolds;
import edu.cuny.qc.cs.kbp2011.tie.aggregation.AggregatorDiscardNone;
import edu.cuny.qc.cs.kbp2011.tie.core.KBP2011TemporalQuery;
import edu.cuny.qc.cs.kbp2011.tie.core.TemporalSFSystem;
import edu.cuny.qc.cs.kbp2011.tie.kernel.ClassifierDepKernel;

public class SystemDriverUW {
	private static final boolean isCommandLine = false;

	private static final int CLASSIFIER_TYPE = 0;
	private static final int AGGREGATOR_TYPE = 0;
	private static final int QUERY_TYPE = 0;
	private static final int DOCUMENT_NUM = 0;

	public static void main(String[] args) throws IOException {

		/* Grab configurations */
		// 0: surface feature | 1 dep Kernel
		int classifierType = isCommandLine ? Integer.parseInt(args[0])
				: CLASSIFIER_TYPE;

		// 0: basic | 1: bias | 2: discardNone
		int aggregatorType = isCommandLine ? Integer.parseInt(args[1])
				: AGGREGATOR_TYPE;

		// 0: dev | 1: test
		int queryType = isCommandLine ? Integer.parseInt(args[2]) : QUERY_TYPE;

		// # of relevant doc to search for
		int document_num = isCommandLine ? Integer.parseInt(args[3])
				: DOCUMENT_NUM;

		/* read file paths from property file */
		// Load *.properties file
		Properties defaultProps = new Properties();
		defaultProps.load(new FileInputStream(PropertiesTag.FILE_INPUT));

		// Development queries, tuples
		File developmentTemporalQueriesFile = new File(
				defaultProps.getProperty(PropertiesTag.DEV_TEMP_QUERIES));
		File developmentTuplesFile = new File(
				defaultProps.getProperty(PropertiesTag.DEV_TUPLES));

		// idx, output, annotation
		File sourceDataIndexPath = new File(
				defaultProps.getProperty(PropertiesTag.SRC_DATA_IDX));
		File outputPath = new File(
				defaultProps.getProperty(PropertiesTag.OUTPUT));
		File annotatedDocsPath = new File(
				defaultProps.getProperty(PropertiesTag.ANNOTATED_DOCS));

		// Test query
		File testTemporalQueriesFile = new File(
				defaultProps.getProperty(PropertiesTag.TEST_TEMP_QUERIES));
		File sfOutputFile = new File(
				defaultProps.getProperty(PropertiesTag.SF_OUTPUT));

		// Load queries
		HashMap<String, KBP2011TemporalQuery> queries = null;
		if (queryType == 0) {
			// Training
			queries = Utils.loadKBP2011TemporalSFQueries(
					developmentTemporalQueriesFile, developmentTuplesFile);
		} else if (queryType == 1) {
			// Eval
			queries = Utilities.loadOfficialKBP2011SFOutputAsTemporalSFQueries(
					testTemporalQueriesFile, sfOutputFile);
		} else {
			System.err.println("invalid query type");
			System.exit(1);
		}

		SourceDataSearcher sourceDataSearcher = new SourceDataSearcher(
				sourceDataIndexPath);

		/* Aggregator, select from aggregatorType from config arguments */
		Aggregator aggregator = new AggregatorBasic();
		Vector<Aggregator> vec_agg = new Vector<Aggregator>();
		vec_agg.add(aggregator);
		aggregator = new AggregatorBiasHolds();
		vec_agg.add(aggregator);
		aggregator = new AggregatorDiscardNone();
		vec_agg.add(aggregator);

		aggregator = vec_agg.get(aggregatorType);

		// dependency kernel system
		File titlesGazFile = new File(
				defaultProps.getProperty(PropertiesTag.TITLES_GAZ));
		File dep_kernel_modelsPath = new File(
				defaultProps.getProperty(PropertiesTag.DEP_KERNEL_MODELS));
		// surface feature system
		File surfaceFeature_modelsPath = new File(
				defaultProps.getProperty(PropertiesTag.SURFACE_FEATURE_MODELS));

		Vector<TemporalSFSystem> systems = new Vector<TemporalSFSystem>();
		// surface feature system, see constructors for more information
		boolean propagateLocations = false;
		boolean useEntityDocument = false;
		// This is what we want <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		TemporalSFSystem system = new UWClassifierSurfaceFeature(
				sourceDataSearcher, annotatedDocsPath, titlesGazFile,
				surfaceFeature_modelsPath, 1, aggregator, document_num,
				propagateLocations, useEntityDocument);
		systems.add(system);
		// dependency kernel system, see constructors for more information
		system = new ClassifierDepKernel(sourceDataSearcher, annotatedDocsPath,
				titlesGazFile, dep_kernel_modelsPath, 2, aggregator,
				document_num, propagateLocations, useEntityDocument);
		systems.add(system);

		// run for system
		system = systems.get(classifierType);

		HashMap<String, SlotTime> queryKey2answer = system.run(queries);
		String systemName = system.getClass().getSimpleName();
		// Write results
		File outputFile = new File(outputPath, systemName + ".out");
		Utilities.saveKBP2011TemporalSFOutput(systemName, queries,
				queryKey2answer, outputFile);

		// Evaluate the system output
		/*
		 * for(KBP2011TemporalQuery query : queries.values()){ String queryID =
		 * query.getQueryid(); String entityName = query.getName();
		 * for(InformationSlot slot : query.getInformationSlots()){
		 * 
		 * String slotName = slot.getSlotName().toString(); String slotText =
		 * slot.getText(); SlotTime goldStdTime = slot.getSlotTime(); String key
		 * = queryID+"#"+slotName+"#"+slotText; SlotTime answer =
		 * queryKey2answer.get(key);
		 * 
		 * if(answer == null) {
		 * System.err.println("!! Couldn't find answer ("+key
		 * +") in the system output"); } else {
		 * System.out.println(entityName+"\t"+slotName+"\t"+slotText);
		 * System.out.println("Gold Standard:");
		 * System.out.println(goldStdTime.toString());
		 * System.out.println("Answer:"); System.out.println(answer.toString());
		 * System.out.println(""); } } }
		 */
		if (queryType == 0) {
			// only development set run scorer
			TSFScore_Revised.main(new String[] { outputFile.getAbsolutePath(),
					developmentTuplesFile.getAbsolutePath() });
		}
	}
}
