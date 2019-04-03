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

public class Index {
	public static void main(String args[]) {
		File inputFolder = new File(args[0]);
		File outputFolder = new File(args[1]);
		int pageCharacters = Integer.parseInt(args[2]);
		
		long startTime = System.currentTimeMillis();
		
		for (File f : inputFolder.listFiles()) {
			try {
				FileReader fr = new FileReader(f);
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
								if (charCount > pageCharacters) {
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
				
				FileWriter fw = new FileWriter(new File(outputFolder, f.getName().replace(".txt", "_output.txt")));
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
		
		System.out.println(System.currentTimeMillis()-startTime);
	}
}