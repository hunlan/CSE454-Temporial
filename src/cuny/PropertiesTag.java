package cuny;

public interface PropertiesTag {
	public static final String FILE_INPUT = "props/default.properties";

	public static final String DEV_TEMP_QUERIES = "developmentTemporalQueriesFilePath";
	public static final String DEV_TUPLES = "developmentTuplesFilePath";

	public static final String SRC_DATA_IDX = "sourceDataIndexPath";
	public static final String OUTPUT = "outputPath";
	public static final String ANNOTATED_DOCS = "annotatedDocsPath";

	public static final String TEST_TEMP_QUERIES = "testTemporalQueriesFilePath";
	public static final String SF_OUTPUT = "sfOutputFile";

	public static final String TITLES_GAZ = "titlesGazFilePath";
	public static final String DEP_KERNEL_MODELS = "dep_kernel_modelsPath";
	public static final String SURFACE_FEATURE_MODELS = "surfaceFeature_modelsPath";

	// Actual Path
	public static final String CANDIDATE_INSTANCES_PATH = "./../mylovelydata/systemOutput/candidateInstances";
}
