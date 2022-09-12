import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.HashSet;

import java.util.Set;

/**
 * This class is used in HW5
 * The purpose is to parse all given html files in the dataset
 * All words will be extracted and stored in "big.txt" file
 */
public class BigDataGenerator {
    private static final String baseURL = "https://www.nytimes.com/";

    private static Set<String> wordSet = new HashSet<>();

    public static void main(String[] args) throws Exception{

        String folderPath = "/Users/pyl/solr-7.7.3/nytimes";
        File webPageFolder = new File(folderPath);
        File[] fileArray = webPageFolder.listFiles();

        for(File page: fileArray){
            extract(page);
        }

        File output = new File("output/big.txt");
        if(!output.exists()){
            output.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        for(String word: wordSet){
            bw.write(word + "\n");
        }

        bw.flush();
        bw.close();


    }


    private static void extract(File page) throws IOException {


        Document doc = Jsoup.parse(page, "UTF-8", baseURL);
        String[] tokens = doc.body().text().trim().replaceAll("[^a-zA-z0-9]", " ").split("\\s+");
        for(String token: tokens){
            if("".equals(token)){
                continue;
            }
            wordSet.add(token);
        }


    }
}
