#!/usr/bin/env python

#Imports required for App
from __future__ import print_function
import calendar
import sys
import json
import os.path
import tweepy
import codecs

#Global variables required through out the app
#Access Keys and tokens
consumer_key = None
consumer_secret = None
access_token = None
access_token_secret = None
tweepyAPI = None
tlist = []
months = {v: k for k,v in enumerate(calendar.month_abbr)}

# Setup various access keys and tokens from the user given input file 
def setupAuthKeys(authFile):
	global consumer_key, consumer_secret, access_token, access_token_secret
	if os.path.isfile(authFile) and os.access(authFile,os.R_OK):
		tokens = open(authFile, 'r').readlines()
		consumer_key = tokens[0].strip()
		consumer_secret = tokens[1].strip()
		access_token = tokens[2].strip()
		access_token_secret = tokens[3].strip()
	else:
		print("Auth file is missing or is not readable,exiting...")
		exit(1)

def setupTweepyAPI():
	global consumer_key, consumer_secret, access_token, access_token_secret, tweepyAPI
	auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
	if auth:
		auth.set_access_token(access_token, access_token_secret)
		tweepyAPI = tweepy.API(auth,parser=tweepy.parsers.JSONParser())
	else:
		print("Authentication setup failed, exiting...")
		exit(1)



def getTweets(queryString, fromDate, toDate):
	global tweepyAPI
	tweetResult = []
	
	# Querying for English tweets
	tweets = tweepyAPI.search(q=queryString,include_entities=True,lang='en', geocode= "40,-73,200km", since='2016-03-'+fromDate,until="2016-03-"+toDate,count=100)
	tweetResult += tweets['statuses']

	# Querying for german tweets
	#Berlin- 52.533028,13.388744,20km
	##tweets = tweepyAPI.search(q=queryString,include_entities=True, lang='de', since='2015-09-'+fromDate,until='2015-09-'+toDate,count=100)#,geocode="51.426129,10.267352,1500km")
	#tweetResult += tweets['statuses']

	# Querying for Russian tweets
	#Moscow - 55.601328,38.034511,500km
	#tweets = tweepyAPI.search(q=queryString,include_entities=True,lang='ru', since='2015-09-'+fromDate,until='2015-09-'+toDate,count=100)#,geocode="60.477265, 92.614585,1500km")
	#tweetResult += tweets['statuses']
	print(tweetResult)
	return tweetResult

# Extract hashtags out of tweet
def processHashtags(tweet):
	hashtags = []
	if tweet.get('entities'):
		if tweet['entities'].get('hashtags'):
			tweet = tweet['entities']['hashtags']
			if tweet and len(tweet) > 0:
				for tag in tweet:
					hashtags.append(tag['text'])
	return hashtags

# Extract Urls out of tweet
def processUrls(tweet):
	urls = []
	if tweet.get('entities'):
		if tweet['entities'].get('urls'):
			tweet = tweet['entities']['urls']
			if tweet and len(tweet) > 0:
				for url in tweet:
					urls.append(url['expanded_url'])
	return urls


def processDate(timeStamp):
	#   
	arr = timeStamp.split()
	year = arr[5]
	month = '0'+str(months[arr[1]])
	day = arr[2]
	dat = '-'.join([y,m,d])
	tim = arr[3].split('.')[0]
	return (dat+'T'+tim+'Z')


#Process given tweet and extract all info
def processTweet(tweet):
	global tlist
	textKeyName = 'text_'
   	twt = {}
	twt['id'] = str(tweet['id'])
	twt['lang'] = tweet['lang']
	textKeyName += twt['lang']
	twt[textKeyName] = tweet['text'].encode('utf-8')
	twt['tweet_hashtags'] = processHashtags(tweet)
	twt['tweet_urls'] = processUrls(tweet)
	twt['created_at'] = processDate(tweet['created_at'])
   	tlist.append(twt)


def writeTweetsToFile(queryString, fromDate, toDate, tweets):
	global tlist
	outFileName = '_'.join(["tweets", queryString, fromDate, toDate])
	f = codecs.open(outFileName+".json",'w','utf-8')
	f.write('[')
	tlen = len(tlist)-1
	for tweet in tlist:
		f.write(json.dumps(tweet))
		if(tlist.index(tweet) != tlen):
			f.write(",")
	f.write("]")
	f.close()
	raw = codecs.open(outFileName+"_raw"+".json",'w','utf-8')
	print(tweets,file=raw)
	raw.close()


#Entry point
def main():		
	global tweepyAPI
	twts = []
	setupAuthKeys(sys.argv[1])
	setupTweepyAPI()
	queryString = "apartment OR CONDO AND rent" #sys.argv[2]
	fromDate = "03"
	toDate = "04"#str(int(fromDate)+1)
	tweets = getTweets(queryString, fromDate, toDate)
	#for tweet in tweets:
#		processTweet(tweet)
	writeTweetsToFile(queryString,fromDate,toDate,tweets)


if __name__ == "__main__":
	main()
