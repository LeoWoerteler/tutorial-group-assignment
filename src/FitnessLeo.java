
public class FitnessLeo implements FitnessFunction {

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
			// fitness += Math.pow(0.1, prio);
			// fitness += Math.pow(0.5, prio);
			// fitness += Math.pow(0.75, prio);
			fitness += 1 - 1.0 * prio / (n - 1);
		}

		int minSize = candidate.length, maxSize = 0;
		for (int gid = 0; gid < groupSizes.length; gid++) {
			final int size = groupSizes[gid];
			if (size < minSize) {
				minSize = size;
			}
			if (size > maxSize) {
				maxSize = size;
			}
		}

		int over = 0;
		for (int gid = 0; gid < groupSizes.length; gid++) {
			final int diff = Math.max(0, groupSizes[gid] - caps[gid]);
			over += diff * diff;
		}

		return Math.pow(0.95, Math.sqrt(over)) * fitness;
	}
}
