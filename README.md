# mwquery - RESTful web service client cli

The idea here is a tool which is a combination of curl and jq. We want to get the values out of a json response and make them available to the next service call.

Work In Progress here...

## Examples

Basic 

```
# print the headers
-r HEAD --url "https://www.google.com/"  --dump

```

Twitter

```
# tweet
-r POST \
--oauth ./twitter.properties \
--strictRFC3896 \
--url "https://api.twitter.com/1.1/statuses/update.json?include_entities=true" \
--data "status=ANZAC Day is sort of a national Veterans holiday in Australia - and it's today. It's a combined Australia/New Zealand holiday." \
--dump

# get status timeline text(tweets) for US President Trump, extract the text
--oauth ./twitter.properties \
--strictRFC3896 \
--url "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=realDonaldTrump" \
--query "list=$..text"

```



