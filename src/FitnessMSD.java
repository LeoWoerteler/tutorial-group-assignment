
public class FitnessMSD implements FitnessFunction {

	@Override
	public double fitness(int[] candidate, int[][] priorities, int[] caps) {
		final int n = caps.length;
		double fitness = 0;
		final int[] groupSizes = new int[n];
		for (int sid = 0; sid < candidate.length; sid++) {
			final int group = candidate[sid];
			groupSizes[group]++;
			final int prio = priorities[sid][group];
			if (prio < 0) {
				return 0;
			}
			fitness += Math.pow(0.1, prio);
			// fitness += Math.pow(0.5, prio);
			// fitness += Math.pow(0.75, prio);
			// fitness += 1 - 1.0 * prio / (N - 1);
		}

		int minSize = candidate.length, maxSize = 0;
		final double average = 1.0 * candidate.length / n;
		double sd = 0;
		for (int gid = 0; gid < groupSizes.length; gid++) {
			final int size = groupSizes[gid];
			if (size < minSize) {
				minSize = size;
			}
			if (size > maxSize) {
				maxSize = size;
			}
			double dist = size - average;
			sd += dist * dist;
		}

		final double msd = sd / n;
		// return Math.pow(0.95, Math.max(0, maxSize - minSize - 2)) * fitness;
		// return 1 / (1 + msd) * fitness;
		return Math.pow(0.97, msd) * fitness;
	}
}
