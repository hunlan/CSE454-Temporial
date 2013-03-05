package cuny;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.cuny.qc.cs.kbp.core.InformationSlot;
import edu.cuny.qc.cs.kbp.core.SlotName;
import edu.cuny.qc.cs.kbp.core.SlotTime;
import edu.cuny.qc.cs.kbp.core.SourceDocument;
import edu.cuny.qc.cs.kbp.searching.SourceDataSearcher;
import edu.cuny.qc.cs.kbp2011.tie.aggregation.Aggregator;
import edu.cuny.qc.cs.kbp2011.tie.classification.ClassificationResult;
import edu.cuny.qc.cs.kbp2011.tie.core.KBP2011TemporalQuery;
import edu.cuny.qc.cs.kbp2011.tie.distantSupervision.stanfordWrapper.AnnotatedDocument;
import edu.cuny.qc.cs.kbp2011.tie.distantSupervision.stanfordWrapper.NamedEntity;
import edu.cuny.qc.cs.kbp2011.tie.distantSupervision.stanfordWrapper.Sentence;
import edu.cuny.qc.cs.kbp2011.tie.distantSupervision.stanfordWrapper.SentenceSequence;
import edu.cuny.qc.cs.kbp2011.tie.kernel.ClassifierDepKernel;
import edu.cuny.qc.cs.kbp2011.tie.surfaceFeature.SurfaceFeatureSVMWrapper;

public class UWClassifierSurfaceFeature extends ClassifierDepKernel
{
	protected String Model_File_Prefix = "fold_0.fv.w4dep.model_";
	protected String Feature_File_Prefix = "fold_0.fv.w4dep.";
	
	public UWClassifierSurfaceFeature(SourceDataSearcher sourceDataSearcher, File annotatedDocsPath, 
			File titlesGazFile, File modelsPath, boolean propagateLocations, boolean useEntitySourceDocument)
	{
		super(sourceDataSearcher, annotatedDocsPath, titlesGazFile,
				modelsPath, propagateLocations, useEntitySourceDocument);
		// by default, using just surface features without patterns
		Model_File_Prefix = "fold_0.fv.w4dep.model_";
		Feature_File_Prefix = "fold_0.fv.w4dep.";
	}
	
	public UWClassifierSurfaceFeature(SourceDataSearcher sourceDataSearcher, File annotatedDocsPath, File titlesGazFile, 
			File modelsPath, Aggregator agg, int documentSize, boolean propagateLocations, boolean useEntitySourceDocument)
	{
		super(sourceDataSearcher, annotatedDocsPath, titlesGazFile,
				modelsPath, 0, agg, documentSize, propagateLocations, useEntitySourceDocument);
		// by default, using just surface features without patterns
		Model_File_Prefix = "fold_0.fv.w4dep.model_";
		Feature_File_Prefix = "fold_0.fv.w4dep.";
	}
	
