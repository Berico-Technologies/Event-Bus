package com.berico.tweetstream;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class TweetMapper implements FieldSetMapper<Tweet> {
    public Tweet mapFieldSet(FieldSet fs) {
                        
       if(fs == null){
           return null;
       }
                   
       Tweet tweet = new Tweet();
       
       User u = new User();
       u.setUserId(fs.readLong(0));
       u.setUser(fs.readString(1));
       u.setAccountName(fs.readString(1));
       tweet.setUser(u);
       
       tweet.setMessage(fs.readString(6));
       
       
       Location loc = new Location();
       

       loc.setCountry(fs.readString(12));

       String latLonString = fs.readString(10);
       if(!latLonString.isEmpty()){
    	   String[] latLon = latLonString.split(",");
           loc.setLatitude(Double.parseDouble(latLon[0]));
           loc.setLongitude(Double.parseDouble(latLon[1]));
       }
       
       loc.setFullname(fs.readString(11));
       tweet.setLocation(loc);
       
       
      
       
       return tweet;
   }
}
