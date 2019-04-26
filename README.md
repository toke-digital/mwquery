# mwquery - RESTful web service client cli


## Examples

```
# tweet
-r POST \
--oauth ./twitter.properties \
--strictRFC3896 \
--url "https://api.twitter.com/1.1/statuses/update.json?include_entities=true" \
--data "status=ANZAC Day is sort of a national Veterans holiday in Australia - and it's today. It's a combined Australia/New Zealand holiday." \
--dump

# get status timeline (tweets) for US President Trump, extract the text
--oauth ./twitter.properties \
--strictRFC3896 \
--url "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=realDonaldTrump" \
--query "list=$..text"



```

