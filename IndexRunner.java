import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IndexRunner {
	
	private File inputDir;
	private File outputDir;
	private int charsPerPage;
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		IndexRunner runner = new IndexRunner(new File(args[0]), new File(args[1]), Integer.parseInt(args[2]));
		runner.go();
		System.out.println(System.currentTimeMillis()-startTime);
	}
	
	public IndexRunner(File input, File output, int chars) {
		this.inputDir = input;
		this.outputDir = output;
		this.charsPerPage = chars;
	}
	
	public void go() {
		ArrayList<IndexWorker> workerList = new ArrayList<IndexWorker>();
		for (File x : this.inputDir.listFiles()) {
			IndexWorker worker = new IndexWorker(x, this.outputDir, this.charsPerPage);
			workerList.add(worker);
			worker.start();
		}
		for (IndexWorker worker : workerList) {
			try {
				worker.join();
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
	}
	
	
	public static class IndexWorker extends Thread {
		
		private File file;
		private File outputDir;
		private int charsPerPage;
		
		public IndexWorker(File inputFile, File output, int chars) {
			this.file = inputFile;
			this.outputDir = output;
			this.charsPerPage = chars;
		}
		
		public void run() {
			try {
				FileReader fr = new FileReader(this.file);
				BufferedReader br = new BufferedReader(fr);
				String line = null;
				int charCount = 0;
				int page = 1;
				HashMap<String, Set<Integer>> map = new HashMap<String, Set<Integer>>();
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						List<String> wordList = Arrays.asList(line.trim().toLowerCase().split("\\s+"));
						for (String word : wordList) {
							if (!word.trim().isEmpty()) {
								charCount = charCount+word.length();
								if (map.get(word) == null) {
									map.put(word, new HashSet<Integer>());
								}
								if (charCount > this.charsPerPage) {
									charCount = word.length();
									page++;
								}
								map.get(word).add(new Integer(page));
							}
						}
					}
				}
				
				br.close();
				fr.close();
				
				ArrayList<String> outList = new ArrayList<String>(map.keySet());
				Collections.sort(outList);
				
				FileWriter fw = new FileWriter(new File(this.outputDir, "a_" + this.file.getName()));
				BufferedWriter bw = new BufferedWriter(fw);
				
				for (String x : outList) {
					ArrayList<Integer> sorted = new ArrayList<Integer>(map.get(x));
					Collections.sort(sorted);
					ArrayList<String> pageList = new ArrayList<String>();
					for (Integer i : sorted) {
						pageList.add(i.toString());
					}
					
					bw.write(x + " " + String.join(", ", pageList));
					bw.newLine();
				}
				
				bw.close();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
