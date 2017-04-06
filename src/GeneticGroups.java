import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

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
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Candidate)) {
				return false;
			}
			return Arrays.equals(this.assignments, ((Candidate) obj).assignments);
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

	public static void main(final String[] args) throws IOException {
		final String file = "anonym.in";
		final String[] tutorials = { "A", "B", "C", "D", "E", "F" };
		final int[] caps = { 10, 12, 12, 10, 10, 10 };
		final int n = tutorials.length;

		final Map<String, Integer> tuts = new LinkedHashMap<>();
		final Map<String, Integer> tutorialMap = new HashMap<>();
		for (int i = 0; i < n; i++) {
			tuts.put(tutorials[i], caps[i]);
			tutorialMap.put(tutorials[i], i);
		}

		final List<String> ids = new ArrayList<>();
		final List<int[]> priorities = new ArrayList<>();
		try (final BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
			for (String line; (line = br.readLine()) != null;) {
				final String trimmed = line.trim();
				if (trimmed.length() == 0 || line.startsWith("#")) {
					continue;
				}

				final String[] parts = trimmed.split("\\s+");
				ids.add(parts[0]);
				final int[] prios = new int[n];
				Arrays.fill(prios, -2);

				int blacklist = prios.length;
				int pos = 0;
				for (int i = 1; i < parts.length; i++) {
					final String choice = parts[i];
					if (choice.equals("!")) {
						blacklist = i + 1;
						break;
					}
					prios[tutorialMap.get(choice)] = i - 1;
					pos++;
				}

				while (blacklist < parts.length) {
					prios[tutorialMap.get(parts[blacklist++])] = -1;
				}

				for (int i = 0; i < prios.length; i++) {
					if (prios[i] == -2) {
						prios[i] = pos;
					}
				}

				priorities.add(prios);
			}
		}

		final int[][] prioArray = priorities.toArray(new int[priorities.size()][]);
		System.out.println(prioArray.length);

		final GeneticGroups algo = new GeneticGroups(tuts, new FitnessLeo());
		final int[] result = algo.optimize(prioArray, 10000);
		System.out.println("\nStudent\tTutorium");
		for (int i = 0; i < result.length; i++) {
			System.out.println(ids.get(i) + "\t" + tutorials[result[i]]);
		}
	}
}
