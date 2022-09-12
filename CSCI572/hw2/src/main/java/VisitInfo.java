/**
 * this class is the data structure used to store records for each url fetched
 */

public class VisitInfo {
    private final String url;
    private final long fileSize; //the size of the file in byte
    private final int numOfOutlink; // number of out links
    private final String contentType; // content-type

    public VisitInfo(String url, long fileSize, int numOfOutlink, String contentType) {
        this.url = url;
        this.fileSize = fileSize;
        this.numOfOutlink = numOfOutlink;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getNumOfOutlink() {
        return numOfOutlink;
    }

    public String getContentType() {
        return contentType;
    }

}