package kn.uni.dbis.groups.fitness;

public interface FitnessFunction {
	double fitness(int[] candidate, int[][] priorities, int[] caps);
}