	/**
	 * 
	 * @param sourceDataSearcher
	 * @param annotatedDocsPath
	 * @param titlesGazFile
	 * @param modelsPath
	 * @param ClassifierType: the type for classifier, 1 is window feature, 2 is windowfeature + patterns, 1 can be a reasonable choice
	 * @param agg: aggregator
	 * @param documentSize: the Threshold for number of documents when searching relevant documents 
	 */
	public UWClassifierSurfaceFeature(SourceDataSearcher sourceDataSearcher, File annotatedDocsPath, File titlesGazFile, 
			File modelsPath, int ClassifierType, Aggregator agg, int documentSize,
			boolean propagateLocations, boolean useEntitySourceDocument)
	{
		super(sourceDataSearcher, annotatedDocsPath, titlesGazFile,
				modelsPath, 0, agg, documentSize, propagateLocations, useEntitySourceDocument);
		if(ClassifierType == 1)
		{
			// this is surface feature model without patterns
			Model_File_Prefix = "fold_0.fv.w4dep.model_";
			Feature_File_Prefix = "fold_0.fv.w4dep.";
		}
		else if(ClassifierType == 2)
		{
			// this is surface featue model with patterns
			Model_File_Prefix = "fold_0.fv.w4deppat.model_";
			Feature_File_Prefix = "fold_0.fv.w4deppat.";
		}
	}
	
	
/**
 * This method is interface of classifier, SystemDriver will invoke this method
 */
public HashMap<String, SlotTime> run(HashMap<String, KBP2011TemporalQuery> queries) {
		
		HashMap<String, SlotTime> results = new HashMap<String, SlotTime>();

		try {
			// TODO : Modified
			log = new BufferedWriter(new FileWriter(new File("/home/hunlan/Desktop/2013_Winter/cse454/cuny/mylovelydata/templog/ClassifierSurfaceFeature.log")));//data/systemOutput/ClassifierSurfaceFeature.log")));

			String errorAnalysisLineHeader1 = "[class-analysis]\tqueryID\tentityName\tslotName\tslotValue\tgoldT1\tgoldT2\tgoldT3\tgoldT4\tsourceDocID\tsourceDocBody\t" +
					"normalizedSentence\toriginalSentence\ttimexString\ttimexNormalizedValue\tpredictedClass\t" +
					"classifierConfidenceValues\n";
			String errorAnalysisLineHeader2 = "[tuple-analysis]\tqueryID\tentityName\tslotName\tslotValue\tgoldT1\tgoldT2\tgoldT3\tgoldT4\tsourceDocID\tsourceDocBody\t" +
			        "sysT1\tsysT2\tsysT3\tsysT4\n";
			
			log.write(errorAnalysisLineHeader1);
			log.write(errorAnalysisLineHeader2);
			
			/* FOR EACH SLOT */
			for(SlotName currentSlot : SlotName.values()){
				// We care about 
				if(!(currentSlot.equals(SlotName.per_employee_of) ||
						currentSlot.equals(SlotName.per_member_of) ||
						currentSlot.equals(SlotName.org_top_members_employees) ||
						currentSlot.equals(SlotName.per_countries_of_residence) ||
						currentSlot.equals(SlotName.per_stateorprovinces_of_residence) ||
						currentSlot.equals(SlotName.per_cities_of_residence) ||
						currentSlot.equals(SlotName.per_spouse) ||
						currentSlot.equals(SlotName.per_title))){					
					continue;
				}
				
				//We normalize the slot name internally because we treat some slots with the same classification models
				String currentInfSlotName = currentSlot.toString();
				if(currentSlot.equals(SlotName.per_countries_of_residence)){
					currentInfSlotName = "per_residence";
				} else if(currentSlot.equals(SlotName.per_cities_of_residence) ||
						currentSlot.equals(SlotName.per_stateorprovinces_of_residence)) {
					continue;
				}
				
				if(currentSlot.equals(SlotName.per_employee_of)){
					currentInfSlotName = "per_employee_of";
				} else if(currentSlot.equals(SlotName.per_member_of) ||
						currentSlot.equals(SlotName.org_top_members_employees)) {
					continue;
				}
				
				// print out slotName
				System.out.println("CurrentInfSlotName: " + currentInfSlotName);
			
				HashMap<String, NamedEntity> instanceID2timex = new HashMap<String, NamedEntity>(); 
				HashMap<String, Set<String>> key2instanceID = new HashMap<String, Set<String>>();
				
				// for each slot type, generate a file contains all instances that to be classified
				String candidateInstancesFilePath = PropertiesTag.CANDIDATE_INSTANCES_PATH + currentInfSlotName;
				BufferedWriter writer_instances = new BufferedWriter(new FileWriter(candidateInstancesFilePath));
				
				/* FOR EACH QUERY(TRAINING DATA QUERY) */
				for(KBP2011TemporalQuery query : queries.values()){
					
					/* FOR EACH INFOSLOT IN QUERY */
					for(InformationSlot infSlot : query.getInformationSlots()){	
						
						String slotName = infSlot.getSlotName().toString();
						if(infSlot.getSlotName().equals(SlotName.per_countries_of_residence) ||
								infSlot.getSlotName().equals(SlotName.per_cities_of_residence) ||
								infSlot.getSlotName().equals(SlotName.per_stateorprovinces_of_residence)){
							slotName = "per_residence";
						}
						if(infSlot.getSlotName().equals(SlotName.per_employee_of) ||
								infSlot.getSlotName().equals(SlotName.per_member_of) ||
								infSlot.getSlotName().equals(SlotName.org_top_members_employees)){
							slotName = "per_employee_of";
						}
						
						if(!slotName.equals(currentInfSlotName)) continue;

						String queryInfo = query.getQueryid() + "\n"+query.getName()+"\t"+infSlot.getSlotName()+"\t"+infSlot.getSlotFill();
						
						System.out.println("queryInfo: " + queryInfo);
						
						
						//Get the relevant documents to the query
						HashSet<SourceDocument> docs = getRelevantDocuments(query, infSlot, topDocumentsFromIndex, useEntitySourceDocument);
						 // System.out.println("docs: " + docs);
						
						//Get relevant sentences in the documents
						//System.out.print("\t- selectSentences... ");
						HashMap<AnnotatedDocument, HashSet<Sentence>> annotatedDoc2sentences = selectSentences(query, infSlot, docs); 
						int numSent = 0;
						for(HashSet<Sentence> sentences : annotatedDoc2sentences.values()) numSent += sentences.size();
						// System.out.print(numSent+"\n");
						
						//Represent each temporal expression as a classification instance, output to a file, and get map from instance_ID to timex namedEntity 
						HashMap<String, NamedEntity> iid2timex = printInstances(writer_instances, query, infSlot, annotatedDoc2sentences);
						instanceID2timex.putAll(iid2timex);
						
						// Qi: incorperate Javier's changes for SentenceSequences
						if(numSent == 0) {

							HashMap<AnnotatedDocument, HashSet<SentenceSequence>> annotatedDoc2sentenceSequences = selectSentenceSequences(query, infSlot, docs); 
							
							// Qi: add sentence sequences instances
							iid2timex = printInstancesForSentenceSequences(writer_instances, query, infSlot, annotatedDoc2sentenceSequences);
							instanceID2timex.putAll(iid2timex);	
						}
						
						String slotSourceDoc = infSlot.getSourceDocuments().iterator().next();
						String key = query.getQueryid()+"#"+infSlot.getSlotName()+"#"+infSlot.getSlotFill()+"#"+slotSourceDoc;
						//String key = query.getQueryid()+"#"+infSlot.getSlotName()+"#"+infSlot.getSlotFill();
						
						key2instanceID.put(key, iid2timex.keySet());
					}
				}
				writer_instances.close();
				
				//Run classifier
				System.out.println("Running classifier for "+currentInfSlotName);
				
				// get model file, output path, and perform classification
				String outputClassificationFile = "../mylovelydata/systemOutput/" + currentInfSlotName;//"data/systemOutput/" + currentInfSlotName;
				String modelFilePath = modelsPath.getAbsolutePath() + File.separatorChar + Model_File_Prefix + currentInfSlotName;	    // file path for libsvm model
				String featureFilepath = modelsPath.getAbsolutePath() + File.separatorChar + Feature_File_Prefix + currentInfSlotName; 	// file path for .ser file of training data
				String patternFilePath = "/kbp/assignments/mylovelydata/patterns/patterns/per-countries_of_residence";// modelsPath.getAbsolutePath() + File.separatorChar + currentInfSlotName + "_patterns";
				
				// TODO Added
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> feature: " + featureFilepath);
				
				// classify
				SurfaceFeatureSVMWrapper.classify(featureFilepath, candidateInstancesFilePath, modelFilePath, outputClassificationFile, patternFilePath);
				
				// Read classification results
				HashMap<String, ClassificationResult> instanceID2classification = SurfaceFeatureSVMWrapper.readClassificationResults(outputClassificationFile);
			
				// Qi: aggregate results
				aggregateResults(queries, results, currentInfSlotName, instanceID2timex, key2instanceID, instanceID2classification);
				break;//TODO I added this
			}
			
			// Qi: refactor this as a method
			if(propagateLocations) propagateLocation(results);
			
			log.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return results;
	}
}
