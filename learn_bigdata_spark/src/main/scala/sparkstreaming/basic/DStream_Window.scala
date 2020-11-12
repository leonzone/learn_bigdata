package sparkstreaming.basic

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * Window算子
 * 应用：实时统计单词出现大次数
 *
 */
object DStream_Window {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //1.获取环境变量
    val conf = new SparkConf().setMaster("local[2]").setAppName("WorkCountStreaming")
    val ssc = new StreamingContext(conf, Seconds(2))
    //2.获取数据
    val lines = ssc.socketTextStream("localhost", 8888)

    //3.处理数据
    val words = lines.flatMap(_.split(","))

    val pairs = words.map(t => (t, 1))

    //    val workCount = pairs.reduceByKey(_ + _)
    /**
     * windowDuration
     * slideDuration
     * 每隔 2 秒钟计算一下，最近 4 秒钟单词出现的次数
     */
    val workCount = pairs.reduceByKeyAndWindow((x: Int, y: Int) => x + y, Seconds(4), Seconds(2))
    //4.输出数据
    workCount.print()

    //5.启动任务
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }

}
