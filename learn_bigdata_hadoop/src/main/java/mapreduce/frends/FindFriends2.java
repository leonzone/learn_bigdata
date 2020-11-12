package mapreduce.frends;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author: reiserx
 * Date:2020/8/20
 * Des:
 */
public class FindFriends2 {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();

        Job job = Job.getInstance(configuration);
        job.setJarByClass(FindFriends1.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        Path outputPath = new Path(args[1]);
        FileSystem fs = FileSystem.get(configuration);
        if (fs.exists(outputPath)) {
            fs.delete(outputPath, true);
        }

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, outputPath);


        // 4 设置map输出
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

//        job.setNumReduceTasks(3);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] token = line.split("\t");
            String id = token[0];
            String[] firends = token[1].split("-");
            Arrays.sort(firends);
            
            for (int i = 0; i < firends.length - 1; i++) {
                for (int j = 1; j < firends.length; j++) {
                    context.write(new Text(firends[i] + "-" + firends[j]), new Text(id));
                }
            }


        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuilder friends = new StringBuilder();
            for (Text i : values) {
                friends.append(i).append(" ");
            }
            context.write(key, new Text(friends.toString()));

        }
    }

}
