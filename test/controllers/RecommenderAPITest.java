/**
 * @author mathana
 */
package controllers;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static models.Fixtures.*;

import models.Fixtures;
import models.Movie;
import models.Rating;
import models.User;

/**
 * A test for the RecommenderAPI which contains the main functionality of the program
 *
 */
public class RecommenderAPITest
{
	private RecommenderAPI recommender;
	private Fixtures f; 

	@Before
	public void setUp() throws Exception
	{
		recommender = new RecommenderAPI();
		f = new Fixtures();

		for (User user: usersFixtures)
		{
			recommender.addUser(user.firstName, user.lastName, user.age, user.gender, 
					user.occupation, user.username, user.password);
		}
		for (Movie movie: moviesFixtures)
		{
			recommender.addMovie(movie.title, movie.year, movie.url);
		}
		for (Rating r: ratingsFixtures)
		{
			recommender.addRating(r.userId, r.movieId, r.rating);
		}

		f.userRatingsFixtures();
		f.ratedMoviesFixtures();
	}

	@After
	public void tearDown() throws Exception
	{
		recommender = null;
	}

	/**
	 * Test to see if a used can be added 
	 */
	@Test
	public void testUser() 
	{
		assertEquals(usersFixtures.length, recommender.getUsers().size());
		recommender.addUser("Joe", "Bloggs", 54, 'M', "Network Administrator", "jbloggs", "secret");
		assertEquals(usersFixtures.length + 1, recommender.getUsers().size());
		assertEquals(usersFixtures[0].ratedMovies.size(), recommender.getUserById(usersFixtures[0].userId).ratedMovies.size());			
		long newUserId = usersFixtures.length + 1;
		assertEquals("Joe", recommender.getUserById(newUserId).firstName);
	}

	/**
	 * Test to ensure a user can be removed from the data set
	 */
	@Test
	public void testRemoveUser()
	{
		assertEquals(usersFixtures.length, recommender.getUsers().size());
		User user = recommender.getUserById(4L);
		recommender.removeUser(user.userId);
		assertEquals(usersFixtures.length - 1, recommender.getUsers().size());
	}

	/**
	 * Test to see if multiple users can be added and stored
	 */
	@Test
	public void testUsers()
	{
		for (User user: usersFixtures)
		{
			assertEquals(user, recommender.getUserById(user.userId));
			assertNotSame(user, recommender.getUserById(user.userId));
		}
	}

	/**
	 * Test the functionality of the addMovie method
	 */
	@Test
	public void testMovie()
	{
		assertEquals(moviesFixtures.length, recommender.getMovies().size());
		recommender.addMovie("The Godfather", 1972, "http://www.imdb.com/title/tt0068646/");
		assertEquals(moviesFixtures.length + 1, recommender.getMovies().size());
		assertEquals(moviesFixtures[0].userRatings.size(), recommender.getMovieById(moviesFixtures[0].movieId).userRatings.size());		
		long newMovieId = moviesFixtures.length + 1;
		assertEquals("The Godfather", recommender.getMovieById(newMovieId).title);
	}

	/**
	 * Test if multiple movies can be added and stored 
	 */
	@Test
	public void testMovies()
	{
		for (Movie movie: moviesFixtures)
		{
			assertEquals(movie, recommender.getMovieById(movie.movieId));
			assertNotSame(movie, recommender.getMovieById(movie.movieId));
		}
	}

	/**
	 * Test if a user can rate a movie
	 */
	@Test
	public void testRating()
	{
		assertEquals(ratingsFixtures.length, recommender.getRatings().size());
		User user = usersFixtures[0];
		Movie movie = moviesFixtures[0];
		recommender.addRating(user.userId, movie.movieId, 5);
		assertEquals(ratingsFixtures.length + 1, recommender.getRatings().size());
		assertEquals(ratingsFixtures[0].movieId, recommender.getMovieById(ratingsFixtures[0].movieId).movieId);		
	}

	/**
	 * Test if multiple ratings can be added and stored in various sub data structures
	 */
	@Test
	public void testRatings()
	{
		for (Rating r: ratingsFixtures)
		{
			User user = usersFixtures[(int) r.userId - 1];
			Integer userRating = user.ratedMovies.get(r.movieId).rating;		
			assertEquals(r.rating, userRating);

		}
	}

