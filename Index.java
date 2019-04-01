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
		File inputFolder = new File("C:/Users/80lascha/Desktop/input");
		File outputFolder = new File("C:/Users/80lascha/Desktop/output");
		int pageCharacters = 1000;
		
		long startTime = System.currentTimeMillis();
		
		for (File f : inputFolder.listFiles()) {
			try {
				FileReader fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				String line = null;
				int charCount = 0;
				HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
				
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						List<String> wordList = Arrays.asList(line.toLowerCase().split("\\s+"));
						for (String word : wordList) {
							charCount = charCount+word.length();
							if (map.get(word) == null) {
								map.put(word, new HashSet<String>());
							}
							
							map.get(word).add(Integer.toString((charCount/pageCharacters)+1));
						}
					}
				}
				
				br.close();
				fr.close();
				
				ArrayList<String> outList = new ArrayList<String>(map.keySet());
				Collections.sort(outList);
				
				FileWriter fw = new FileWriter(new File(outputFolder, "a_" + f.getName()));
				BufferedWriter bw = new BufferedWriter(fw);
				
				for (String x : outList) {
					ArrayList<String> pageList = new ArrayList<String>(map.get(x));
					Collections.sort(pageList);
					bw.write(x + " " + String.join(", ", pageList));
					bw.newLine();
				}
				
				bw.close();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.print(System.currentTimeMillis()-startTime);
	}
}
