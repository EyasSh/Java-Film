import java.io.*;
import java.rmi.UnexpectedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmReport {
    final static  String DB_URL = "jdbc:mysql://localhost:3306/sakila?user=root";
    private String username;
    private  String password;

    Connection connection;
    /**
     * Constructs a FilmReport object with the provided username and password.
     * The constructor initializes the database connection using the provided credentials
     * to establish a connection to the database. Additionally, it calls the createTables method
     * to ensure that required tables are created in the database.
     *
     * @param username the username used to connect to the database.
     * @param password the password used to connect to the database.
     * @throws Exception if an error occurs during the database connection setup or table creation.
     */
    public FilmReport(String username, String password) throws Exception
    {
        this.username=username;
        this.password=password;
        try {
            connection = DriverManager.getConnection(DB_URL, this.username, this.password);
            System.out.println("Connected to database");


        } catch (SQLException e) {
            e.printStackTrace(); // Handle connection failure
            throw e;
        }
    }


    /**
     * Retrieves a list of film titles featuring an actor with the specified first name and last name.
     * The method executes a SQL query to select film titles from the database, filtering films by
     * the given actor's first name and last name. The query is parameterized to prevent SQL injection
     * vulnerabilities. The result is a string array containing the titles of the films featuring the actor.
     *
     * @param firstName the first name of the actor.
     * @param lastName  the last name of the actor.
     * @return an array of strings representing the titles of films featuring the actor.
     * @throws SQLException if an SQL exception occurs while executing the query.
     */
    public String[] getFilmsByActor(String firstName,String lastName) throws Exception
    {
        List<String> films = new ArrayList<>();
        /*
        * SQL query obtained using chatGPT
        * */
        final String query =  "SELECT film.title " +
                "FROM film " +
                "INNER JOIN film_actor ON film.film_id = film_actor.film_id " +
                "INNER JOIN actor ON film_actor.actor_id = actor.actor_id " +
                "WHERE actor.first_name = ? AND actor.last_name = ? " +
                "ORDER BY film.title ASC";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,firstName);
            statement.setString(2,lastName);
            ResultSet set = statement.executeQuery();
            while (set.next()){
                String filmTitle = set.getString("title");
                films.add(filmTitle);
            }

        } catch (SQLException e) {
            throw  e;
        }
        // Convert List<String> to String[]
        String[] filmsArray = new String[films.size()];
        return films.toArray(filmsArray);
    }
    /**
     * Retrieves movies with similar titles based on specific criteria from an array of movie titles.
     * The method takes an array of movie titles as input and returns an array of movies with similar titles,
     * where similarity is determined by movies sharing commonalities in their descriptions and running times.
     * Movies are considered similar if:
     * - Their descriptions contain at least one common word with exactly five letters.
     * - Their running times differ by two minutes or less.
     *
     * @param titles an array of movie titles for which similar titles are to be retrieved.
     * @return an array of strings representing similar movie titles along with their corresponding similar titles.
     *         Each string contains the original movie title followed by a colon ':' and the titles of similar movies,
     *         separated by colons ':'.
     * @throws Exception if an unexpected exception occurs during the execution of the method.
     */
    public String[] getSimilarTitles( String[] titles) throws Exception
    {
        if (titles.length==0){
            System.out.println("Title array is empty");
            return  new String[0];
        }

        else
        {

                List<String[]> moviesByTitlesData = fetchMoviesByTitles(titles);

            if(moviesByTitlesData.size()==3)
            {
                //get the data from the title arrList

                String[] titlesASC = moviesByTitlesData.get(0);
                String[] descriptionsFromTitles = moviesByTitlesData.get(1);
                String[] timesFromTitles =moviesByTitlesData.get(2);

                // Declaring result list
                List<String> resultList = new ArrayList<>();
                String similarTitles= "";
                for (int i =0; i< titlesASC.length;i++)
                {
                    similarTitles=titlesASC[i];
                    for(int j=0; j<titlesASC.length;j++)
                    {
                        if(!areSimilar(descriptionsFromTitles[i],descriptionsFromTitles[j],Integer.parseInt(timesFromTitles[i]),Integer.parseInt(timesFromTitles[j]))){
                            continue;
                        }
                        else
                            similarTitles+=":"+titlesASC[j];
                    }
                    if(similarTitles.equals(titlesASC[i])){
                        continue;
                    }

                    else{
                        titlesASC[i]=similarTitles;
                        similarTitles="";
                        continue;
                    }
                }
                return titlesASC;

            }
            else
            {
                System.out.println("The array lists in the similar tiles method do not have the appropriate size");
                UnexpectedException e =new UnexpectedException("Lists do not have the proper size in getSimilarTitles");
                e.printStackTrace();
                return  new String[0];
            }


        }
    }
    /**
     * Fetches movie details (titles, descriptions, and times) based on the provided titles.
     * The method queries the database to retrieve movie details for the titles provided in the input array.
     * It constructs and executes a SQL query with a dynamic IN clause to efficiently fetch movie details
     * for multiple titles at once. The retrieved movie details are then added to separate lists for titles,
     * descriptions, and times, which are subsequently converted to arrays and returned as a list of arrays.
     *
     * @param titles an array of movie titles for which details are to be fetched.
     * @return a list containing arrays of strings representing the fetched movie details.
     *         Each array contains details for titles, descriptions, and times, respectively.
     * @throws SQLException if an SQL exception occurs during the execution of the database query.
     */
    private List<String[]> fetchMoviesByTitles (String[] titles){
        List<String> names = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<String> times = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, username, password)) {
            // Create a placeholder for the IN clause
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < titles.length; i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("?");
            }

            // Prepare SQL query with dynamic IN clause and ORDER BY
            String query = "SELECT title, description, length_minutes FROM film WHERE title IN (" + placeholders + ") ORDER BY title ASC";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Set titles as parameters for the IN clause
                for (int i = 0; i < titles.length; i++) {
                    statement.setString(i + 1, titles[i]);
                }

                // Execute the query
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String description = resultSet.getString("description");
                        int lengthMinutes = resultSet.getInt("length_minutes");

                        // Add movie data to the respective lists
                        names.add(title);
                        descriptions.add(description);
                        times.add(String.valueOf(lengthMinutes));
                    }
                    connection.close();
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }

        // Convert lists to arrays and return as a list of arrays
        List<String[]> movieData = new ArrayList<>();
        movieData.add(names.toArray(new String[names.size()]));
        movieData.add(descriptions.toArray(new String[names.size()]));
        movieData.add(times.toArray(new String[names.size()]));

        return movieData;
    }
    /**
     * Fetches all movie details (titles, descriptions, and times) in alphabetical order.
     * The method queries the database to retrieve all movie details, ordered alphabetically by title.
     * It constructs and executes a SQL query to fetch movie details for all movies in the database.
     * The retrieved movie details are then added to separate lists for titles, descriptions, and times,
     * which are subsequently converted to arrays and returned as a list of arrays.
     *
     * @return a list containing arrays of strings representing the fetched movie details.
     *         Each array contains details for titles, descriptions, and times, respectively.
     */
    private List<String[]> fetchMoviesAlphabetical() {
        List<String> names = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        List<String> times = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, username, password)) {
            String query = "SELECT title, description, length_minutes FROM film ORDER BY title ASC";

            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String description = resultSet.getString("description");
                    int lengthMinutes = resultSet.getInt("length_minutes");

                    // Add movie data to the respective lists
                    names.add(title);
                    descriptions.add(description);
                    times.add(String.valueOf(lengthMinutes));
                }
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert lists to arrays and return as a list of arrays
        List<String[]> movieData = new ArrayList<>();
        movieData.add(names.toArray(new String[names.size()]));
        movieData.add(descriptions.toArray(new String[descriptions.size()]));
        movieData.add(times.toArray(new String[times.size()]));

        return movieData;
    }
    /**
     * Checks if two movies are similar based on their descriptions and running times.
     * The method compares the descriptions of two movies to determine if they contain a common word
     * with exactly five letters. It also checks if the difference in running times between the two movies
     * is two minutes or less. Movies cannot be similar to themselves.
     *
     * @param fiveWordFirst  the description of the first movie.
     * @param fiveWordSecond the description of the second movie.
     * @param minsFirst      the running time in minutes of the first movie.
     * @param minsSecond     the running time in minutes of the second movie.
     * @return true if the movies are considered similar based on the defined criteria, false otherwise.
     */
    private boolean areSimilar(String fiveWordFirst,String fiveWordSecond,int minsFirst,int minsSecond){
        return ((!WordWithFiveLetters(fiveWordFirst).equals("")&& !WordWithFiveLetters(fiveWordSecond).equals("")) && (!fiveWordFirst.equals(fiveWordSecond))
                &&(WordWithFiveLetters(fiveWordFirst).equals(WordWithFiveLetters(fiveWordSecond))))&& (minsFirst-minsSecond<=2 || minsSecond-minsFirst<=2);
    }
    /**
     * Fetches the first word with five letters from the given description string.
     * The method iterates through the characters of the description string until it finds
     * the first word with exactly five letters. If the description string is less than five letters
     * or a word with five letters is not found, the method returns an empty string.
     *
     * @param s the description string from which to fetch the word with five letters.
     * @return the first word with five letters found in the description string, or an empty string
     *         if the description string is less than five letters or a word with five letters is not found.
     */
    private String WordWithFiveLetters(String s){
        if(s.length()<5){
            System.out.println("String s has a length lower than 5 at: WordWithFiveLetters");
            return "";
        }
        String word="";
        for(int i=0; i<s.length();i++){
            char ch = s.charAt(i);

            if(!(Character.isDigit(ch) || Character.isWhitespace(ch)) && word.length()<5){
                word+=ch;
            }
            else if(word.length()==5)
                break;
            else {
                word="";
                continue;
            }
        }
        return  word;
    }


}