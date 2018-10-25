package kn.uni.dbis.groups;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kn.uni.dbis.groups.fitness.FitnessStefan;

public class GroupAssignmentTest {

	public static void main(final String[] args) throws IOException {
		final Path file = Paths.get("example-groups.in");
		final String[] tutorials = { "A", "B", "C", "D", "E", "F" };
		final int[] caps = { 10, 10, 10, 10, 10, 10 };
		final int n = tutorials.length;

		final Map<String, Integer> tuts = new LinkedHashMap<>();
		final Map<String, Integer> tutorialMap = new HashMap<>();
		for (int i = 0; i < n; i++) {
			tuts.put(tutorials[i], caps[i]);
			tutorialMap.put(tutorials[i], i);
		}

		final int[] firsts = new int[tutorials.length];

		final List<String> ids = new ArrayList<>();
		final List<int[]> priorities = new ArrayList<>();
		try (final BufferedReader br = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
			for (String line; (line = br.readLine()) != null;) {
				final String trimmed = line.trim();
				if (trimmed.length() == 0 || line.startsWith("#")) {
					continue;
				}

				final String[] parts = trimmed.split("\\s+");
				ids.add(parts[0]);
				final int[] prios = new int[n];
				Arrays.fill(prios, -2);

				if (parts.length > 1) {
					firsts[tutorialMap.get(parts[1])]++;
				}

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

		final GeneticGroups algo = new GeneticGroups(tuts, new FitnessStefan(FitnessStefan.PENALTIES3));
		final int[] result = algo.optimize(prioArray, 10000);
		System.out.println("\nStudent\tTutorium");
		for (int i = 0; i < result.length; i++) {
			System.out.println(ids.get(i) + "\t" + tutorials[result[i]]);
		}

		System.out.println(Arrays.toString(firsts));
	}
}
