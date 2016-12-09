package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import models.Movie;
import models.Rating;
import models.User;
import utils.MovieAverageRatingComparator;
import utils.Parser;
import utils.RatingByRatingComparator;
import utils.Serializer;

public class RecommenderAPI
{
  private Serializer serializer;
  
	Map<Long, User> users = new HashMap<>();
	Map<String, User> usersLogin = new HashMap<>();
	Map<Long, Movie> movies = new HashMap<>();
	List<Rating> ratings = new ArrayList<>();
	Parser parser = new Parser();
	RatingByRatingComparator ratingComparator;
	MovieAverageRatingComparator movieAvgComparator;

	public RecommenderAPI()
	{}
	
	
	
	public RecommenderAPI(Serializer serializer) throws Exception
	{
		this.serializer = serializer;
	}

	
	public void loadRawData() throws Exception
	{
		String userDataPath = ("././data/moviedata_small/users5.dat");
		String movieDataPath = ("././data/moviedata_small/items5.dat");
		String ratingDataPath = ("././data/moviedata_small/ratings5.dat");
		
		users = parser.parseUserData(userDataPath);
		movies = parser.parseMovieData(movieDataPath);
		ratings = parser.parseRatingData(ratingDataPath);
		store();
	}
	
  @SuppressWarnings("unchecked")
  public void load() throws Exception
  {
    serializer.read();
    ratings = (List<Rating>) serializer.pop();
    movies = (Map<Long, Movie>) serializer.pop();
    usersLogin = (Map<String, User>) serializer.pop();
    users = (Map<Long, User>) serializer.pop();
  }
  
  public void store() throws Exception
  {
    serializer.push(users);
    serializer.push(usersLogin);
    serializer.push(movies);
    serializer.push(ratings);
    serializer.write(); 
  }
  
	public User addUser(String firstName, String lastName, int age, char gender, String occupation, String username, String password)
	{
		long userId = users.size() + 1;
		User user = new User(userId, firstName, lastName, age, gender, occupation, username, password);
		users.put(user.userId, user);
		usersLogin.put(user.username, user);
		return user;
	}
	
	public void removeUser(long userId)
	{
		User user = getUserById(userId);
		users.remove(user.userId);
		usersLogin.remove(user.username);
	}
	
	public Movie addMovie(String title, int year, String url)
	{
		long movieId = movies.size() + 1;
		Movie movie = new Movie(movieId, title, year, url);
		movies.put(movieId, movie);
		return movie;
	}
	
	public Rating addRating(long userId, long movieId, int rating)
	{
		Rating r = new Rating(userId, movieId, rating);
		User user = getUserById(r.userId);
		Movie movie = getMovieById(r.movieId);

		user.addRatedMovies(movie.movieId, r);
		movie.addUserRatings(user.userId, r);
		ratings.add(r);
		
		return r;
	}
	
	public List<Movie> getTopTenMovies()
	{
		List<Movie> topTen = new ArrayList<>(movies.values());
		movieAvgComparator = new MovieAverageRatingComparator();
		Collections.sort(topTen, movieAvgComparator);
		if (topTen.size() > 10)
		{
			return topTen.subList(0, 10);
		}
		else
		{
			return topTen;
		}
	}
	public List<Movie> getUserRecommendations(long userId)
	{
		User currentUser = getUserById(userId);
		
		if (currentUser.ratedMovies.size() > 0) 
		{
			ratingComparator = new RatingByRatingComparator(); //Sorts highest to lowest rating
			movieAvgComparator = new MovieAverageRatingComparator(); //Sorts highest to lowest rating average of a movie
			HashSet<User> targetUsers = new HashSet<>();
			Set<Movie> recommendedMoviesSet = new HashSet<>();
			List<Rating> currentUserRatings = new ArrayList<>(currentUser.ratedMovies.values());
			Collections.sort(currentUserRatings, ratingComparator);
			for (Rating currentUserRating: currentUserRatings)
			{
				if(currentUserRating.rating >= 3)
				{
					Movie movie = getMovieById(currentUserRating.movieId);
					List<Rating> topUserRatings = new ArrayList<>(movie.userRatings.values());
					Collections.sort(topUserRatings, ratingComparator);
					for (Rating otherUserRating: topUserRatings)
					{
						if (otherUserRating.rating >= currentUserRating.rating)
						{
							targetUsers.add(getUserById(otherUserRating.userId));
						}
					}
				}
				
			}
			//For each of the narrowed down target users, each with their own list/map of rated movies
			for (User target: targetUsers)
			{
				List<Rating> targetRatedMovies = new ArrayList<>(target.ratedMovies.values());
				Collections.sort(targetRatedMovies, ratingComparator);
				
				for (Rating targetRating: targetRatedMovies)
				{
					//If above 3 and the movie has not been rated before
					if (targetRating.rating >= 3 && !currentUser.ratedMovies.containsKey(targetRating.movieId))
					{
						Movie movie = getMovieById(targetRating.movieId);
						recommendedMoviesSet.add(movie); //Using a set to ensure no duplicates
						
					}
				}
			}
			List<Movie> recommendedMoviesList = new ArrayList<>(recommendedMoviesSet);
			Collections.sort(recommendedMoviesList, movieAvgComparator); //Sort final results with best first
			System.out.println(recommendedMoviesList);
			return recommendedMoviesList;
		}
		else
		{
			return null;
		}
	}
	
	public boolean authenticate(String username, String password)
	{
		if (usersLogin.containsKey(username))
		{
			User user = usersLogin.get(username);
			if (user.password.matches(password))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<Rating> getRatings()
	{
		return ratings;
	}
	
	public Map<Long, Movie> getMovies()
	{
		return movies;
	}
	
	public Movie getMovieById(long id)
	{
		return movies.get(id);
	}
	
	public Map<Long, User> getUsers()
	{
		return users;
	}
	
	public User getUserById(long id)
	{
		return users.get(id);
	}
	
	public User getUserByUsername(String username)
	{
		return usersLogin.get(username);
	}

	public Map<Long, Rating> getUserRatings(long userId)
	{
		User user = getUserById(userId);
		return user.ratedMovies;
	}
}
