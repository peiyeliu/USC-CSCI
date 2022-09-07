import java.util.List;

/**
 * Test quantization implementation
 */
public class Tester {


    static ImageDisplay imageDisplay = new ImageDisplay();

    private static void testLogQuantization(){
        int errorCount = 0;
        for(int i = 1; i <= 8; i++){
            for(int j = 0; j <= 255; j++){
                List<Integer> res = imageDisplay.getQuantizationIntervals(i, j);
                int expectedSize = (int) Math.pow(2, i) + 1;
                if(res.size() != expectedSize){
                    System.err.println("Failed at case: [" + i + ", " + j + "]");
                    System.err.println("Expected = " + expectedSize + " , Actual = " + res.size());
                    errorCount++;
                }
            }
        }
        if(errorCount == 0){
            System.out.println("All tests passed");
        }
    }

    public static void main(String[] args){
        testLogQuantization();
    }
}
