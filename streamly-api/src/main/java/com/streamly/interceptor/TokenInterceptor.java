package com.streamly.interceptor;

//WHAT: A hook that runs before every GraphQL request. WHY: reads the login token from the HTTP header
//once, so controllers can just ask for "token" instead of parsing headers themselves.

import java.util.Map;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component // Spring picks it up automatically
class TokenInterceptor implements WebGraphQlInterceptor {
	@Override
	public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
		String h = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION); // header "Authorization: Bearer abc"
		if (h != null && h.startsWith("Bearer ")) { // only if it has the expected format
			String token = h.substring(7); // cut off "Bearer " (7 characters)
			// put the token in the GraphQL "context" so controllers can read it with
			// @ContextValue
			request.configureExecutionInput((in, b) -> b.graphQLContext(Map.of("token", token)).build());
		}
		return chain.next(request); // continue to the normal GraphQL processing
	}
}
