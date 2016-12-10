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
			Set<Long> targetUsers = users.keySet();
			targetUsers.remove(currentUser.userId);
			List<Rating> currentUserRatings = new ArrayList<>(currentUser.ratedMovies.values());

			for (Rating currentUserRating: currentUserRatings)
			{
				Movie movie = getMovieById(currentUserRating.movieId);
				Set<Long> currentTarget = movie.userRatings.keySet();
				targetUsers.retainAll(currentTarget);
			}
			Long mostSimilar = 0L;

			for (Rating currentUserRating: currentUserRatings)
			{
				int currentRating = currentUserRating.rating;

				Movie movie = getMovieById(currentUserRating.movieId);

				for (Long targetId: targetUsers)
				{
					int similarity = 0;
					User targetUser = getUserById(targetId);
					int targetRating = targetUser.ratedMovies.get(movie.movieId).rating;
					similarity += (currentRating * targetRating);

					if (similarity > mostSimilar)
					{
						mostSimilar = targetId;
					}

				}
			}
			User mostSimilarUser = getUserById(mostSimilar);
			List<Movie> recommendedMoviesList = new ArrayList<>();
			
			if (mostSimilarUser.ratedMovies.size() > 0) {
				for (Rating rating: mostSimilarUser.ratedMovies.values())
				{
					if (rating.rating >=3 && !currentUser.ratedMovies.containsKey(rating.movieId))
					{
						recommendedMoviesList.add(getMovieById(rating.movieId));
					}
				}
			}
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
