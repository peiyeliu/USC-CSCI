import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class InvertedIndexBigrams {
    /**
     * mapper class
     */
    public static class BigramCountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private static final String regex = "[^a-zA-Z\t]+";
        private Text bigram = new Text();
        private LongWritable docId = new LongWritable();
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String initStr = value.toString();
            int indexOfFirstTab = initStr.indexOf("\t");
            String docIdStr = initStr.substring(0, indexOfFirstTab);
            docId.set(Long.parseLong(docIdStr));
            String[] tokens = initStr.substring(indexOfFirstTab + 1).replaceAll(regex, " ").toLowerCase().split("\\s+");
            for(int i = 0; i < tokens.length - 1; i++){
                String str1 = tokens[i].trim();
                String str2 = tokens[i+1].trim();
                if(str1.length() == 0 || str2.length() == 0){
                    continue;
                }
                bigram.set(tokens[i].trim() + " " + tokens[i+1].trim());
                context.write(bigram, docId);
            }
        }
    }

    /**
     * reducer class
     */
    public static class BigramCountReducer extends Reducer<Text, LongWritable, Text, Text> {
        private Text outcome = new Text();

        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {
            Map<Long, Integer> countMap = new HashMap<>();

            for(LongWritable value: values){
                Long docId = value.get();
                countMap.put(docId, countMap.getOrDefault(docId, 0) + 1);
            }

            StringBuilder stringBuilder = new StringBuilder();
            for(Long docId: countMap.keySet()){
                stringBuilder.append(docId);
                stringBuilder.append(":");
                stringBuilder.append(countMap.get(docId));
                stringBuilder.append(" ");
            }
            outcome.set(stringBuilder.toString());
            context.write(key, outcome);
        }
    }

    /**
     * the main program
     * @param args must have two arguments, input path and output path
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println(Arrays.toString(args));
        if(args.length != 2){
            System.err.println("Usage: <input path> <output path>");
            return;
        }


        Job job = new Job();
        job.setJarByClass(InvertedIndexBigrams.class);
        job.setJobName("Bigram Count");

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        job.setMapperClass(BigramCountMapper.class);
        job.setReducerClass(BigramCountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.waitForCompletion(true);
    }

}
