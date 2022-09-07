import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to write crawler data into files
 */

public class LocalDataDumper {
    private LocalData localData;

    public LocalDataDumper(LocalData localData) {
        this.localData = localData;
    }

    /**
     * write all 3 CSV files
     *
     * @param newsSite the news site used: "nytimes"
     */
    public void writeCSVFiles(String newsSite) {
        try {
            writeFetchCSV(newsSite);
            writeVisitCSV(newsSite);
            writeUrlCSV(newsSite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * write the txt statistic files
     *
     * @param fileName     the file name
     * @param studentName  student name
     * @param uscID        uscID
     * @param siteCrawled  the root site crawled
     * @param numOfThreads number of threads used
     */
    public void writeStats(String fileName, String studentName, String uscID,
                           String siteCrawled, int numOfThreads) {
        try (BufferedWriter bf = new BufferedWriter(new FileWriter(new File(fileName), true))) {
            bf.write("Name: " + studentName + "\n");
            bf.write("USC ID: " + uscID + "\n");
            bf.write("News site crawled: " + siteCrawled + "\n");
            bf.write("Number of threads: " + numOfThreads + "\n");

            System.out.println("HTTP Code entries: " + localData.getUrlCode().size());
            System.out.println("File info entries: " + localData.getUrlInfoList().size());
            System.out.println("Visit URL entries: " + localData.getUrlStatus().size());

            getFetchStats(bf);
            getURLStats(bf);
            getStatusCodeStats(bf);
            getFileSizeStats(bf);
            getContentTypeStats(bf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the number of urls within the root website
     */
    private int getURLWithinRootWebsite(Set<String> uniqueUrlSet)  {
        String header = "nytimes.com";
        int count = 0;
        for (String urlOriginal: uniqueUrlSet) {
            String url = urlOriginal.toLowerCase().
                    replace("https://www.", "").
                    replace("http://www.", "");
            if (url.startsWith(header)) {
                count++;
            }
        }
        return count;
    }

    private void getFetchStats(BufferedWriter writer) throws IOException{
        int totalFetches = 0;
        int successFetches = 0;
        for(String infoStr: localData.getUrlCode()){
            String[] info = infoStr.split(",");
            totalFetches++;
            if("200".equals(info[1])){
                successFetches++;
            }
        }

        writer.write("\n");
        writer.write("Fetch Statistics\n");
        writer.write("================\n");
        writer.write("# fetches attempted:" + totalFetches + "\n");
        writer.write("# fetches succeeded:" + successFetches + "\n");
        writer.write("# fetches failed or aborted:" + (totalFetches - successFetches) + "\n");
    }


    /**
     * write urls stats into the txt file
     *
     * @param writer
     * @throws IOException
     */
    private void getURLStats(BufferedWriter writer) throws IOException {
        int totalURL = this.localData.getUrlStatus().size();
        Set<String> uniqueUrlSet = new HashSet<>();
        for (String str : this.localData.getUrlStatus()) {
            String[] urlAndStatus = str.split(",");
            uniqueUrlSet.add(urlAndStatus[0]);
        }
        int uniqueURL = uniqueUrlSet.size();
        int URLinRoot = getURLWithinRootWebsite(uniqueUrlSet);

        writer.write("\n");
        writer.write("Outgoing URLs\n");
        writer.write("================\n");
        writer.write("Total URLs extracted: " + totalURL + "\n");
        writer.write("# unique URLs extracted: " + uniqueURL + "\n");
        writer.write("# unique URLs within News site: " + URLinRoot + "\n");
        writer.write("# unique URLs outside News site: " + (uniqueURL - URLinRoot) + "\n");
    }

    /**
     * write the status stats into the txt file
     *
     * @param writer
     * @throws IOException
     */
    private void getStatusCodeStats(BufferedWriter writer) throws IOException {
        TreeMap<Integer, Integer> map = new TreeMap<>();

        for(String str: localData.getUrlCode()){
            String[] codeInfo = str.split(",");
            int code = Integer.parseInt(codeInfo[1]);
            map.put(code, map.getOrDefault(code, 0) + 1);
        }

        writer.write("\n");
        writer.write("Status Codes:\n");
        writer.write("=============\n");
        for (int code : map.keySet()) {
            writer.write(code + ": " + map.get(code) + "\n");
        }
    }

    /**
     * write file size stats into the txt file
     *
     * @param writer
     * @throws IOException
     */
    private void getFileSizeStats(BufferedWriter writer) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        map.put("< 1KB:", 0);
        map.put("1KB ~ < 10KB:", 0);
        map.put("10KB ~ < 100KB:", 0);
        map.put("100KB ~ < 1MB:", 0);
        map.put(">= 1MB:", 0);
        for (VisitInfo visitInfo : this.localData.getUrlInfoList()) {

            long size = visitInfo.getFileSize();
            if (size < 1024) {
                map.put("< 1KB:", map.get("< 1KB:") + 1);
            } else if (size < 1024 * 10) {
                map.put("1KB ~ < 10KB:", map.get("1KB ~ < 10KB:") + 1);
            } else if (size < 1024 * 100) {
                map.put("10KB ~ < 100KB:", map.get("10KB ~ < 100KB:") + 1);
            } else if (size < 1048576) {
                map.put("100KB ~ < 1MB:", map.get("100KB ~ < 1MB:") + 1);
            } else {
                map.put(">= 1MB:", map.get(">= 1MB:") + 1);
            }
        }
        writer.write("\n");
        writer.write("File Sizes:\n");
        writer.write("===========\n");
        writer.write("< 1KB: " + map.get("< 1KB:") + "\n");
        writer.write("1KB ~ < 10KB: " + map.get("1KB ~ < 10KB:") + "\n");
        writer.write("10KB ~ < 100KB: " + map.get("10KB ~ < 100KB:") + "\n");
        writer.write("100KB ~ < 1MB: " + map.get("100KB ~ < 1MB:") + "\n");
        writer.write(">= 1MB: " + map.get(">= 1MB:") + "\n");
    }

    /**
     * write the content-type stats into the txt file
     *
     * @param writer
     * @throws IOException
     */
    private void getContentTypeStats(BufferedWriter writer) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        for (VisitInfo visitInfo : this.localData.getUrlInfoList()) {
            String contentType = visitInfo.getContentType();
            map.put(contentType, map.getOrDefault(contentType, 0) + 1);
        }
        writer.write("\n");
        writer.write("Content Types:\n");
        writer.write("==============\n");

        for (String contentType : map.keySet()) {
            writer.write(contentType + ": " + map.get(contentType) + "\n");
        }
    }

    /**
     * write the fetch csv
     *
     * @param newsSite
     * @throws IOException
     */
    private void writeFetchCSV(String newsSite) throws IOException {
        File file = new File("fetch_" + newsSite + ".csv");
        BufferedWriter bf = new BufferedWriter(new FileWriter(file, true));
        bf.write("URL,Status Code\n");
        for(String str: localData.getUrlCode()){
            bf.write(str + "\n");
        }
        bf.close();
    }

    /**
     * write the visit csv file
     *
     * @param newsSite
     * @throws IOException
     */
    private void writeVisitCSV(String newsSite) throws IOException {
        File file = new File("visit_" + newsSite + ".csv");
        BufferedWriter bf = new BufferedWriter(new FileWriter(file, true));
        bf.append("URL,File Size,Number of Outlink,Content Type\n");
        for (VisitInfo visitInfo : this.localData.getUrlInfoList()) {
            bf.append(visitInfo.getUrl() + "," + visitInfo.getFileSize()
                    + "," + visitInfo.getNumOfOutlink() + "," + visitInfo.getContentType() + "\n");
        }
        bf.close();
    }

    /**
     * write the url csv file
     *
     * @param newsSite
     * @throws IOException
     */
    private void writeUrlCSV(String newsSite) throws IOException {
        File file = new File("urls_" + newsSite + ".csv");
        BufferedWriter bf = new BufferedWriter(new FileWriter(file, true));
        List<String> urlStatus = localData.getUrlStatus();
        bf.append("URL,Visit Status\n");
        for (String str : urlStatus) {
            bf.append(str + "\n");
        }
        bf.close();
    }
}
