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
import java.util.Stack;

public class GlobalRunner {
	
	public static final int MAX_THREADS = 5;
	private Stack<File> stack = new Stack<File>();
	private ArrayList<GlobalWorker> workerList = new ArrayList<GlobalWorker>();
	private File inputDir;
	private File outputDir;
	private int charsPerPage;
	private HashMap<String, HashMap<String, ArrayList<String>>> results = new HashMap<String, HashMap<String, ArrayList<String>>>();
	
	public GlobalRunner(File inputDirectory, File outputDirectory, int inputCharsPerPage) {
		this.inputDir = inputDirectory;
		this.outputDir = outputDirectory;
		this.charsPerPage = inputCharsPerPage;
	}
	
	public void go() {
		
		this.stack.addAll(Arrays.asList(this.inputDir.listFiles()));
		
		for (int i = 1; i <= MAX_THREADS; i++) {
			GlobalWorker worker = new GlobalWorker(this);
			this.workerList.add(worker);
			worker.start();
		}
		
		for (GlobalWorker x : this.workerList) {
			try {
				x.join();
				this.results.putAll(x.getResults());
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		ArrayList<String> fileNames = new ArrayList<String>(this.results.keySet());
		Collections.sort(fileNames);
		
		HashSet<String> wordSet = new HashSet<String>();
		
		for (String name : fileNames) {
			wordSet.addAll(this.results.get(name).keySet());
		}
		
		ArrayList<String> wordList = new ArrayList<String>(wordSet);
		Collections.sort(wordList);
		
		try {
			FileWriter fw = new FileWriter(new File(this.outputDir, "output.txt"));
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("Word, " + String.join(", ", fileNames));
			bw.newLine();
			
			for (String word : wordList) {
				ArrayList<String> line = new ArrayList<String>();
				line.add(word);
				
				
				for (String fileName : fileNames) {
					line.add(String.join(":", this.results.get(fileName).get(word)));
				}
				
				bw.write(String.join(", ", line));
			}
			
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Stack<File> getFileStack() {
		return this.stack;
	}
	
	public int getCharsPerPage() {
		return this.charsPerPage;
	}
	
	
	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		GlobalRunner runner = new GlobalRunner(new File(args[0]), new File(args[1]), Integer.parseInt(args[2]));
		runner.go();
		System.out.println(System.currentTimeMillis()-startTime);
	}
	
	
	public static class GlobalWorker extends Thread {
		
		private GlobalRunner parent;
		private HashMap<String, HashMap<String, ArrayList<String>>> results = new HashMap<String, HashMap<String, ArrayList<String>>>();
		
		public GlobalWorker(GlobalRunner inputParent) {
			this.parent = inputParent;
		}
		
		public HashMap<String, HashMap<String, ArrayList<String>>> getResults() {
			return results;
		}

		// The methods in a Stack are synchronized for thread safety
		
		public void run() {
			while (parent.getFileStack().size() > 0) {
				File file = parent.getFileStack().pop();
				this.processFile(file);
			}
		}
		
		private void processFile(File file) {
			try {
				FileReader fr = new FileReader(file);
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
								if (charCount > this.parent.getCharsPerPage()) {
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
				
				HashMap<String, ArrayList<String>> sortedMap = new HashMap<String, ArrayList<String>>();
				for (String key : map.keySet()) {
					ArrayList<Integer> intList = new ArrayList<Integer>(map.get(key));
					Collections.sort(intList);
					ArrayList<String> sorted = new ArrayList<String>();
					for (Integer i : intList) {
						sorted.add(i.toString());
					}
					sortedMap.put(key, sorted);
				}
				
				this.results.put(file.getName(), sortedMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
