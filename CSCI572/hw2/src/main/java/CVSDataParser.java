import java.io.*;
import java.util.*;

/**
 * This class is used to validate our crawl report by parsing these 3 CSV files
 */
public class CVSDataParser {
    private static void getFetchStats(){
        try (BufferedReader bf = new BufferedReader(new FileReader("fetch_nytimes.csv"))) {
            int total = 0;
            int succeeded = 0;
            TreeMap<Integer, Integer> statusCount = new TreeMap<>();
            String line;
            while((line = bf.readLine()) != null){
                String[] values = line.split(",");
                if("URL".equals(values[0])){
                    continue;
                }
                total++;
                if("200".equals(values[1])){
                    succeeded++;
                }
                int status = Integer.parseInt(values[1]);
                statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
            }
            System.out.print("\n");
            System.out.print("Fetch Statistics\n");
            System.out.print("================\n");
            System.out.print("# fetches attempted:" + total + "\n");
            System.out.print("# fetches succeeded:" + succeeded + "\n");
            System.out.print("# fetches failed or aborted:" + (total - succeeded) + "\n");

            System.out.print("\n");
            System.out.print("Status Codes\n");
            System.out.print("============\n");
            for(int code: statusCount.keySet()){
                System.out.println(code+": " + statusCount.get(code));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getOutGoingURLStats(){
        try(BufferedReader bf = new BufferedReader(new FileReader("urls_nytimes.csv"))){
            int totalURL = 0;
            int uniqueURL = 0;
            int URLinRoot = 0;
            Set<String> uniqueURLSet = new HashSet<>();
            String line;
            while((line = bf.readLine()) != null){
                String[] values = line.split(",");
                if("URL".equals(values[0])){
                    continue;
                }
                totalURL++;
                if(!uniqueURLSet.contains(values[0])){
                    uniqueURLSet.add(values[0]);
                    uniqueURL++;
                    String urlTrimmed = values[0].toLowerCase().
                            replace("https://www.", "").
                            replace("http://www.", "");
                    if(urlTrimmed.startsWith("nytimes.com")){
                        URLinRoot++;
                    }
                }
            }
            System.out.print("\n");
            System.out.print("Outgoing URLs\n");
            System.out.print("================\n");
            System.out.print("Total URLs extracted: " + totalURL + "\n");
            System.out.print("# unique URLs extracted: " + uniqueURL + "\n");
            System.out.print("# unique URLs within News site: " + URLinRoot + "\n");
            System.out.print("# unique URLs outside News site: " + (uniqueURL - URLinRoot) + "\n");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getFileStats(){
        Map<String, Integer> fileMap = new HashMap<>();
        fileMap.put("< 1KB:", 0);
        fileMap.put("1KB ~ < 10KB:", 0);
        fileMap.put("10KB ~ < 100KB:", 0);
        fileMap.put("100KB ~ < 1MB:", 0);
        fileMap.put(">= 1MB:", 0);

        Map<String, Integer> contentMap = new HashMap<>();

        try(BufferedReader bf = new BufferedReader(new FileReader("visit_nytimes.csv"))){
            String line;
            while((line = bf.readLine())!= null){
                String[] info = line.split(",");
                if("URL".equals(info[0])){
                    continue;
                }
                long size = Long.parseLong(info[1]);
                String contentType = info[3];
                if (size < 1024) {
                    fileMap.put("< 1KB:", fileMap.get("< 1KB:") + 1);
                } else if (size < 1024 * 10) {
                    fileMap.put("1KB ~ < 10KB:", fileMap.get("1KB ~ < 10KB:") + 1);
                } else if (size < 1024 * 100) {
                    fileMap.put("10KB ~ < 100KB:", fileMap.get("10KB ~ < 100KB:") + 1);
                } else if (size < 1048576) {
                    fileMap.put("100KB ~ < 1MB:", fileMap.get("100KB ~ < 1MB:") + 1);
                } else {
                    fileMap.put(">= 1MB:", fileMap.get(">= 1MB:") + 1);
                }
                contentMap.put(contentType, contentMap.getOrDefault(contentType, 0) + 1);
            }


            System.out.print("\n");
            System.out.print("File Sizes:\n");
            System.out.print("===========\n");
            System.out.print("< 1KB: " + fileMap.get("< 1KB:") + "\n");
            System.out.print("1KB ~ < 10KB: " + fileMap.get("1KB ~ < 10KB:") + "\n");
            System.out.print("10KB ~ < 100KB: " + fileMap.get("10KB ~ < 100KB:") + "\n");
            System.out.print("100KB ~ < 1MB: " + fileMap.get("100KB ~ < 1MB:") + "\n");
            System.out.print(">= 1MB: " + fileMap.get(">= 1MB:") + "\n");

            System.out.print("\n");
            System.out.print("Content Types:\n");
            System.out.print("==============\n");
            for(String key: contentMap.keySet()){
                System.out.println(key + ": " + contentMap.get(key));
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        getFetchStats();
        getOutGoingURLStats();
        getFileStats();
    }
}
