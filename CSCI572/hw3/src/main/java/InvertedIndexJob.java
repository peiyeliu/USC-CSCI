import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class InvertedIndexJob {

    /**
     *  mapper class
     */
    public static class WordCountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private Text word = new Text();
        private static final String regex = "[^a-zA-Z]+";
        private LongWritable docId = new LongWritable();
        public void map(Object key,
                        Text value,
                        Context context) throws IOException, InterruptedException {
            String initStr = value.toString();
            int indexOfFirstTab = initStr.indexOf("\t");
            String docIdStr = initStr.substring(0, indexOfFirstTab);
            docId.set(Long.parseLong(docIdStr));
            StringTokenizer tokenizer = new StringTokenizer(initStr.substring(indexOfFirstTab + 1).replaceAll(regex, " ").toLowerCase());
            while(tokenizer.hasMoreTokens()){
                word.set(tokenizer.nextToken());
                context.write(word, docId);
            }
        }
    }

    /**
     * reducer class
     */
    public static class WordCountReducer extends Reducer<Text, LongWritable, Text, Text> {
        private Text outcome = new Text();

        public void reduce(Text key,
                           Iterable<LongWritable> values,
                           Context context)
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
     * @param args must have two arguments: input path and output path
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if(args.length != 2){
            System.err.println("Usage: <input path> <output path>");
            return;
        }


        Job job = new Job();
        job.setJarByClass(InvertedIndexJob.class);
        job.setJobName("Word Count");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.waitForCompletion(true);

    }
}
