/* package com.streamly.datafeeder;

//WHAT: Fills the empty in-memory database with sample movies and rails at startup.
//WHY: H2 starts empty every time, so without this the home page would be blank.

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.streamly.dto.Movie;
import com.streamly.dto.Rail;
import com.streamly.repository.MovieRepo;
import com.streamly.repository.RailRepo;

@Component
class DataSeeder implements CommandLineRunner { // CommandLineRunner.run() executes once after startup
	private static final String BASE = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/"; // public
																												// sample
																												// videos
	private static final String[] TRAILERS = { "ForBiggerBlazes.mp4", "ForBiggerEscapes.mp4", "ForBiggerJoyrides.mp4" }; // short
																															// clips
																															// =
																															// "trailers"
	private static final String[] FULL = { "BigBuckBunny.mp4", "ElephantsDream.mp4", "Sintel.mp4", "TearsOfSteel.mp4" }; // long
																															// clips
																															// =
																															// "full
																															// movies"

	private final MovieRepo movies;
	private final RailRepo rails;

	DataSeeder(MovieRepo movies, RailRepo rails) {
		this.movies = movies;
		this.rails = rails;
	}

	@Override
	public void run(String... args) {
		String[][] d = { // {title, genre, year, synopsis}
				{ "Neon Harbor", "Thriller", "2024",
						"A night-shift dockworker finds a ledger that names half the city." },
				{ "The Last Lighthouse", "Drama", "2022",
						"Three generations of keepers fight to keep a dying coast alive." },
				{ "Orbit Nine", "Sci-Fi", "2025",
						"A repair crew drifts past the edge of the map and hears something answer." },
				{ "Tuesday Specials", "Comedy", "2023",
						"A failing diner enters a cook-off it has no business winning." },
				{ "Glass Garden", "Thriller", "2021", "A botanist's greenhouse hides more than orchids." },
				{ "Paper Moons", "Drama", "2025", "Two estranged sisters reopen their father's puppet theatre." },
				{ "Signal Lost", "Sci-Fi", "2024", "When every phone on Earth goes silent, one radio keeps talking." },
				{ "Second Wind", "Comedy", "2022", "A retired sprinter coaches a village relay team to nationals." },
				{ "Ashfall", "Sci-Fi", "2023", "The volcano is the least of the crew's problems." },
				{ "Cold Open", "Comedy", "2025", "A late-night writers' room survives one catastrophic live show." },
				{ "Marrow Road", "Thriller", "2020", "A hitchhiker knows too much about the driver's family." },
				{ "Kite Season", "Drama", "2024", "A summer festival brings a runaway home." } };
		List<Movie> all = new ArrayList<>();
		for (int i = 0; i < d.length; i++) { // build one Movie per row
			Movie m = new Movie();
			m.title = d[i][0];
			m.genre = d[i][1];
			m.year = Integer.parseInt(d[i][2]);
			m.synopsis = d[i][3];
			m.posterUrl = "https://picsum.photos/seed/" + m.title.replace(' ', '-').toLowerCase() + "/300/450"; // placeholder
																												// poster,
																												// same
																												// title
																												// =
																												// same
																												// image
			m.trailerUrl = BASE + TRAILERS[i % TRAILERS.length]; // rotate through the sample clips
			m.streamUrl = BASE + FULL[i % FULL.length];
			all.add(movies.save(m)); // save() returns the movie with its new id
		}
		rail("Trending now", 0, all.stream().limit(6).toList()); // first 6 movies
		rail("New releases", 1, all.stream().filter(m -> m.year >= 2024).toList()); // 2024 or newer
		rail("Sci-Fi", 2, all.stream().filter(m -> m.genre.equals("Sci-Fi")).toList()); // by genre
		rail("Comedy", 3, all.stream().filter(m -> m.genre.equals("Comedy")).toList());
	}

	// Helper: create and save one rail
	private void rail(String title, int pos, List<Movie> list) {
		Rail r = new Rail();
		r.title = title;
		r.position = pos;
		r.movies = list;
		rails.save(r);
	}
} */
