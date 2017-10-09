package main.com.addtweet.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.json.JSONArray;
import org.json.JSONObject;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

/**
 * Servlet implementation class GetSentiments
 */
@WebServlet("/GetSentiments")
public class GetSentiments extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	DoccatModel model;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSentiments() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String result = "Tweet Posted ..";
		int positive = 0;
	    int negative = 0;
		try {
			System.out.println("inside do get ..... ");
			
			String line = "";
			GetSentiments twitterCategorizer = new GetSentiments();
	        twitterCategorizer.trainModel();
	
	          String topic = "dbserver1.public.data";
		      System.out.println(" connecting to >>>> "+ getConnectionUrl());
		      Properties props = new Properties();
		      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getConnectionUrl());
		      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		      props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		      props.put(ConsumerConfig.GROUP_ID_CONFIG, getGrouId());
		      props.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple");
		      KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		      
		      consumer.subscribe(Arrays.asList(topic));
		      
		      System.out.println("Subscribed to topic " + topic);
		      int i = 0;
		      System.out.println("After getting the topic >>>>>>>>>>>>> ");
		      try {
		    	  int cnt = 10;
		    	  while (cnt > 0) {
		 	        ConsumerRecords<String, String> records = consumer.poll(100);
		 	        System.out.println("Records found : " + records.count());
	 	            for (ConsumerRecord<String, String> record : records) {
	 	            	String tweet = getTweetValue(record.value());
	 	            	int result1 = twitterCategorizer.classifyNewTweet(tweet);
		 	            if (result1 == 1) {
		 	                positive++;
		 	            } else {
		 	                negative++;
		 	            }
	 	            }
	 	            cnt--;
		 	      }
	        }catch(WakeupException ex){
	            System.out.println("Exception caught " + ex.getMessage());
	        }finally{
	      	  consumer.close();
	            System.out.println("After closing KafkaConsumer");
	        }

	        result = positive+":"+negative;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(" Exception >>>> " + e);
			result = "Error";
		}
		
		response.getWriter().write(result);
	}
	
