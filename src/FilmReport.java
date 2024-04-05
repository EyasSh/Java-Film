import java.rmi.UnexpectedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmReport {
    final static  String DB_URL = "jdbc:mysql://localhost:3306/sakila";
    private String username;
    private  String password;
    Connection connection;

    public FilmReport(String username, String password) throws Exception
    {
        this.username=username;
        this.password=password;
        try {
            connection = DriverManager.getConnection(DB_URL, this.username, this.password);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle connection failure
            throw e;
        }
        createTables();
    }
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Create actor table
            String createActorTableSQL = "CREATE TABLE actor (" +
                    "actor_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "first_name VARCHAR(50) NOT NULL," +
                    "last_name VARCHAR(50) NOT NULL)";

            statement.executeUpdate(createActorTableSQL);

            // Create film table
            String createFilmTableSQL = "CREATE TABLE film (" +
                    "film_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "description TEXT," +  // Adding description column
                    "length_minutes INT UNSIGNED," + // Adding length_minutes column
                    "CONSTRAINT fk_actor_film FOREIGN KEY (film_id) REFERENCES actor(actor_id))"; // Adding foreign key constraint

            statement.executeUpdate(createFilmTableSQL);

            System.out.println("Tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public String[] getSimilarTitles( String[] titles) throws Exception
    {
        if (titles.length==0){
            System.out.println("Title array is empty");
            return  new String[0];
        }

        else
        {

                List<String[]> moviesByTitlesData = fetchMoviesByTitles(titles);
                List<String[]> allMovies = fetchMoviesAlphabetical();
            if(allMovies.size()==3 && moviesByTitlesData.size()==3)
            {
                //get the data from the title arrList

                String[] titlesASC = moviesByTitlesData.get(0);
                String[] descriptionsFromTitles = moviesByTitlesData.get(1);
                String[] timesFromTitles =moviesByTitlesData.get(2);
                //get the data of all movies
                String[] allTitles =allMovies.get(0);
                String[] allDescriptions = allMovies.get(1);
                String[] allTimes = allMovies.get(2);
                // Declaring result list
                List<String> resultList = new ArrayList<>();
                String similarTitles= "";
                for (int i =0; i< titlesASC.length;i++)
                {
                    for(int j=0; j<allTitles.length;j++)
                    {
                        if(!areSimilar(descriptionsFromTitles[i],allDescriptions[j],Integer.parseInt(timesFromTitles[i]),Integer.parseInt(allTimes[j]))){
                            continue;
                        }
                        else
                            similarTitles+=":"+allTitles[j];
                    }
                    if(similarTitles.isEmpty())
                        continue;
                    else{
                        resultList.add(titlesASC[i]+":"+similarTitles);
                        similarTitles="";
                        continue;
                    }
                }
                return resultList.toArray(new String[resultList.size()]);

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

    private boolean areSimilar(String fiveWordFirst,String fiveWordSecond,int minsFirst,int minsSecond){
        return ((WordWithFiveLetters(fiveWordFirst)!=""&& WordWithFiveLetters(fiveWordSecond)!="") && (fiveWordFirst!=fiveWordSecond)
                &&(WordWithFiveLetters(fiveWordFirst) == WordWithFiveLetters(fiveWordSecond))) && (minsFirst-minsSecond<=2 || minsSecond-minsFirst<=2);
    }
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