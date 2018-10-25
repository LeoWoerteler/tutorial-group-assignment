package kn.uni.dbis.groups.fitness;

public class FitnessStefan2 implements FitnessFunction {

	@Override
	public double fitness(int[] candidate, int[][] priorities, int[] caps) {
		final int n = caps.length;
		double penalties = 1;
		final int[] groupSizes = new int[n];
		for (int sid = 0; sid < candidate.length; sid++) {
			final int group = candidate[sid];
			groupSizes[group]++;
			final int prio = priorities[sid][group];
			if (prio < 0) {
				return 0;
			}
			penalties += prio * prio;
		}
		final double maxPenalties = candidate.length * (n - 1) * (n - 1);
		int over = 0;
		for (int gid = 0; gid < groupSizes.length; gid++) {
			final int diff = Math.max(0, groupSizes[gid] - caps[gid]);
			over += diff * diff;
		}
		return Math.pow(0.95, Math.sqrt(over)) * (maxPenalties - penalties);
	}
}
