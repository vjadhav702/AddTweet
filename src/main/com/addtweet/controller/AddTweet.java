package main.com.addtweet.controller;


import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Path("/hashTweet")
public class AddTweet {
	
	@GET
	@Path("/addtweet")
	public Response createTable(@QueryParam("hashTweet") String tweetValue) {

		String result = "Tweet Posted ..";
		
		try {
 			ConfigurationBuilder cb = new ConfigurationBuilder();
 	        cb.setDebugEnabled(true)
 	                .setOAuthConsumerKey("spOwyMAS7g9yyAmitMxXjj7Rp")
 	                .setOAuthConsumerSecret("lBowkugy26zebqP5MF67i6ZavdMZxbZrkaOQFyhdOqpJkkK20S")
 	                .setOAuthAccessToken("912657465485021184-dtcbsG9W8Kbgzj2XNicMrXeB3mXJMsD")
 	                .setOAuthAccessTokenSecret("o236OqYQTHloagpF0zWMY1b2sXBaAUQi3VMKW6sIpenj3");
 	        TwitterFactory tf = new TwitterFactory(cb.build());
 	        Twitter twitter = tf.getInstance();

 			twitter.updateStatus(tweetValue);

 			System.out.println("Successfully updated the status in Twitter.");
 		} catch (TwitterException te) {
 			te.printStackTrace();
 		}
		
		return Response.status(201).entity(result).build();
	}
}
