package com.streamly.streamly_api;

//WHAT: End-to-end tests for EVERY GraphQL query and mutation (CatalogController + AccountController).

//WHY: they start the real app on a random port and send real HTTP requests to /graphql, so they also
//prove the pieces work together: schema, controllers, database, token header (TokenInterceptor) and errors.
//RUN:  mvn test

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath; // reads values out of JSON text, e.g. "$.data.me.email"
import java.net.URI;
import java.net.http.HttpClient; // built into Java: no extra test-client library needed
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

//RANDOM_PORT = start the whole app (incl. DataSeeder) on a free port so tests never clash with a running app
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphQlApiTest {

	@Value("${local.server.port}")
	int port; // Spring tells us which port it picked
	private final HttpClient http = HttpClient.newHttpClient();

	// ---------------------------------------------------------------- helpers

	// Send one GraphQL document; optional login token goes in the Authorization
	// header. Returns raw JSON text.
	private String gql(String query, String token) throws Exception {
		String escaped = query.replace("\\", "\\\\").replace("\"", "\\\""); // make the query safe inside a JSON string
		HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/graphql"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"" + escaped + "\"}"));
		if (token != null)
			req.header("Authorization", "Bearer " + token); // same header the Vue app sends
		return http.send(req.build(), HttpResponse.BodyHandlers.ofString()).body();
	}

	// Sign in with a brand-new unique email (so tests never share a user) and
	// return the token
	private String newToken() throws Exception {
		String email = "t-" + UUID.randomUUID() + "@example.com";
		return JsonPath.read(gql("mutation { login(email: \"" + email + "\") { token } }", null), "$.data.login.token");
	}

	// Id of the first seeded movie (we read it instead of assuming it is 1)
	private String firstMovieId() throws Exception {
		return JsonPath.read(gql("{ rails { movies { id } } }", null), "$.data.rails[0].movies[0].id");
	}

	// Shortcut for the error category of the first error, e.g. "FORBIDDEN"
	private String errorType(String body) {
		return JsonPath.read(body, "$.errors[0].extensions.classification");
	}

	// ---------------------------------------------------------------- Query.rails
	// / Query.movie (CatalogController)

	@Test
	void rails_returnsSeededRowsInOrder() throws Exception {
		String body = gql("{ rails { title movies { id } } }", null);
		List<String> titles = JsonPath.read(body, "$.data.rails[*].title");
		assertThat(titles).containsExactly("Trending now", "New releases", "Sci-Fi", "Comedy"); // sorted by position
		List<Object> trending = JsonPath.read(body, "$.data.rails[0].movies");
		assertThat(trending).hasSize(6); // seeder gives 6 movies
	}

	@Test
	void rails_exposeFreeTrailerUrl_withoutLogin() throws Exception {
		List<String> urls = JsonPath.read(gql("{ rails { movies { trailerUrl } } }", null),
				"$.data.rails[0].movies[*].trailerUrl");
		assertThat(urls).isNotEmpty().allMatch(u -> u.endsWith(".mp4")); // trailers are public: no token was sent
	}

	@Test
	void movie_byId_returnsThatMovie() throws Exception {
		String id = firstMovieId();
		String body = gql("{ movie(id: \"" + id + "\") { id title year genre } }", null);
		assertThat((String) JsonPath.read(body, "$.data.movie.id")).isEqualTo(id);
		assertThat((String) JsonPath.read(body, "$.data.movie.title")).isEqualTo("Neon Harbor"); // first seeded movie
	}

	@Test
	void movie_unknownId_returnsNull() throws Exception {
		Object movie = JsonPath.read(gql("{ movie(id: \"999999\") { id } }", null), "$.data.movie");
		assertThat(movie).isNull(); // Optional.empty() => null, not an error
	}

	@Test
	void movie_streamUrl_isNotPartOfThePublicSchema() throws Exception {
		String body = gql("{ rails { movies { streamUrl } } }", null); // asking for the secret field...
		assertThat((String) JsonPath.read(body, "$.errors[0].message")).contains("streamUrl"); // ...is a validation
																								// error
	}

	// ---------------------------------------------------------------- Query.plans
	// (AccountController)

	@Test
	void plans_listsAllThreePlansWithPrices() throws Exception {
		String body = gql("{ plans { plan priceMonthly description } }", null);
		List<String> names = JsonPath.read(body, "$.data.plans[*].plan");
		assertThat(names).containsExactly("BASIC", "STANDARD", "PREMIUM");
		assertThat((Double) JsonPath.read(body, "$.data.plans[0].priceMonthly")).isEqualTo(5.99);
	}

	// ---------------------------------------------------------------- Query.me /
	// Mutation.login

	@Test
	void me_withoutToken_isNull() throws Exception {
		Object me = JsonPath.read(gql("{ me { email } }", null), "$.data.me");
		assertThat(me).isNull(); // guest is not an error, just null
	}

	@Test
	void me_withInvalidToken_isNull() throws Exception {
		Object me = JsonPath.read(gql("{ me { email } }", "not-a-real-token"), "$.data.me");
		assertThat(me).isNull();
	}

	@Test
	void login_createsUser_andMeReturnsIt() throws Exception {
		String email = "new-" + UUID.randomUUID() + "@example.com";
		String login = gql("mutation { login(email: \"" + email + "\") { token user { email active plan } } }", null);
		String token = JsonPath.read(login, "$.data.login.token");
		assertThat(token).isNotBlank();
		assertThat((Boolean) JsonPath.read(login, "$.data.login.user.active")).isFalse(); // new user has no plan yet
		assertThat((Object) JsonPath.read(login, "$.data.login.user.plan")).isNull();

		String me = gql("{ me { email active } }", token); // the token now identifies the user
		assertThat((String) JsonPath.read(me, "$.data.me.email")).isEqualTo(email);
	}

	@Test
	void login_trimsAndLowercasesEmail() throws Exception {
		String unique = UUID.randomUUID().toString();
		String body = gql("mutation { login(email: \"  Mixed-" + unique + "@Example.COM \") { user { email } } }",
				null);
		assertThat((String) JsonPath.read(body, "$.data.login.user.email"))
				.isEqualTo("mixed-" + unique + "@example.com");
	}

	// ----------------------------------------------------------------
	// Mutation.subscribe / cancelSubscription

	@Test
	void subscribe_withoutLogin_isUnauthorized() throws Exception {
		String body = gql("mutation { subscribe(plan: BASIC) { active } }", null);
		assertThat(errorType(body)).isEqualTo("UNAUTHORIZED");
	}

	@Test
	void subscribe_activatesChosenPlan() throws Exception {
		String token = newToken();
		String body = gql("mutation { subscribe(plan: STANDARD) { plan active subscribedUntil } }", token);
		assertThat((String) JsonPath.read(body, "$.data.subscribe.plan")).isEqualTo("STANDARD");
		assertThat((Boolean) JsonPath.read(body, "$.data.subscribe.active")).isTrue();
		assertThat((String) JsonPath.read(body, "$.data.subscribe.subscribedUntil")).isNotBlank(); // ~30 days from now
	}

	@Test
	void cancelSubscription_removesPlan() throws Exception {
		String token = newToken();
		gql("mutation { subscribe(plan: PREMIUM) { active } }", token);
		String body = gql("mutation { cancelSubscription { plan active subscribedUntil } }", token);
		assertThat((Boolean) JsonPath.read(body, "$.data.cancelSubscription.active")).isFalse();
		assertThat((Object) JsonPath.read(body, "$.data.cancelSubscription.plan")).isNull();
	}

	@Test
	void cancelSubscription_withoutLogin_isUnauthorized() throws Exception {
		assertThat(errorType(gql("mutation { cancelSubscription { active } }", null))).isEqualTo("UNAUTHORIZED");
	}

	// ---------------------------------------------------------------- Query.watch
	// (the paid feature)

	@Test
	void watch_withoutLogin_isUnauthorized() throws Exception {
		String body = gql("{ watch(movieId: \"" + firstMovieId() + "\") { url } }", null);
		assertThat(errorType(body)).isEqualTo("UNAUTHORIZED");
	}

	@Test
	void watch_withoutPlan_isForbidden_andHintsAtTrailers() throws Exception {
		String body = gql("{ watch(movieId: \"" + firstMovieId() + "\") { url } }", newToken());
		assertThat(errorType(body)).isEqualTo("FORBIDDEN");
		assertThat((String) JsonPath.read(body, "$.errors[0].message")).contains("Subscribe"); // message the UI shows
	}

	@Test
	void watch_withActivePlan_returnsFullStreamUrl() throws Exception {
		String token = newToken();
		String id = firstMovieId();
		gql("mutation { subscribe(plan: BASIC) { active } }", token);
		String body = gql("{ watch(movieId: \"" + id + "\") { movieId url } }", token);
		assertThat((String) JsonPath.read(body, "$.data.watch.movieId")).isEqualTo(id);
		assertThat((String) JsonPath.read(body, "$.data.watch.url")).endsWith(".mp4");
	}

	@Test
	void watch_afterCancelling_isForbiddenAgain() throws Exception {
		String token = newToken();
		String id = firstMovieId();
		gql("mutation { subscribe(plan: BASIC) { active } }", token);
		gql("mutation { cancelSubscription { active } }", token);
		assertThat(errorType(gql("{ watch(movieId: \"" + id + "\") { url } }", token))).isEqualTo("FORBIDDEN");
	}

	@Test
	void watch_unknownMovie_isNotFound() throws Exception {
		String token = newToken();
		gql("mutation { subscribe(plan: BASIC) { active } }", token); // plan is fine, the movie id is not
		assertThat(errorType(gql("{ watch(movieId: \"999999\") { url } }", token))).isEqualTo("NOT_FOUND");
	}
}
