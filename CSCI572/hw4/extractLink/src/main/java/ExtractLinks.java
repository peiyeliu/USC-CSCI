import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractLinks {

    private static final String baseURL = "https://www.nytimes.com/";
    private static Map<String, String> URLFileMap= new HashMap<>();
    private static Map<String, String> fileURLMap= new HashMap<>();

    private static Set<String> edges = new HashSet<>();

    public static void main(String[] args) throws Exception{
        readURLtoHTML();

        String folderPath = "/Users/pyl/Desktop/NYTIMES/nytimes";
        File webPageFolder = new File(folderPath);
        File[] fileArray = webPageFolder.listFiles();

        for(File page: fileArray){
            extract(page);
        }

        File output = new File("output/allEdges.txt");
        if(!output.exists()){
            output.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        for(String edge: edges){
            bw.write(edge);
            bw.newLine();
        }
        bw.flush();
        bw.close();


    }

    private static void readURLtoHTML(){
        File f = new File("./data/URLtoHTML_nytimes_news.csv");
        try(BufferedReader br = new BufferedReader(new FileReader("./data/URLtoHTML_nytimes_news.csv"))) {
            String line = "";
            while((line = br.readLine()) != null){
                String[] tokens = line.split(",");
                fileURLMap.put(tokens[0], tokens[1]);
                URLFileMap.put(tokens[1], tokens[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extract(File page) throws IOException {
        String html = fileURLMap.get(page.getName());
        if(html == null){
            System.err.println("Error: current file does not have corresponding html: " + page.getName());
            return;
        }

        Document doc = Jsoup.parse(page, "UTF-8", baseURL);
        Elements links = doc.select("a[href]");

        for(Element link: links){
            String outgoingLink = link.attr("abs:href").trim();
            if(URLFileMap.containsKey(outgoingLink)){
                edges.add(page.getName() + " " + URLFileMap.get(outgoingLink));
            }
        }
    }
}
