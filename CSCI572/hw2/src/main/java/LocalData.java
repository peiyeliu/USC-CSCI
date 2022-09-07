import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



/**
 * The LocalData is used in MyCrawler objects to store the url infos during crawling
 */

public class LocalData {
    private List<VisitInfo> urlInfoList; // this list contains infos for each url extracted
    private List<String> urlCode; // url with its HTTP status code
    private List<String> urlStatus; // url with its status("OK" or "N_OK"), separated by a comma

    public LocalData() {
        this.urlInfoList = new ArrayList<>();
        this.urlStatus = new ArrayList<>();
        this.urlCode = new ArrayList<>();
    }

    /**
     * add infos for one url visited
     *
     * @param visitInfo
     */
    public void addUrlInfo(VisitInfo visitInfo) {
        urlInfoList.add(visitInfo);
    }

    /**
     * @param url
     * @param shouldVisit
     */
    public void addUrlStatus(String url, boolean shouldVisit) {
        urlStatus.add(url + "," + (shouldVisit ? "OK" : "N_OK"));
    }

    public void addUrlCode(String url, int code){
        urlCode.add(url + "," + code);
    }


    /**
     * merge localdata from all crawler threads into one localdata record
     *
     * @param localDataList
     * @return
     */
    public static LocalData mergeIntoOne(List<Object> localDataList) {
        if (localDataList == null || localDataList.size() == 0) {
            throw new IllegalArgumentException();
        }
        LocalData merged = new LocalData();
        for (Object obj : localDataList) {
            LocalData localData = (LocalData) obj;
            merged.urlInfoList.addAll(localData.urlInfoList);
            merged.urlStatus.addAll(localData.urlStatus);
            merged.urlCode.addAll(localData.urlCode);
        }
        return merged;
    }

    public List<VisitInfo> getUrlInfoList() {
        return urlInfoList;
    }

    public List<String> getUrlStatus() {
        return urlStatus;
    }

    public List<String> getUrlCode() {
        return urlCode;
    }
}
