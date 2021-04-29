package stolentweetsbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


public class TwitterBot {
	
	//TODO: all of the System.out.println() have to be replaced by the logger
//	final static Logger logger = LoggerFactory.getLogger(TwitterBot.class);
	
	private Twitter twitterInstance;
	
//	public void loggerCheck() {
//		System.out.println("regular print");
//		logger.info("info logger");
//	}
	
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
	
	//used to send tweet to user that mentioned the bot
	public void sendTweetWithStatus(StatusUpdate status) {
		try {
			twitterInstance.updateStatus(status);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * 
	 */
	public void getResponseStatus() {
		//TODO: need some logic so that we wont be checking for already processed mentions
		List<Status> mentions = getMentions();
		
		for(Status curStatus : mentions)
		{
//			
//			System.out.println(curStatus.getUser());
//			System.out.println(curStatus.getUser().getId());
			System.out.println("the status ID is" + curStatus.getId());
			System.out.println("the tweet text is " + curStatus.getText());
			System.out.println("this mention is from " + curStatus.getUser().getScreenName());
			System.out.println("the potential tweet thief is " + curStatus.getInReplyToScreenName());
			System.out.println();
						
			Status potentialStolenTweetStatus = retrievePotentiallyStolenTweetsStatus(curStatus);
			
			if(potentialStolenTweetStatus == null) {
				continue;
			}
			
			StringBuilder strBuilder = new StringBuilder(String.format("@%s hi, ", curStatus.getUser().getScreenName()));
			checkIfTweetWasStolen(strBuilder, potentialStolenTweetStatus);
			
			//TODO: IF TWEET WASNT STOLEN, MAYBE CHECK TO SEE IF IT WAS ALTERED SLIGHTLY?
			//Should I run multiple tasks concurrently? (Meaning do I search for slight alterations by choosing some key words 
			//and putting them together in different queries?
			
			
			Scanner input = new Scanner(System.in);
			if(!input.next().equals("yes")) {
				continue;
			}
			
			
			
			StatusUpdate responseStatus = new StatusUpdate(strBuilder.toString());
			long tweetId = curStatus.getId();
			responseStatus.setInReplyToStatusId(tweetId);
			System.out.println(responseStatus.getStatus());
			
			

			sendTweetWithStatus(responseStatus);
			
			
			System.out.println("--------------------------------------------- \n\n\n\n");
		}
	}
	
	
	
	
	
	public void checkIfTweetWasStolen(StringBuilder strBuilder, Status potentialStolenTweetStatus) {
		System.out.println("the tweet we're checking on is " + potentialStolenTweetStatus.getText());
		Query query = new Query(potentialStolenTweetStatus.getText());
		try {
			QueryResult queryResults = twitterInstance.search(query);
			int count = 0;
			
			while(true) {
				List<Status> list = queryResults.getTweets();
				
				count += list.size();
//				for(Status tweet : list)
//				{
//					count++;
//					System.out.println("the user is " + tweet.getUser());
//					System.out.println(tweet.getText());
//					System.out.println("the count is " + count);
//				}
				
				if(count >= 500)
				{
					break;
				}
				else if(queryResults.hasNext())
				{
					queryResults.nextQuery();;
				}
				else
				{
					break;
				}
			}
			
			if(count > 1 && count < 500) 
			{
				strBuilder.append("this tweet has been stolen " + count + " times");
			}
			else if(count >= 500)
			{
				strBuilder.append("this tweet has been stolen over 500 times");
			}
			else 
			{
				strBuilder.append("this tweet seems to have only been tweeted once");
			}
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
