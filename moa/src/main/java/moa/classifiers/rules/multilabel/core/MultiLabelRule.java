package moa.classifiers.rules.multilabel.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import moa.AbstractMOAObject;
import moa.classifiers.MultiLabelLearner;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.rules.core.anomalydetection.AnomalyDetector;
import moa.classifiers.rules.multilabel.attributeclassobservers.NominalStatisticsObserver;
import moa.classifiers.rules.multilabel.attributeclassobservers.NumericStatisticsObserver;
import moa.classifiers.rules.multilabel.core.splitcriteria.MultiLabelSplitCriterion;
import moa.classifiers.rules.multilabel.errormeasurers.MultiLabelErrorMeasurer;
import moa.classifiers.rules.multilabel.inputselectors.InputAttributesSelector;
import moa.classifiers.rules.multilabel.outputselectors.OutputAttributesSelector;
import moa.core.StringUtils;

import com.yahoo.labs.samoa.instances.InstanceInformation;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import com.yahoo.labs.samoa.instances.MultiLabelInstance;
import com.yahoo.labs.samoa.instances.Prediction;


public class MultiLabelRule extends AbstractMOAObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Literal> literalList = new LinkedList<Literal>();

	protected LearningLiteral learningLiteral;
	

	protected int ruleNumberID;
	
	protected MultiLabelRule otherBranchRule;
	
	protected InstanceInformation instanceInformation;
	
	public MultiLabelRule(LearningLiteral learningLiteral) {
		this.learningLiteral=learningLiteral; //copy()?
	}

	public MultiLabelRule() {
		
	}
	
	public MultiLabelRule(int id) {
		this();
		ruleNumberID=id;
	}

	public int getRuleNumberID() {
		return ruleNumberID;
	}

	public void setRuleNumberID(int ruleNumberID) {
		this.ruleNumberID = ruleNumberID;
	}
	
	public boolean isCovering(MultiLabelInstance inst) {
		boolean isCovering = true;
		for (Literal l : literalList) {
			if (l.evaluate(inst) == false) {
				isCovering = false;
				break;
			}
		}
		return isCovering;
	}
	
	public int[] getOutputsCovered() {
		return learningLiteral.getOutputsToLearn();
	}
	
	
	
	@Override
	public void getDescription(StringBuilder out, int indent) {
		StringUtils.appendIndented(out, indent+1, "Rule Nr." + this.ruleNumberID + " Instances seen:" + this.learningLiteral.getWeightSeenSinceExpansion() + "\n"); 
		for (Literal literal : literalList) {
			literal.getDescription(out, indent+1, instanceInformation);
			StringUtils.appendIndented(out, indent+1, " ");
		}
		StringUtils.appendIndented(out, indent+1, " Output: " + this.learningLiteral.getStaticOutput(instanceInformation));
	}

	 protected String getStaticOutput() {
		return "";
	}

	public boolean updateChangeDetection(MultiLabelInstance instance) {
		return this.learningLiteral.updateAndCheckChange(instance);
	}

	public boolean updateAnomalyDetection(MultiLabelInstance instance) {
		return this.learningLiteral.updateAndCheckAnomalyDetection(instance);
	}

	public void trainOnInstance(MultiLabelInstance instance) {
		if(this.instanceInformation==null)
			this.instanceInformation=((InstancesHeader)instance.dataset()).getInstanceInformation();
		learningLiteral.trainOnInstance(instance);
	}

	public double getWeightSeenSinceExpansion() {
		return learningLiteral.getWeightSeenSinceExpansion();
	}

	public LearningLiteral getLearningNode() {
		return learningLiteral;
	}

	public double [] getCurrentErrors() {
		return learningLiteral.getErrors();
	}

	public  Prediction getPredictionForInstance(MultiLabelInstance instance) {
		return learningLiteral.getPredictionForInstance(instance);
	}
	
	public boolean tryToExpand(double splitConfidence, double tieThresholdOption) {
		boolean hasExpanded=learningLiteral.tryToExpand(splitConfidence,tieThresholdOption);
		if(hasExpanded){
			otherBranchRule=new MultiLabelRule(learningLiteral.getOtherBranchLearningLiteral());
			//check for obsolete predicate
			int attribIndex=learningLiteral.getBestSuggestion().getPredicate().getAttributeIndex();
			boolean isEqualOrLess=learningLiteral.getBestSuggestion().getPredicate().isEqualOrLess();
			Iterator<Literal> it=literalList.iterator();
			while(it.hasNext()){
				Literal l=it.next();
				if(l.predicate.getAttributeIndex()==attribIndex && l.predicate.isEqualOrLess()==isEqualOrLess)
				{
					it.remove();
					break;
				}
			}
			this.literalList.add(new Literal(learningLiteral.getBestSuggestion().getPredicate()));
			learningLiteral=learningLiteral.getExpandedLearningLiteral();
			
		}
		return hasExpanded;
	}
	
	public MultiLabelRule getNewRuleFromOtherBranch(){
		return otherBranchRule;
	}
	
	@Override
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		getDescription(out, 1);
		return out.toString();
	}

	public void setSplitCriterion(MultiLabelSplitCriterion splitCriterion) {
		learningLiteral.setSplitCriterion(splitCriterion);
		
	}

	public void setChangeDetector(ChangeDetector changeDetector) {
		learningLiteral.setChangeDetector(changeDetector);
		
	}

	public void setAnomalyDetector(AnomalyDetector anomalyDetector) {
		learningLiteral.setAnomalyDetector(anomalyDetector);
		
	}

	public void setNumericObserverOption(
			NumericStatisticsObserver numericStatisticsObserver) {
		learningLiteral.setNumericObserverOption(numericStatisticsObserver);
		
	}

	public void setLearner(MultiLabelLearner learner) {
		learningLiteral.setLearner(learner);
		
	}

	public void setErrorMeasurer(MultiLabelErrorMeasurer errorMeasurer) {
		learningLiteral.setErrorMeasurer(errorMeasurer);
		
	}

	public void setOutputAttributesSelector(OutputAttributesSelector outputSelector) {
		learningLiteral.setOutputAttributesSelector(outputSelector);
		
	}

	public void setNominalObserverOption(NominalStatisticsObserver nominalStatisticsObserver) {
		learningLiteral.setNominalObserverOption(nominalStatisticsObserver);	
	}

	public void setRandomGenerator(Random random) {
		learningLiteral.setRandomGenerator(random);
	}

	public void setAttributesPercentage(double attributesPercentage) {
		learningLiteral.setAttributesPercentage(attributesPercentage);
		
	}

	public void setInputAttributesSelector(InputAttributesSelector inputSelector) {
		learningLiteral.setInputAttributesSelector(inputSelector);
		
	}


}
