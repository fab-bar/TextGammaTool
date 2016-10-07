package iaa;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import corpus.AnnotatedText;
import corpus.alignment.Alignment;
import corpus.alignment.dissimilarity.Dissimilarity;
import corpus.merge.AnnotatedTextMerge;
import iaa.textgamma.DisorderSampler;

public class TextGamma {
	
	public static double getGamma(AnnotatedText orig, AnnotatedText a1, AnnotatedText a2,
			Dissimilarity d, DisorderSampler sampler,
			char openUnit, char closeUnit, char gap,
			float precision, float alpha) {

		double disorder_observed = TextGamma.getObservedDisorder(a1, a2, d, openUnit, closeUnit, gap);
		double disorder_expected = TextGamma.getExpectedDisorder(sampler, precision, alpha);

		return 1 - (disorder_observed/disorder_expected);
	}

	public static double getObservedDisorder(AnnotatedText a1, AnnotatedText a2,
			Dissimilarity d,
			char openUnit, char closeUnit, char gap) {
		
		double min_disorder = Double.MAX_VALUE;
		
		for (Alignment al: AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(a1, a2, openUnit, closeUnit, gap)) {
			double disorder = al.getDisorder(d);
			if (disorder < min_disorder)
				min_disorder = disorder;
		}
		return min_disorder;
	}

	public static double getExpectedDisorder(DisorderSampler sampler, float precision, float alpha) {

		long n = 30;

		Mean m = new Mean();
		StandardDeviation v = new StandardDeviation(true);

		NormalDistribution sn = new NormalDistribution(null, 0, 1);

		while (m.getN() < n) {

			while (m.getN() < n) {
				double disorder = sampler.sampleDisorder();
				m.increment(disorder);
				v.increment(disorder);
			}

			// reestimate n
			double mean_disorder = m.getResult();

			double sd_disorder = v.getResult();
			double cv = sd_disorder/mean_disorder;
			n = Math.round(Math.pow(cv*sn.inverseCumulativeProbability(1-(alpha/2))/precision, 2));

		}
		return m.getResult();
	}
}