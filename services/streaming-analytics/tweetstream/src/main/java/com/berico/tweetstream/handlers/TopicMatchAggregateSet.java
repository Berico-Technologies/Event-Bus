package com.berico.tweetstream.handlers;

import java.util.List;

public class TopicMatchAggregateSet {

	private List<TopicMatchAggregate> topicMatchAggregates = null;

	private TopicMatchAggregate changingAggregate = null;
	
	public TopicMatchAggregateSet(
			List<TopicMatchAggregate> topicMatchAggregates,
			TopicMatchAggregate changingAggregate) {

		this.topicMatchAggregates = topicMatchAggregates;
		this.changingAggregate = changingAggregate;
	}

	public List<TopicMatchAggregate> getTopicMatchAggregates() {
		return topicMatchAggregates;
	}

	public void setTopicMatchAggregates(
			List<TopicMatchAggregate> topicMatchAggregates) {
		this.topicMatchAggregates = topicMatchAggregates;
	}

	public TopicMatchAggregate getChangingAggregate() {
		return changingAggregate;
	}

	public void setChangingAggregate(TopicMatchAggregate changingAggregate) {
		this.changingAggregate = changingAggregate;
	}	
	
}
