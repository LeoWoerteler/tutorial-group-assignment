
public class FitnessStefan implements FitnessFunction {

	public static final double[] PENALTIES = { 1.0/64, 2.0/64, 4.0/64, 8.0/64, 16.0/64, 32.0/64, 64.0/64 };
	public static final double[] PENALTIES2 = { 1.0/128, 3.0/128, 7.0/128, 15.0/128, 31.0/128, 63.0/128, 127.0/128 };
	public static final double[] PENALTIES3 = { 1.0/49, 1.0/36, 1.0/25, 1.0/16, 1.0/9, 1.0/4, 1 };
	public static final double[] PENALTIES4 = { 1.0/343, 1.0/216, 1.0/125, 1.0/64, 1.0/27, 1.0/8, 1 };
	
	private final double[] penalties;

	public FitnessStefan(final double[] penalties) {
		this.penalties = penalties;
	}

	@Override
	public double fitness(int[] candidate, int[][] priorities, int[] caps) {
		double fitness = 0;
		final int[] groupSizes = new int[caps.length];
		for (int sid = 0; sid < candidate.length; sid++) {
			final int group = candidate[sid];
			groupSizes[group]++;
			final int prio = priorities[sid][group];
			if (prio < 0) {
				return 0;
			}
			fitness += Math.pow(1 - penalties[prio], prio);
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

		return Math.pow(0.95, Math.max(0, maxSize - minSize - 2)) * fitness;
	}
}