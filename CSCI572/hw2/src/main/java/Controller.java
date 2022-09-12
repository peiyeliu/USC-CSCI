import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "src/main/crawlresult";
        int numberOfCrawlers = 16;
        CrawlConfig config = new CrawlConfig();

        //HW2 required parameters
        config.setMaxPagesToFetch(20000);
        config.setMaxDepthOfCrawling(16);
        config.setPolitenessDelay(101);
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setIncludeBinaryContentInCrawling(true);
        config.setMaxDownloadSize(10000000);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://www.nytimes.com");
        controller.start(MyCrawler.class, numberOfCrawlers);
        List<Object> crawlData = controller.getCrawlersLocalData();

        if (crawlData == null || crawlData.size() == 0) {
            logger.error("Failed to retrieve data!");
            return;
        }
        LocalData data = LocalData.mergeIntoOne(crawlData);
        LocalDataDumper localDataDumper = new LocalDataDumper(data);
        localDataDumper.writeStats("CrawlReport_nytimes.txt",
                "Peiye Liu", "5961770016",
                "https://www.nytimes.com", numberOfCrawlers);
        localDataDumper.writeCSVFiles("nytimes");
    }


}