public static String getTweetValue(String value) {
		
		String tweet = "";
		try {
			JSONObject obj = new JSONObject(value);
			JSONObject obj1 = obj.getJSONObject("payload");
			JSONObject obj2 = obj1.getJSONObject("after");
			String user = obj2.getString("user_handle");
			tweet = obj2.getString("tweet_data");
		} catch(Exception e) {
			System.out.println("Error in parsing the value ..... ");
		}
		return tweet;
	}
	
	public static String getConnectionUrl() {
		
		String env = System.getenv("VCAP_SERVICES");
		String url = "";
		if (env != null) {
			JSONObject obj = new JSONObject(env);
			JSONArray arr = obj.getJSONArray("DistributedServiceBundle");

			String ip = arr.getJSONObject(0).getJSONObject("credentials").getString("kafka_ip");
			int port = arr.getJSONObject(0).getJSONObject("credentials").getInt("kafka_port");
			port = 9092;
			
			url = ip+":"+port;
			
		}
		return url;
	}
	
	public static String getGrouId() {
		
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
	public void trainModel() {
        InputStream dataIn = null;
        try {
        	
        	//dataIn = Class.class.getResource("").getPath();
        	//System.out.println("");
            //dataIn = new FileInputStream(Class.class.getResource("/tweets.txt").getPath());
            //dataIn = new FileInputStream(this.getClass().getResource( "/tweets.txt" ).getPath());
        	String exampleString = "1	Watching a nice movie\r\n" + 
        			"0	The painting is ugly, will return it tomorrow...\r\n" + 
        			"1	One of the best soccer games, worth seeing it\r\n" + 
        			"1	Very tasty, not only for vegetarians\r\n" + 
        			"1	Super party!\r\n" + 
        			"0	Too early to travel..need a coffee\r\n" + 
        			"0	Damn..the train is late again...\r\n" + 
        			"0	Bad news, my flight just got cancelled.\r\n" + 
        			"1	Happy birthday mr. president\r\n" + 
        			"1	Just watch it. Respect.\r\n" + 
        			"1	Wonderful sunset.\r\n" + 
        			"1	Bravo, first title in 2014!\r\n" + 
        			"0	Had a bad evening, need urgently a beer.\r\n" + 
        			"0	I put on weight again\r\n" + 
        			"1	On today's show we met Angela, a woman with an amazing story\r\n" + 
        			"1	I fell in love again\r\n" + 
        			"0	I lost my keys\r\n" + 
        			"1	On a trip to Iceland\r\n" + 
        			"1	Happy in Berlin\r\n" + 
        			"0	I hate Mondays\r\n" + 
        			"1	Love the new book I reveived for Christmas\r\n" + 
        			"0	He killed our good mood\r\n" + 
        			"1	I am in good spirits again\r\n" + 
        			"1	This guy creates the most awesome pics ever \r\n" + 
        			"0	The dark side of a selfie.\r\n" + 
        			"1	Cool! John is back!\r\n" + 
        			"1	Many rooms and many hopes for new residents\r\n" + 
        			"0	False hopes for the people attending the meeting\r\n" + 
        			"1	I set my new year's resolution\r\n" + 
        			"0	The ugliest car ever!\r\n" + 
        			"0	Feeling bored\r\n" + 
        			"0	Need urgently a pause\r\n" + 
        			"1	Nice to see Ana made it\r\n" + 
        			"1	My dream came true\r\n" + 
        			"0	I didn't see that one coming\r\n" + 
        			"0	Sorry mate, there is no more room for you\r\n" + 
        			"0	Who could have possibly done this?\r\n" + 
        			"1	I won the challenge\r\n" + 
        			"0	I feel bad for what I did		\r\n" + 
        			"1	I had a great time tonight\r\n" + 
        			"1	It was a lot of fun\r\n" + 
        			"1	Thank you Molly making this possible\r\n" + 
        			"0	I just did a big mistake\r\n" + 
        			"1	I love it!!\r\n" + 
        			"0	I never loved so hard in my life\r\n" + 
        			"0	I hate you Mike!!\r\n" + 
        			"0	I hate to say goodbye\r\n" + 
        			"1	Lovely!\r\n" + 
        			"1	Like and share if you feel the same\r\n" + 
        			"0	Never try this at home\r\n" + 
        			"0	Don't spoil it!\r\n" + 
        			"1	I love rock and roll\r\n" + 
        			"0	The more I hear you, the more annoyed I get\r\n" + 
        			"1	Finnaly passed my exam!\r\n" + 
        			"1	Lovely kittens\r\n" + 
        			"0	I just lost my appetite\r\n" + 
        			"0	Sad end for this movie\r\n" + 
        			"0	Lonely, I am so lonely\r\n" + 
        			"1	Beautiful morning\r\n" + 
        			"1	She is amazing\r\n" + 
        			"1	Enjoying some time with my friends\r\n" + 
        			"1	Special thanks to Marty\r\n" + 
        			"1	Thanks God I left on time\r\n" + 
        			"1	Greateful for a wonderful meal\r\n" + 
        			"1	So happy to be home\r\n" + 
        			"0	Hate to wait on a long queue		\r\n" + 
        			"0	No cab available\r\n" + 
        			"0	Electricity outage, this is a nightmare\r\n" + 
        			"0	Nobody to ask about directions\r\n" + 
        			"1	Great game!\r\n" + 
        			"1	Nice trip\r\n" + 
        			"1	I just received a pretty flower\r\n" + 
        			"1	Excellent idea\r\n" + 
        			"1	Got a new watch. Feeling happy\r\n" + 
        			"0	I feel sick\r\n" + 
        			"0	I am very tired\r\n" + 
        			"1	Such a good taste \r\n" + 
        			"0	Such a bad taste\r\n" + 
        			"1	Enjoying brunch\r\n" + 
        			"0	I don't recommend this restaurant\r\n" + 
        			"1	Thank you mom for supporting me\r\n" + 
        			"0	I will never ever call you again\r\n" + 
        			"0	I just got kicked out of the contest\r\n" + 
        			"1	Smiling\r\n" + 
        			"0	Big pain to see my team loosing\r\n" + 
        			"0	Bitter defeat tonight\r\n" + 
        			"0	My bike was stollen\r\n" + 
        			"1	Great to see you!\r\n" + 
        			"0	I lost every hope for seeing him again\r\n" + 
        			"1	Nice dress!\r\n" + 
        			"1	Stop wasting my time\r\n" + 
        			"1	I have a great idea\r\n" + 
        			"1	Excited to go to the pub\r\n" + 
        			"1	Feeling proud\r\n" + 
        			"1	Cute bunnies\r\n" + 
        			"0	Cold winter ahead\r\n" + 
        			"0	Hopless struggle..\r\n" + 
        			"0	Ugly hat\r\n" + 
        			"1	Big hug and lots of love\r\n" + 
        			"1	I hope you have a wonderful celebration";
        	dataIn = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8.name()));
            
            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);
            // Specifies the minimum number of times a feature must be seen
            int cutoff = 2;
            int trainingIterations = 30;
            model = DocumentCategorizerME.train("en", sampleStream, cutoff,
                    trainingIterations);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int classifyNewTweet(String tweet) throws IOException {
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);

        System.out.print("-----------------------------------------------------\nTWEET :" + tweet + " ===> ");
        if (category.equalsIgnoreCase("1")) {
            System.out.println(" POSITIVE ");
            return 1;
        } else {
            System.out.println(" NEGATIVE ");
            return 0;
        }

    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
