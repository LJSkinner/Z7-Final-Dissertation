/**
 * 
 */
package uk.ac.students.stir.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.InputMismatchException;
import java.util.List;

import uk.ac.students.stir.jputils.JPExtractor;

/**
 * This class is responsible for utilising the JavaParser metric extractor and
 * running it over open source projects which have been used in Gin.
 * 
 * 
 * @author Luke Skinner (2727141)
 *
 */
public class GinRunner {

	private static final String methodList = "AllMethods3.txt";

	private static final String outputFileName = "StatsFromJavaParser.csv";

	private static final String[] githubProjects = new String[] { "arthas", "disruptor", "druid", "gson", "jcodec",
			"junit", "ibatis", "opennlp", "spark", "spatial4j" };

	private static final String[] projectPaths = new String[] {
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\arthas\\core\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\disruptor\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\druid\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\gson\\gson\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\jcodec\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\junit4\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\mybatis-3\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\opennlp\\opennlp-tools\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\spark\\src\\main\\java\\",
			"D:\\OneDrive\\College\\Year 4\\CSCU9Z7 - Computing Science Project\\casestudies\\spatial4j\\src\\main\\java\\" };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			processFiles();
		} catch (FileNotFoundException fnfe) {
			System.err.printf("There was a problem with creating the file, please review the message:%n%s",
					fnfe.getMessage());
		} catch (IOException e) {
			System.err.printf(
					"An I/O error occured when attempting to read from the file, please review the message:%n%s",
					e.getMessage());
		}
	}

	private static void processFiles() throws IOException {
		final PrintStream outFile = new PrintStream(new FileOutputStream(outputFileName));

		List<String> methods = Files.readAllLines(new File(methodList).toPath());

		// Header line
		outFile.println("method," + "surfaceIfs,nestedIfs," + "surfaceSwitches,nestedSwitches,"
				+ "surfaceFors,nestedFors," + "surfaceForEachs,nestedForEachs," + "surfaceWhiles,nestedWhiles,"
				+ "surfaceDos,nestedDos," + "iterativeStmts,conditionalStmts");

		for (String method : methods) {
			System.out.println("Processing method " + method);

			int projectIndex = getProjectIndex(method);

			String methodFqNameMinusArgs = method.substring(0, method.lastIndexOf("("));

			String className = projectPaths[projectIndex]
					+ methodFqNameMinusArgs.substring(0, methodFqNameMinusArgs.lastIndexOf(".")).replace(".", "/")
					+ ".java";

			int[] metrics = computeMetrics(className, method);

			outFile.printf("\"%s\",%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n", method, metrics[0], metrics[1],
					metrics[2], metrics[3], metrics[4], metrics[5], metrics[6], metrics[7], metrics[8], metrics[9],
					metrics[10], metrics[11], metrics[12], metrics[13]);
		}

		outFile.close();

	}

	private static int[] computeMetrics(String sourceFile, String method) throws FileNotFoundException {
		JPExtractor jpExtractor = new JPExtractor(sourceFile);

		if (!jpExtractor.hasMethod(method)) {
			throw new InputMismatchException(
					"ERROR: Could not find the supplied method, something has went wrong internally. Please review Casestudies folder and source files");
		}

		int surfaceIfs = jpExtractor.numberOfSurfaceIfIn(method);

		int nestedIfs = jpExtractor.numberOfNestedIfIn(method);

		int surfaceSwitches = jpExtractor.numberOfSurfaceSwitchIn(method);

		int nestedSwitches = jpExtractor.numberOfNestedSwitchIn(method);

		int surfaceFors = jpExtractor.numberOfSurfaceForIn(method);

		int nestedFors = jpExtractor.numberOfNestedForIn(method);

		int surfaceForEachs = jpExtractor.numberOfSurfaceForEachIn(method);

		int nestedForEachs = jpExtractor.numberOfNestedForEachIn(method);

		int surfaceWhiles = jpExtractor.numberOfSurfaceWhileIn(method);

		int nestedWhiles = jpExtractor.numberOfNestedWhileIn(method);

		int surfaceDos = jpExtractor.numberOfSurfaceDoIn(method);

		int nestedDos = jpExtractor.numberOfNestedDoIn(method);

		int iterativeStmts = surfaceFors + nestedFors + surfaceForEachs + nestedForEachs + surfaceWhiles + nestedWhiles
				+ surfaceDos + nestedDos;

		int conditionalStmts = surfaceIfs + nestedIfs + surfaceSwitches + nestedSwitches;

		return new int[] { surfaceIfs, nestedIfs, surfaceSwitches, nestedSwitches, surfaceFors, nestedFors,
				surfaceForEachs, nestedForEachs, surfaceWhiles, nestedWhiles, surfaceDos, nestedDos, iterativeStmts,
				conditionalStmts };
	}

	private static int getProjectIndex(String method) {
		for (int i = 0; i < projectPaths.length; i++) {
			if (method.contains(githubProjects[i])) {
				return i;
			}
		}

		return -1;
	}

}
