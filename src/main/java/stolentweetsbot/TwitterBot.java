package stolentweetsbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class TwitterBot {
	
//	final static Logger logger = LoggerFactory.getLogger(TwitterBot.class);
	
	private Twitter twitterInstance;
	
	public TwitterBot() {
		makeTwitterConection();
	}
	
	public void sendTweet(String tweetContent) {
		try {
			twitterInstance.updateStatus(tweetContent);
			System.out.println("updated tweet to " + tweetContent);
		} 
		catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void getResponseStatus() {
		//TODO: need some logic so that we wont be checking for already processed mentions
		List<Status> mentions = getMentions();
		
		for(Status curStatus : mentions)
		{
			
			System.out.println(curStatus.getUser());
			System.out.println(curStatus.getUser().getId());
			System.out.println("this mention is from " + curStatus.getUser().getScreenName());
			System.out.println("in reply to " + curStatus.getInReplyToScreenName());
						
			Status potentialStolenTweetStatus = retrievePotentiallyStolenTweetsStatus(curStatus);
			
			if(potentialStolenTweetStatus == null) {
				continue;
			}
			
			StatusUpdate responseStatus = new StatusUpdate(String.format("@%s hi", curStatus.getUser().getScreenName()));
//			long userId = curStatus.getUser().getId();
//			responseStatus.setInReplyToStatusId(userId);
			System.out.println(responseStatus.getStatus());
			
			try {
				twitterInstance.updateStatus(responseStatus);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
			
			System.out.println("--------------------------------------------- \n\n\n\n");
		}
	}
	
	
	
	public List<Status> getMentions() {
		try 
		{
			return twitterInstance.getMentionsTimeline();		
		} 
		catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	//this gets the potentially stolen tweet 
	public Status retrievePotentiallyStolenTweetsStatus(Status status) {
		long potentiallyStolenTweetId = status.getInReplyToStatusId();
		
		try {
			return twitterInstance.showStatus(potentiallyStolenTweetId);
			
		} catch (TwitterException e) {
			System.out.println("this tweet isn't responding to any other tweet, we won't do anything");
//			logger.warn("this is just a regular mention, we won't do anything");
		}
		return null;
	}
	

	
	
	public void makeTwitterConection() {
		Properties keys = getAPIProperties();
		ConfigurationBuilder cbBuilder = new ConfigurationBuilder()
				.setOAuthConsumerKey(keys.getProperty("twitter.consumerKey"))
				.setOAuthConsumerSecret(keys.getProperty("twitter.consumerSecret"))
				.setOAuthAccessToken(keys.getProperty("twitter.accessToken"))
				.setOAuthAccessTokenSecret(keys.getProperty("twitter.accessTokenSecret"));
		
		twitterInstance = new TwitterFactory(cbBuilder.build()).getInstance();
		
	}
	
	public Properties getAPIProperties() {
		Properties systemProps = new Properties();
		try {
			FileInputStream input = new FileInputStream("secret.properties");
			systemProps.load(input);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return systemProps;
	}

}
