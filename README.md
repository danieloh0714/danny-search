# DannySearch
This is a news article search engine replica based off of news search engines such as https://news.google.com/

[Here](https://drive.google.com/file/d/16MZQNZtdZuYb41b_8aX63awvx_fzQTrq/view?usp=sharing) is a demo video where I briefly show architecture of the application and its features.

## News Article ETL
The first component of the architecture is the news article ETL, which fetches news articles from the Common Crawl dataset.

I used Java to build an application that would fetch the news articles from the latest Common Crawl set, parse the WARC files,
and create and upload the articles to an index on Amazon Elasticsearch Service (Amazon ES).

This ETL application would only do this when ran. To make sure that the latest news articles would be uploaded to the ES index periodically,
I set up an AWS EC2 instance and set up a Cron Job that would run the ETL every two hours. This way, my ES idnex would constantly be updating with new articles.

## Search Backend Service
With news article documents now in my ES index (and new ones being uploaded periodically), I then built a backend application
that would allow a client to search the ES index for documents that matched provided query parameters (keyword, language, date, etc.) using a GET request.

The client could provide these parameters in the url using the Lucene query syntax:

e.g. GET /search?q=Northwestern%20AND%20lang:en&from=30&size=10

which would request ten English articles with the keyword "Northwestern" starting from the thirtieth result.

I hosted this application on AWS Elastic Beanstalk to an Apache Tomcat environment. This backend application is stateless--this is important,
because stateless applications are easy to scale, and I was able to easily put a load balancer in front of multiple instances of the application.

## Frontend
For the frontend, I built a React SPA with TypeScript. The application calls GET requests to the backend service deployed on AWS Elastic Beanstalk
and displays the returned documents in the ES index. The site only displays ten articles max, but the user can click a button at the bottom of the page to load more articles.

The frontend application is deployed to AWS in an S3 bucket.
