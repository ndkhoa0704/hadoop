package org.myorg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;

import org.apache.log4j.Logger;

//Main class for MapReduce job
public class WordCount3 extends Configured implements Tool {

  private static final Logger LOG = Logger.getLogger(WordCount3.class);

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new WordCount3(), args);
    System.exit(res);
  }

  // Run class 
  public int run(String[] args) throws Exception {
    // Initialize job
    Job job = Job.getInstance(getConf(), "wordcount");
    for (int i = 0; i < args.length; i += 1) {
      // Detect stop_words.txt file by argument 
      if ("-skip".equals(args[i])) {
        job.getConfiguration().setBoolean("wordcount.skip.patterns", true);
        i += 1;
        job.addCacheFile(new Path(args[i]).toUri());
        // this demonstrates logging
        LOG.info("Added file to the distributed cache: " + args[i]);
      }
    }
    job.setJarByClass(this.getClass());
    // Use TextInputFormat, the default unless job.setInputFormatClass is used
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.setMapperClass(Map.class);
    job.setCombinerClass(Reduce.class);
    job.setReducerClass(Reduce.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

  // Map Phase
  public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    // Initialize input and output
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private boolean caseSensitive = false;
    private long numRecords = 0;
    private String input;
    private Set<String> patternsToSkip = new HashSet<String>();
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s*\\b\\s*");

    protected void setup(Mapper.Context context)
        throws IOException,
        InterruptedException {

      // Identify input data file and stop_words file 
      if (context.getInputSplit() instanceof FileSplit) {
        this.input = ((FileSplit) context.getInputSplit()).getPath().toString();
      } else {
        this.input = context.getInputSplit().toString();
      }
      Configuration config = context.getConfiguration();
      // Setup case sensitivity and stop words flag 
      this.caseSensitive = config.getBoolean("wordcount.case.sensitive", false);
      if (config.getBoolean("wordcount.skip.patterns", false)) {
        URI[] localPaths = context.getCacheFiles();
        parseSkipFile(localPaths[0]);
      }
    }

    //Get stop_words.txt file and parse 
    private void parseSkipFile(URI patternsURI) {
      LOG.info("Added file to the distributed cache: " + patternsURI);

      // Identify stop_words.txt path and allocate resource
      try {
        BufferedReader fis = new BufferedReader(new FileReader(new File(patternsURI.getPath()).getName()));
        String pattern;

        // Add each word in stop_words.txt file to ignore in the map phase 
        while ((pattern = fis.readLine()) != null) {
          patternsToSkip.add(pattern);
        }
      } catch (IOException ioe) {
        System.err.println("Caught exception while parsing the cached file '"
            + patternsURI + "' : " + StringUtils.stringifyException(ioe));
      }
    }

    public void map(LongWritable offset, Text lineText, Context context)
        throws IOException, InterruptedException {
      String line = lineText.toString();
      // Remove case sensitivity by convert to lowercase
      if (!caseSensitive) {
        line = line.toLowerCase();
      }
      Text currentWord = new Text();
      /*
      Read file line by line 
      Use Regex to filter non-alphanum character for each word 
      */
      for (String word : WORD_BOUNDARY.split(line)) {
        // Check if word is in stop words pattern 
        if (word.isEmpty() || patternsToSkip.contains(word)) {
          continue;
        }
        // Remove non-alphanumeric character
        if (word.matches(".*[^A-Za-z0-9].*")) {
          continue;
        }
        currentWord = new Text(word);
        context.write(currentWord, one);
      }
    }
  }

  //Reduce Phase
  public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    public void reduce(Text word, Iterable<IntWritable> counts, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      /*
      Sum up count of each word 
      Return <word,count> pair 
      */
      for (IntWritable count : counts) {
        sum += count.get();
      }
      context.write(word, new IntWritable(sum));
    }
  }
}
