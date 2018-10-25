package kn.uni.dbis.groups;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import kn.uni.dbis.groups.fitness.FitnessFunction;

public final class GeneticGroups {

	private final String[] tutorials;
	private final int[] capacities;
	private final FitnessFunction fitnessFunc;
	private final int popSize;
	private final int offspring;
	private final int mutations;

	private static final class Candidate {

		final int[] assignments;
		final double fitness;

		Candidate(final int[] assignments, final double fitness) {
			this.assignments = assignments;
			this.fitness = fitness;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj ||
					obj instanceof Candidate && Arrays.equals(this.assignments, ((Candidate) obj).assignments);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(this.assignments);
		}
	}

	public GeneticGroups(final Map<String, Integer> groups, final FitnessFunction fitnessFunc) {
		this(groups, fitnessFunc, 1000, 100, 100);
	}

	public GeneticGroups(final Map<String, Integer> groups, final FitnessFunction fitnessFunc,
			final int popSize, final int offspring, final int mutations) {
		this.tutorials = new String[groups.size()];
		this.capacities = new int[groups.size()];
		this.fitnessFunc = fitnessFunc;
		this.popSize = popSize;
		this.offspring = offspring;
		this.mutations = mutations;
		int i = 0;
		for (final Entry<String, Integer> e : groups.entrySet()) {
			this.tutorials[i] = e.getKey();
			this.capacities[i] = e.getValue();
			i++;
		}
	}

	public int[] optimize(final int[][] priorities, final int limit) {
		final Random rng = new Random();
		final int n = this.tutorials.length;

		final int[] firstCandidate = new int[priorities.length];
		OUTER: for (int i = 0; i < priorities.length; i++) {
			final int[] stud = priorities[i];
			for (int j = 0; j < stud.length; j++) {
				if (stud[j] == 0) {
					firstCandidate[i] = j;
					continue OUTER;
				}
			}
			throw new AssertionError();
		}

		long time = System.currentTimeMillis();
		final Set<Candidate> set = new HashSet<>();
		set.add(new Candidate(firstCandidate, this.fitnessFunc.fitness(firstCandidate, priorities, this.capacities)));

		Candidate winner = null;
		for (long run = 0; run <= limit; run++) {
			final Candidate[] arr = new Candidate[set.size()];
			set.toArray(arr);
			set.clear();

			Arrays.sort(arr, (a, b) -> Double.compare(b.fitness, a.fitness));

			winner = arr[0];

			final int pop = Math.min(this.popSize, arr.length);
			double sumFitness = 0;
			for (int i = 0; i < pop; i++) {
				set.add(arr[i]);
				sumFitness += arr[i].fitness;
			}

			if (pop > 1) {
				for (int i = 0; i < this.offspring; i++) {
					final int a = rng.nextInt(pop);
					final int b0 = rng.nextInt(pop - 1);
					final int b = b0 < a ? b0 : b0 + 1;
					final int[] e1 = arr[a].assignments, e2 = arr[b].assignments;
					final int[] res = e1.clone();
					for (int j = 0; j < res.length; j++) {
						if (rng.nextBoolean()) {
							res[j] = e2[j];
						}
					}
					set.add(new Candidate(res, this.fitnessFunc.fitness(res, priorities, this.capacities)));
				}
			}

			for (int i = 0; i < this.mutations; i++) {
				if (rng.nextBoolean()) {
					final int[] res = arr[rng.nextInt(pop)].assignments.clone();
					final int a = rng.nextInt(res.length);
					final int b0 = rng.nextInt(res.length - 1);
					final int b = b0 < a ? b0 : b0 + 1;
					final int tmp = res[a];
					res[a] = res[b];
					res[b] = tmp;
					final double fitness = this.fitnessFunc.fitness(res, priorities, capacities);
					if (fitness > 0) {
						set.add(new Candidate(res, fitness));
					}
				} else {
					final int[] res = arr[rng.nextInt(pop)].assignments.clone();
					final int a = rng.nextInt(res.length);
					final int tut = rng.nextInt(n);
					res[a] = tut;
					final double fitness = this.fitnessFunc.fitness(res, priorities, this.capacities);
					if (fitness > 0) {
						set.add(new Candidate(res, fitness));
					}
				}
			}

			if (run % 100 == 0) {
				final int[] choices = new int[winner.assignments.length];
				final int[] tuts = new int[n];
				final int[] hist = new int[n];
				for (int i = 0; i < winner.assignments.length; i++) {
					choices[i] = priorities[i][winner.assignments[i]];
					hist[choices[i]]++;
					tuts[winner.assignments[i]]++;
				}

				final long newTime = System.currentTimeMillis();
				System.out.println("\nround: " + run);
				System.out.println("\tfitness: " + winner.fitness);
				System.out.println("\taverage fitness: " + sumFitness / pop);
				System.out.println("\ttime: " + (newTime - time));
				
				System.out.println("\n\twinner: " + Arrays.toString(
						IntStream.of(winner.assignments)
								.mapToObj(i -> tutorials[i])
								.toArray(len -> new String[len])));
				System.out.println("\thistogram: " + Arrays.toString(hist));
				System.out.println("\ttutorials: " + Arrays.toString(tuts));
			}
		}
		return winner.assignments;
	}
}
