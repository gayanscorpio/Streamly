package com.streamly.exception;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component // Spring auto-registers it
class ApiExceptionResolver extends DataFetcherExceptionResolverAdapter {
	@Override
	protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
		if (ex instanceof ApiException e) // only handle OUR exceptions
			return GraphqlErrorBuilder.newError(env) // build a GraphQL error...
					.errorType(e.type) // ...with our category
					.message(e.getMessage()) // ...and our readable message
					.build();
		return null; // null = let Spring handle anything else
	}
}
