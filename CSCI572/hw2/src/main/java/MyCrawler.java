import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class MyCrawler extends WebCrawler {
    Set<String> FILE_CONTENTTYPE_SET = new HashSet<>(Arrays.asList("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|mp3|mp4|zip|gz|vcf|xml|mid|wav|avi|mov|m4v|mpeg|ram))$");
    private LocalData localData;


    public MyCrawler() {
        this.localData = new LocalData();
    }


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String urlStr = url.getURL().toLowerCase().replace(",", "_");
        StringBuilder urlBuilder = new StringBuilder(urlStr);
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '/') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        if (urlBuilder.indexOf("https://") == 0) {
            urlBuilder.delete(0, 8);
        } else if (urlBuilder.indexOf("http://") == 0) {
            urlBuilder.delete(0, 7);
        }
        if (urlBuilder.indexOf("www.") == 0) {
            urlBuilder.delete(0, 4);
        }
        String href = urlBuilder.toString();
        localData.addUrlStatus(urlStr, href.startsWith("nytimes.com"));
        return !FILTERS.matcher(href).matches() && href.startsWith("nytimes.com");
    }

    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
        String url = webUrl.getURL().toLowerCase().replace(",", "_");
        localData.addUrlCode(url, statusCode);
    }


    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL().toLowerCase().replace(",", "_");
        String contentType = page.getContentType().toLowerCase().split(";")[0];
        long fileLengthInByte = page.getContentData().length;
        ParseData parseData = page.getParseData();
        int numOfOutgoingLink = 0;
        if (parseData instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            numOfOutgoingLink = htmlParseData.getOutgoingUrls().size();
            localData.addUrlInfo(new VisitInfo(url, fileLengthInByte, numOfOutgoingLink, contentType));
        } else if (contentType.startsWith("image") || FILE_CONTENTTYPE_SET.contains(contentType)) {
            localData.addUrlInfo(new VisitInfo(url, fileLengthInByte, numOfOutgoingLink, contentType));
        }
    }


    @Override
    public Object getMyLocalData() {
        return localData;
    }
}