	/**
	 * Test if the stored used ratings can be retrieved correctly 
	 */
	@Test
	public void testGetUserRatings()
	{
		User user = usersFixtures[0];
		assertEquals(user.ratedMovies.size(), recommender.getUserById(user.userId).ratedMovies.size());
		assertEquals(user.ratedMovies.get(1), recommender.getUserById(user.userId).ratedMovies.get(1));
	}

	/**
	 * Test the accuracy of the top 10 movies result
	 */
	@Test
	public void testGetTopTen()
	{
		recommender.addMovie("The Godfather", 1972, "http://www.imdb.com/title/tt0068646/");
		assertEquals(moviesFixtures.length + 1, recommender.getMovies().size()); //Adding another movie to make it 11

		List<Movie> topTen = recommender.getTopTenMovies();
		assertEquals(10, topTen.size()); //Assure only 10 movies printed

		assertEquals(topTen.get(0).movieId, moviesFixtures[0].movieId);
		assertEquals(topTen.get(0).getAverageRating(), 3.4, 0.001);
		assertEquals(topTen.get(1).movieId, moviesFixtures[7].movieId);
		assertEquals(topTen.get(1).getAverageRating(), 3.0, 0.001);
		assertEquals(topTen.get(2).movieId, moviesFixtures[2].movieId);
		assertEquals(topTen.get(2).getAverageRating(), 1.8, 0.001);
		assertEquals(topTen.get(3).movieId, moviesFixtures[9].movieId);
		assertEquals(topTen.get(3).getAverageRating(), 1.5, 0.001);

	}

	/**
	 * Test that the movie recommendations are as predicted 
	 */
	@Test
	public void testGetUserRecommendations()
	{
		User user = recommender.getUserById(usersFixtures[2].userId);
		List<Movie> recommendedMovies = recommender.getUserRecommendations(user.userId);
		System.out.println(recommendedMovies);
		assertNotEquals(user.ratedMovies.size(), recommendedMovies.size());
		assertTrue(!recommendedMovies.contains(user.ratedMovies.get(1)));
		assertTrue(!recommendedMovies.contains(user.ratedMovies.get(4)));

	}

	/**
	 * Test if the login authenticator system is working
	 */
	@Test
	public void testAuthenticate()
	{
		recommender.addUser("Joe", "Bloggs", 54, 'M', "Network Administrator", "jbloggs", "secret");
		assertTrue(recommender.authenticate("jbloggs", "secret"));
		assertFalse(recommender.authenticate("Jbloggs", "Secret"));
		assertFalse(recommender.authenticate("Hello", "PassLOL"));
	}

	/**
	 * Test for exceptions
	 */
	@Test (expected = Exception.class)
	public void testExceptions()
	{
		//authenticate
		String username = null;
		String password = null;
		recommender.authenticate(username, password);

		//topTen
		recommender.users = null;
		recommender.movies = null;
		recommender.ratings = null;
		recommender.getTopTenMovies();

		//getUserRecommendations
		recommender.users = null;
		recommender.movies = null;
		recommender.ratings = null;
		long userId = -500;
		recommender.getUserRecommendations(userId);

		//testAddMovie
		String title = null;
		int year = Integer.MAX_VALUE;
		int year2 = Integer.MIN_VALUE;
		String url = null;
		recommender.addMovie(title, year, url);
		recommender.addMovie(title, year2, url);

		//testAddUser
		String firstName = null;
		String lastName = null;
		int age = Integer.MAX_VALUE;
		int age2 = Integer.MIN_VALUE;
		char gender = '&';
		String occupation = null;
		String username2 = "^&%^&^%";
		String password2 = "*&^&&^*&";
		recommender.addUser(firstName, lastName, age, gender, occupation, username2, password2);
		recommender.addUser(firstName, lastName, age2, gender, occupation, username2, password2);

		//addRating
		recommender.addRating(Long.MAX_VALUE, Long.MAX_VALUE, Integer.MAX_VALUE);
		recommender.addRating(Long.MIN_VALUE, Long.MIN_VALUE, Integer.MIN_VALUE);

		//removeUser
		recommender.removeUser(-5000L);
	}	
}
