package models;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MovieTest
{
	Movie movie;

	@Before
	public void setUp() throws Exception
	{
		movie = new Movie(23, "Jolly Jolly WITmas", 1996, "http://bit.ly/test");
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testCreateMovie()
	{
		assertEquals(23,  movie.movieId);
		assertEquals("Jolly Jolly WITmas", movie.title);
		assertEquals(1996, movie.year);
		assertEquals("http://bit.ly/test", movie.url);
	}

//	@Test
//	public void testAddAverageRating()
//	{
//		movie.addAverageRating(1);
//		movie.addAverageRating(3);
//		movie.addAverageRating(5);
//		assertEquals(9.0, movie.averageRating, 0.01);
//	}
	
	@Test
	public void testGetAverageRating()
	{
		movie.addUserRatings(1L, 1);
		movie.addUserRatings(2L, 3);
		movie.addUserRatings(3L, 5);
		assertEquals(9.0/3, movie.getAverageRating(), 0.01);
	}
}
